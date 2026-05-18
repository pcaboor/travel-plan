package com.travelplan.payment.provider;

public class ProviderException extends RuntimeException {

    private final boolean disabled;

    public ProviderException(String message) {
        this(message, false, null);
    }

    public ProviderException(String message, boolean disabled, Throwable cause) {
        super(message, cause);
        this.disabled = disabled;
    }

    public static ProviderException disabled(String provider) {
        return new ProviderException(provider + " is not configured on this deployment", true, null);
    }

    public boolean isDisabled() {
        return disabled;
    }
}
