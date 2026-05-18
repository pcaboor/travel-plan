package com.travelplan.payment.provider;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import com.travelplan.payment.config.PaymentProperties;
import com.travelplan.payment.config.PaymentProperties.StripeProperties;
import com.travelplan.payment.domain.PaymentStatus;

import jakarta.annotation.PostConstruct;

@Component
public class StripeAdapter {

    private static final Logger log = LoggerFactory.getLogger(StripeAdapter.class);

    private final StripeProperties properties;

    public StripeAdapter(PaymentProperties properties) {
        this.properties = properties.stripe();
    }

    @PostConstruct
    void init() {
        if (isEnabled()) {
            Stripe.apiKey = properties.apiKey();
            log.info("Stripe adapter initialised (sandbox or live depending on the API key)");
        } else {
            log.info("Stripe adapter disabled (no API key configured)");
        }
    }

    public boolean isEnabled() {
        return properties.enabled()
                && properties.apiKey() != null
                && !properties.apiKey().isBlank();
    }

    public ProviderIntent createIntent(BigDecimal amount, String currency, UUID userId, UUID bookingRefId) {
        ensureEnabled();
        Map<String, String> metadata = new HashMap<>();
        metadata.put("userId", userId.toString());
        if (bookingRefId != null) {
            metadata.put("bookingRefId", bookingRefId.toString());
        }
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(toMinorUnits(amount))
                .setCurrency(currency.toLowerCase())
                .setAutomaticPaymentMethods(PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                        .setEnabled(true)
                        .build())
                .putAllMetadata(metadata)
                .build();
        try {
            PaymentIntent intent = PaymentIntent.create(params);
            return ProviderIntent.ofStripe(intent.getId(), mapStatus(intent.getStatus()), intent.getClientSecret());
        } catch (StripeException e) {
            throw new ProviderException("Stripe createIntent failed: " + e.getMessage(), false, e);
        }
    }

    public ProviderIntent retrieve(String paymentIntentId) {
        ensureEnabled();
        try {
            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
            return ProviderIntent.ofStripe(intent.getId(), mapStatus(intent.getStatus()), intent.getClientSecret());
        } catch (StripeException e) {
            throw new ProviderException("Stripe retrieve failed: " + e.getMessage(), false, e);
        }
    }

    public ProviderIntent cancel(String paymentIntentId) {
        ensureEnabled();
        try {
            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId).cancel();
            return ProviderIntent.ofStripe(intent.getId(), mapStatus(intent.getStatus()), intent.getClientSecret());
        } catch (StripeException e) {
            throw new ProviderException("Stripe cancel failed: " + e.getMessage(), false, e);
        }
    }

    public Event verifyWebhook(String payload, String signatureHeader) {
        if (properties.webhookSecret() == null || properties.webhookSecret().isBlank()) {
            throw new ProviderException("STRIPE_WEBHOOK_SECRET is not configured");
        }
        try {
            return Webhook.constructEvent(payload, signatureHeader, properties.webhookSecret());
        } catch (Exception e) {
            throw new ProviderException("Invalid Stripe webhook signature: " + e.getMessage(), false, e);
        }
    }

    private void ensureEnabled() {
        if (!isEnabled()) {
            throw ProviderException.disabled("Stripe");
        }
    }

    public static PaymentStatus mapStatus(String stripeStatus) {
        return switch (stripeStatus) {
            case "requires_payment_method", "requires_confirmation" -> PaymentStatus.PENDING;
            case "requires_action" -> PaymentStatus.REQUIRES_ACTION;
            case "processing" -> PaymentStatus.PROCESSING;
            case "succeeded" -> PaymentStatus.SUCCEEDED;
            case "canceled" -> PaymentStatus.CANCELLED;
            case "requires_capture" -> PaymentStatus.PROCESSING;
            default -> PaymentStatus.FAILED;
        };
    }

    static long toMinorUnits(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValueExact();
    }
}
