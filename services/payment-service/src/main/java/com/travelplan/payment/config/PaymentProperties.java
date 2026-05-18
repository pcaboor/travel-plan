package com.travelplan.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = "payment.providers")
public record PaymentProperties(
        @NestedConfigurationProperty StripeProperties stripe,
        @NestedConfigurationProperty PaypalProperties paypal) {

    public record StripeProperties(
            boolean enabled,
            String apiKey,
            String webhookSecret) {
    }

    public record PaypalProperties(
            boolean enabled,
            String clientId,
            String clientSecret,
            String apiBase,
            String webhookId) {
    }
}
