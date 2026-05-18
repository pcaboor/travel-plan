package com.travelplan.payment.api;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.travelplan.payment.domain.PaymentProvider;
import com.travelplan.payment.domain.PaymentStatus;
import com.travelplan.payment.provider.PaypalAdapter;
import com.travelplan.payment.provider.StripeAdapter;
import com.travelplan.payment.service.PaymentService;

@RestController
@RequestMapping("/api/payments/webhooks")
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    private final StripeAdapter stripe;
    private final PaypalAdapter paypal;
    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    public WebhookController(StripeAdapter stripe,
                             PaypalAdapter paypal,
                             PaymentService paymentService,
                             ObjectMapper objectMapper) {
        this.stripe = stripe;
        this.paypal = paypal;
        this.paymentService = paymentService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/stripe")
    public ResponseEntity<Map<String, String>> stripe(@RequestHeader("Stripe-Signature") String signature,
                                                      @RequestBody String payload) {
        Event event = stripe.verifyWebhook(payload, signature);
        StripeObject object = event.getDataObjectDeserializer().getObject().orElse(null);
        if (object instanceof PaymentIntent intent) {
            PaymentStatus status = StripeAdapter.mapStatus(intent.getStatus());
            String failureReason = intent.getLastPaymentError() != null
                    ? intent.getLastPaymentError().getMessage()
                    : null;
            paymentService.updateStatusByProviderIntentId(
                    PaymentProvider.STRIPE, intent.getId(), status, failureReason);
        } else {
            log.debug("Ignoring Stripe event {}", event.getType());
        }
        return ResponseEntity.ok(Map.of("received", event.getId()));
    }

    @PostMapping("/paypal")
    public ResponseEntity<Map<String, String>> paypal(@RequestBody String payload,
                                                      @RequestHeader HttpHeaders headers) throws Exception {
        if (!paypal.verifyWebhook(payload, headers)) {
            return ResponseEntity.status(400).body(Map.of("error", "invalid_signature"));
        }
        JsonNode event = objectMapper.readTree(payload);
        String eventType = event.path("event_type").asText();
        String resourceId = event.path("resource").path("id").asText();
        PaymentStatus status = mapPaypalEvent(eventType);
        if (status != null && !resourceId.isBlank()) {
            paymentService.updateStatusByProviderIntentId(
                    PaymentProvider.PAYPAL, resourceId, status, null);
        }
        return ResponseEntity.ok(Map.of("received", event.path("id").asText()));
    }

    private static PaymentStatus mapPaypalEvent(String eventType) {
        return switch (eventType) {
            case "CHECKOUT.ORDER.APPROVED" -> PaymentStatus.REQUIRES_ACTION;
            case "CHECKOUT.ORDER.COMPLETED", "PAYMENT.CAPTURE.COMPLETED" -> PaymentStatus.SUCCEEDED;
            case "PAYMENT.CAPTURE.DENIED" -> PaymentStatus.FAILED;
            case "PAYMENT.CAPTURE.REFUNDED" -> PaymentStatus.REFUNDED;
            case "CHECKOUT.ORDER.VOIDED" -> PaymentStatus.CANCELLED;
            default -> null;
        };
    }
}
