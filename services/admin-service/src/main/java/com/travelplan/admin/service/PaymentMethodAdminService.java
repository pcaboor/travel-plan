package com.travelplan.admin.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.travelplan.admin.api.dto.PaymentMethodCreateRequest;
import com.travelplan.admin.api.dto.PaymentMethodResponse;
import com.travelplan.admin.api.dto.PaymentMethodUpdateRequest;
import com.travelplan.admin.domain.PaymentMethod;
import com.travelplan.admin.domain.PaymentMethodStatus;
import com.travelplan.admin.domain.User;
import com.travelplan.admin.repository.PaymentMethodRepository;
import com.travelplan.admin.repository.UserRepository;

@Service
@Transactional
public class PaymentMethodAdminService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final UserRepository userRepository;

    public PaymentMethodAdminService(PaymentMethodRepository paymentMethodRepository,
                                     UserRepository userRepository) {
        this.paymentMethodRepository = paymentMethodRepository;
        this.userRepository = userRepository;
    }

    public PaymentMethodResponse create(UUID userId, PaymentMethodCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        PaymentMethod method = new PaymentMethod();
        method.setUser(user);
        method.setProvider(request.provider());
        method.setType(request.type());
        method.setProviderToken(request.providerToken());
        method.setLastFour(request.lastFour());
        method.setExpiresAt(request.expiresAt());
        method.setStatus(Optional.ofNullable(request.status()).orElse(PaymentMethodStatus.ACTIVE));
        return PaymentMethodResponse.from(paymentMethodRepository.save(method));
    }

    public PaymentMethodResponse update(UUID userId, UUID methodId, PaymentMethodUpdateRequest request) {
        PaymentMethod method = findOrThrow(userId, methodId);
        if (request.lastFour() != null) {
            method.setLastFour(request.lastFour());
        }
        if (request.expiresAt() != null) {
            method.setExpiresAt(request.expiresAt());
        }
        if (request.status() != null) {
            method.setStatus(request.status());
        }
        return PaymentMethodResponse.from(method);
    }

    public void delete(UUID userId, UUID methodId) {
        PaymentMethod method = findOrThrow(userId, methodId);
        paymentMethodRepository.delete(method);
    }

    @Transactional(readOnly = true)
    public PaymentMethodResponse get(UUID userId, UUID methodId) {
        return PaymentMethodResponse.from(findOrThrow(userId, methodId));
    }

    @Transactional(readOnly = true)
    public List<PaymentMethodResponse> list(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found: " + userId);
        }
        return paymentMethodRepository.findByUserId(userId).stream()
                .map(PaymentMethodResponse::from)
                .toList();
    }

    private PaymentMethod findOrThrow(UUID userId, UUID methodId) {
        PaymentMethod method = paymentMethodRepository.findById(methodId)
                .orElseThrow(() -> new NotFoundException("Payment method not found: " + methodId));
        if (!method.getUser().getId().equals(userId)) {
            throw new NotFoundException("Payment method " + methodId + " not attached to user " + userId);
        }
        return method;
    }
}
