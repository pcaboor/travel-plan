package com.travelplan.payment.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.travelplan.payment.api.dto.BookingPaymentStatus;
import com.travelplan.payment.api.dto.CreateIntentRequest;
import com.travelplan.payment.api.dto.IntentResponse;
import com.travelplan.payment.domain.PaymentProvider;
import com.travelplan.payment.domain.PaymentStatus;
import com.travelplan.payment.domain.PaymentTransaction;
import com.travelplan.payment.provider.PaypalAdapter;
import com.travelplan.payment.provider.ProviderIntent;
import com.travelplan.payment.provider.StripeAdapter;
import com.travelplan.payment.repository.PaymentTransactionRepository;

@Service
@Transactional
public class PaymentService {

    private final PaymentTransactionRepository repository;
    private final StripeAdapter stripe;
    private final PaypalAdapter paypal;

    public PaymentService(PaymentTransactionRepository repository,
                          StripeAdapter stripe,
                          PaypalAdapter paypal) {
        this.repository = repository;
        this.stripe = stripe;
        this.paypal = paypal;
    }

    public IntentResponse createIntent(UUID userId, CreateIntentRequest request) {
        String currency = request.currency().toUpperCase();
        ProviderIntent providerIntent = switch (request.provider()) {
            case STRIPE -> stripe.createIntent(request.amount(), currency, userId, request.bookingRefId());
            case PAYPAL -> paypal.createIntent(request.amount(), currency, userId, request.bookingRefId());
            default -> throw new IllegalStateException("Unsupported provider: " + request.provider());
        };

        PaymentTransaction tx = new PaymentTransaction();
        tx.setUserId(userId);
        tx.setBookingRefId(request.bookingRefId());
        tx.setProvider(request.provider());
        tx.setProviderIntentId(providerIntent.providerIntentId());
        tx.setAmount(request.amount());
        tx.setCurrency(currency);
        tx.setStatus(providerIntent.status());
        repository.save(tx);

        return IntentResponse.from(tx, providerIntent.clientSecret(), providerIntent.approvalUrl());
    }

    @Transactional(readOnly = true)
    public BookingPaymentStatus statusForBooking(UUID bookingRefId) {
        boolean paid = repository.findByBookingRefId(bookingRefId).stream()
                .anyMatch(tx -> tx.getStatus() == PaymentStatus.SUCCEEDED);
        return new BookingPaymentStatus(bookingRefId, paid);
    }

    @Transactional(readOnly = true)
    public IntentResponse get(UUID id) {
        PaymentTransaction tx = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Payment intent not found: " + id));
        return IntentResponse.from(tx, null, null);
    }

    public IntentResponse refresh(UUID id) {
        PaymentTransaction tx = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Payment intent not found: " + id));
        ProviderIntent providerIntent = switch (tx.getProvider()) {
            case STRIPE -> stripe.retrieve(tx.getProviderIntentId());
            case PAYPAL -> paypal.retrieve(tx.getProviderIntentId());
            default -> throw new IllegalStateException("Unsupported provider: " + tx.getProvider());
        };
        tx.setStatus(providerIntent.status());
        return IntentResponse.from(tx, providerIntent.clientSecret(), providerIntent.approvalUrl());
    }

    public IntentResponse capture(UUID id) {
        PaymentTransaction tx = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Payment intent not found: " + id));
        ProviderIntent providerIntent = switch (tx.getProvider()) {
            case PAYPAL -> paypal.capture(tx.getProviderIntentId());
            case STRIPE -> stripe.retrieve(tx.getProviderIntentId());
            default -> throw new IllegalStateException("Unsupported provider: " + tx.getProvider());
        };
        tx.setStatus(providerIntent.status());
        return IntentResponse.from(tx, providerIntent.clientSecret(), providerIntent.approvalUrl());
    }

    public IntentResponse cancel(UUID id) {
        PaymentTransaction tx = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Payment intent not found: " + id));
        if (tx.getProvider() == PaymentProvider.STRIPE) {
            ProviderIntent providerIntent = stripe.cancel(tx.getProviderIntentId());
            tx.setStatus(providerIntent.status());
        } else {
            tx.setStatus(PaymentStatus.CANCELLED);
        }
        return IntentResponse.from(tx, null, null);
    }

    public void updateStatusByProviderIntentId(PaymentProvider provider,
                                               String providerIntentId,
                                               PaymentStatus status,
                                               String failureReason) {
        repository.findByProviderAndProviderIntentId(provider, providerIntentId)
                .ifPresent(tx -> {
                    tx.setStatus(status);
                    tx.setFailureReason(failureReason);
                });
    }
}
