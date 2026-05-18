package com.travelplan.payment.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.travelplan.payment.domain.PaymentProvider;
import com.travelplan.payment.domain.PaymentTransaction;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {

    Optional<PaymentTransaction> findByProviderAndProviderIntentId(PaymentProvider provider,
                                                                   String providerIntentId);
}
