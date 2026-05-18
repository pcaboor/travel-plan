package com.travelplan.payment.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.travelplan.payment.provider.PaypalAdapter;
import com.travelplan.payment.provider.ProviderException;
import com.travelplan.payment.provider.StripeAdapter;

@SpringBootTest
@AutoConfigureMockMvc
class WebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StripeAdapter stripeAdapter;

    @MockBean
    private PaypalAdapter paypalAdapter;

    @Test
    void stripe_webhook_returns_400_for_invalid_signature() throws Exception {
        when(stripeAdapter.verifyWebhook(anyString(), anyString()))
                .thenThrow(new ProviderException("Invalid signature"));

        mockMvc.perform(post("/api/payments/webhooks/stripe")
                        .header("Stripe-Signature", "garbage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.error").value("provider_error"));
    }

    @Test
    void stripe_webhook_accepts_valid_event() throws Exception {
        Event event = mock(Event.class);
        when(event.getId()).thenReturn("evt_test");
        when(event.getType()).thenReturn("payment_intent.succeeded");
        PaymentIntent intent = mock(PaymentIntent.class);
        when(intent.getId()).thenReturn("pi_unknown_to_db");
        when(intent.getStatus()).thenReturn("succeeded");
        EventDataObjectDeserializer deserializer = mock(EventDataObjectDeserializer.class);
        when(deserializer.getObject()).thenReturn(Optional.of(intent));
        when(event.getDataObjectDeserializer()).thenReturn(deserializer);
        when(stripeAdapter.verifyWebhook(anyString(), anyString())).thenReturn(event);

        mockMvc.perform(post("/api/payments/webhooks/stripe")
                        .header("Stripe-Signature", "valid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"evt_test\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.received").value("evt_test"));
    }

    @Test
    void paypal_webhook_rejects_unverified_payload() throws Exception {
        when(paypalAdapter.verifyWebhook(anyString(), any())).thenReturn(false);

        mockMvc.perform(post("/api/payments/webhooks/paypal")
                        .header("Paypal-Auth-Algo", "SHA256")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"event_type\":\"CHECKOUT.ORDER.COMPLETED\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void paypal_webhook_accepts_verified_payload() throws Exception {
        when(paypalAdapter.verifyWebhook(anyString(), any())).thenReturn(true);

        mockMvc.perform(post("/api/payments/webhooks/paypal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"WH-EVT\",\"event_type\":\"CHECKOUT.ORDER.COMPLETED\","
                                + "\"resource\":{\"id\":\"ORDER-XYZ\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.received").value("WH-EVT"));
    }
}
