package com.travelplan.admin.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.travelplan.admin.domain.PaymentMethod;
import com.travelplan.admin.domain.PaymentMethodStatus;
import com.travelplan.admin.domain.PaymentMethodType;
import com.travelplan.admin.domain.PaymentProvider;
import com.travelplan.admin.domain.User;

@DataJpaTest
@AutoConfigureTestDatabase
class PaymentMethodRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void finds_payment_methods_by_user() {
        User user = persistedUser("payer@example.com");
        paymentMethodRepository.save(stripeCardFor(user));
        paymentMethodRepository.save(paypalWalletFor(user));

        List<PaymentMethod> methods = paymentMethodRepository.findByUserId(user.getId());

        assertThat(methods).hasSize(2)
                .extracting(PaymentMethod::getProvider)
                .containsExactlyInAnyOrder(PaymentProvider.STRIPE, PaymentProvider.PAYPAL);
    }

    @Test
    void filters_by_status() {
        User user = persistedUser("filter@example.com");
        PaymentMethod active = stripeCardFor(user);
        PaymentMethod revoked = stripeCardFor(user);
        revoked.setStatus(PaymentMethodStatus.REVOKED);
        paymentMethodRepository.save(active);
        paymentMethodRepository.save(revoked);

        List<PaymentMethod> activeMethods =
                paymentMethodRepository.findByUserIdAndStatus(user.getId(), PaymentMethodStatus.ACTIVE);

        assertThat(activeMethods).hasSize(1);
    }

    @Test
    void deleting_user_cascades_payment_methods() {
        User user = persistedUser("cascade@example.com");
        paymentMethodRepository.saveAndFlush(stripeCardFor(user));
        entityManager.clear();

        userRepository.deleteById(user.getId());
        userRepository.flush();
        entityManager.clear();

        assertThat(paymentMethodRepository.findByUserId(user.getId())).isEmpty();
    }

    private User persistedUser(String email) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setPasswordHash("hash");
        return userRepository.save(user);
    }

    private PaymentMethod stripeCardFor(User user) {
        PaymentMethod method = new PaymentMethod();
        method.setUser(user);
        method.setProvider(PaymentProvider.STRIPE);
        method.setType(PaymentMethodType.CARD);
        method.setLastFour("4242");
        return method;
    }

    private PaymentMethod paypalWalletFor(User user) {
        PaymentMethod method = new PaymentMethod();
        method.setUser(user);
        method.setProvider(PaymentProvider.PAYPAL);
        method.setType(PaymentMethodType.WALLET);
        return method;
    }
}
