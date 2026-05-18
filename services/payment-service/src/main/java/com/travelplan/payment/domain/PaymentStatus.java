package com.travelplan.payment.domain;

public enum PaymentStatus {
    PENDING,
    REQUIRES_ACTION,
    PROCESSING,
    SUCCEEDED,
    FAILED,
    CANCELLED,
    REFUNDED
}
