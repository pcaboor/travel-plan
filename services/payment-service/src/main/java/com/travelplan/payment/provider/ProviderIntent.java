package com.travelplan.payment.provider;

import com.travelplan.payment.domain.PaymentStatus;

public record ProviderIntent(
        String providerIntentId,
        PaymentStatus status,
        String clientSecret,
        String approvalUrl) {

    public static ProviderIntent ofStripe(String id, PaymentStatus status, String clientSecret) {
        return new ProviderIntent(id, status, clientSecret, null);
    }

    public static ProviderIntent ofPaypal(String id, PaymentStatus status, String approvalUrl) {
        return new ProviderIntent(id, status, null, approvalUrl);
    }
}
