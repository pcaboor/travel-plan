package com.travelplan.payment.provider;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.travelplan.payment.config.PaymentProperties;
import com.travelplan.payment.config.PaymentProperties.PaypalProperties;
import com.travelplan.payment.domain.PaymentStatus;

import jakarta.annotation.PostConstruct;

@Component
public class PaypalAdapter {

    private static final Logger log = LoggerFactory.getLogger(PaypalAdapter.class);

    private final PaypalProperties properties;
    private RestClient client;

    private volatile String cachedToken;
    private volatile Instant cachedTokenExpiry = Instant.EPOCH;

    public PaypalAdapter(PaymentProperties properties) {
        this.properties = properties.paypal();
    }

    @PostConstruct
    void init() {
        if (isEnabled()) {
            this.client = RestClient.builder().baseUrl(properties.apiBase()).build();
            log.info("PayPal adapter initialised against {}", properties.apiBase());
        } else {
            log.info("PayPal adapter disabled (no client credentials configured)");
        }
    }

    public boolean isEnabled() {
        return properties.enabled()
                && properties.clientId() != null && !properties.clientId().isBlank()
                && properties.clientSecret() != null && !properties.clientSecret().isBlank();
    }

    public ProviderIntent createIntent(BigDecimal amount, String currency, UUID userId, UUID bookingRefId) {
        ensureEnabled();
        String value = amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
        String reference = bookingRefId != null ? bookingRefId.toString() : userId.toString();
        Map<String, Object> body = Map.of(
                "intent", "CAPTURE",
                "purchase_units", java.util.List.of(Map.of(
                        "reference_id", reference,
                        "amount", Map.of("currency_code", currency, "value", value))));
        try {
            JsonNode response = client.post()
                    .uri("/v2/checkout/orders")
                    .headers(h -> h.setBearerAuth(accessToken()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
            return toProviderIntent(response);
        } catch (HttpClientErrorException e) {
            throw new ProviderException("PayPal createOrder failed: " + e.getResponseBodyAsString(), false, e);
        }
    }

    public ProviderIntent retrieve(String orderId) {
        ensureEnabled();
        try {
            JsonNode response = client.get()
                    .uri("/v2/checkout/orders/{id}", orderId)
                    .headers(h -> h.setBearerAuth(accessToken()))
                    .retrieve()
                    .body(JsonNode.class);
            return toProviderIntent(response);
        } catch (HttpClientErrorException e) {
            throw new ProviderException("PayPal retrieveOrder failed: " + e.getResponseBodyAsString(), false, e);
        }
    }

    public boolean verifyWebhook(String body, HttpHeaders headers) {
        if (properties.webhookId() == null || properties.webhookId().isBlank()) {
            throw new ProviderException("PAYPAL_WEBHOOK_ID is not configured");
        }
        try {
            Map<String, Object> payload = Map.of(
                    "auth_algo", firstHeader(headers, "Paypal-Auth-Algo"),
                    "cert_url", firstHeader(headers, "Paypal-Cert-Url"),
                    "transmission_id", firstHeader(headers, "Paypal-Transmission-Id"),
                    "transmission_sig", firstHeader(headers, "Paypal-Transmission-Sig"),
                    "transmission_time", firstHeader(headers, "Paypal-Transmission-Time"),
                    "webhook_id", properties.webhookId(),
                    "webhook_event", new com.fasterxml.jackson.databind.ObjectMapper().readTree(body));
            JsonNode response = client.post()
                    .uri("/v1/notifications/verify-webhook-signature")
                    .headers(h -> h.setBearerAuth(accessToken()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(JsonNode.class);
            return response != null && "SUCCESS".equals(response.path("verification_status").asText());
        } catch (Exception e) {
            throw new ProviderException("PayPal webhook verification failed: " + e.getMessage(), false, e);
        }
    }

    private void ensureEnabled() {
        if (!isEnabled()) {
            throw ProviderException.disabled("PayPal");
        }
    }

    private String accessToken() {
        if (Instant.now().isBefore(cachedTokenExpiry.minusSeconds(30))) {
            return cachedToken;
        }
        String basic = Base64.getEncoder().encodeToString(
                (properties.clientId() + ":" + properties.clientSecret()).getBytes(StandardCharsets.UTF_8));
        try {
            JsonNode response = client.post()
                    .uri("/v1/oauth2/token")
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + basic)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body("grant_type=client_credentials")
                    .retrieve()
                    .body(JsonNode.class);
            if (response == null) {
                throw new ProviderException("PayPal returned an empty token response");
            }
            this.cachedToken = response.path("access_token").asText();
            long expiresIn = response.path("expires_in").asLong(3600L);
            this.cachedTokenExpiry = Instant.now().plusSeconds(expiresIn);
            return cachedToken;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new ProviderException("PayPal credentials are invalid", false, e);
            }
            throw new ProviderException("PayPal token endpoint failed: " + e.getResponseBodyAsString(), false, e);
        }
    }

    private ProviderIntent toProviderIntent(JsonNode order) {
        String id = order.path("id").asText();
        PaymentStatus status = mapStatus(order.path("status").asText("CREATED"));
        String approvalUrl = null;
        for (JsonNode link : order.path("links")) {
            if ("approve".equalsIgnoreCase(link.path("rel").asText())) {
                approvalUrl = link.path("href").asText();
                break;
            }
        }
        return ProviderIntent.ofPaypal(id, status, approvalUrl);
    }

    static PaymentStatus mapStatus(String paypalStatus) {
        return switch (paypalStatus) {
            case "CREATED", "SAVED" -> PaymentStatus.PENDING;
            case "APPROVED" -> PaymentStatus.REQUIRES_ACTION;
            case "COMPLETED" -> PaymentStatus.SUCCEEDED;
            case "VOIDED" -> PaymentStatus.CANCELLED;
            case "PAYER_ACTION_REQUIRED" -> PaymentStatus.REQUIRES_ACTION;
            default -> PaymentStatus.FAILED;
        };
    }

    private static String firstHeader(HttpHeaders headers, String name) {
        String value = headers.getFirst(name);
        return value == null ? "" : value;
    }
}
