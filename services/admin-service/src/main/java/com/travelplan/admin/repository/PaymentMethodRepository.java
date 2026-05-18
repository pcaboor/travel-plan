package com.travelplan.admin.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.travelplan.admin.domain.PaymentMethod;
import com.travelplan.admin.domain.PaymentMethodStatus;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, UUID> {

    List<PaymentMethod> findByUserId(UUID userId);

    List<PaymentMethod> findByUserIdAndStatus(UUID userId, PaymentMethodStatus status);
}
