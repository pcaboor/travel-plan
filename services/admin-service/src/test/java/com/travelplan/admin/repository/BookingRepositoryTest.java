package com.travelplan.admin.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.travelplan.admin.domain.Booking;
import com.travelplan.admin.domain.BookingStatus;
import com.travelplan.admin.domain.PaymentMethod;
import com.travelplan.admin.domain.PaymentMethodType;
import com.travelplan.admin.domain.PaymentProvider;
import com.travelplan.admin.domain.User;

@DataJpaTest
@AutoConfigureTestDatabase
class BookingRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void persists_and_finds_booking_by_user_and_travel() {
        User user = persistedUser("booker@example.com");
        PaymentMethod method = persistedPaymentMethod(user);
        UUID travelRef = UUID.randomUUID();

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setTravelRefId(travelRef);
        booking.setPaymentMethod(method);
        booking.setAmount(new BigDecimal("499.00"));
        bookingRepository.save(booking);

        assertThat(bookingRepository.findByUserId(user.getId())).hasSize(1);
        assertThat(bookingRepository.findByTravelRefId(travelRef)).hasSize(1);
    }

    @Test
    void deleting_payment_method_keeps_booking_with_null_payment() {
        User user = persistedUser("survivor@example.com");
        PaymentMethod method = persistedPaymentMethod(user);

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setTravelRefId(UUID.randomUUID());
        booking.setPaymentMethod(method);
        booking.setAmount(new BigDecimal("100.00"));
        Booking saved = bookingRepository.save(booking);
        bookingRepository.flush();

        paymentMethodRepository.deleteById(method.getId());
        paymentMethodRepository.flush();
        entityManager.clear();

        Booking reloaded = bookingRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getPaymentMethod()).isNull();
    }

    @Test
    void bulk_status_update_by_travel_ref() {
        User user = persistedUser("bulk@example.com");
        UUID travelRef = UUID.randomUUID();
        bookingRepository.save(bookingFor(user, travelRef));
        bookingRepository.save(bookingFor(user, travelRef));
        bookingRepository.save(bookingFor(user, UUID.randomUUID()));

        int updated = bookingRepository.updateStatusByTravelRefId(travelRef, BookingStatus.CANCELLED);

        assertThat(updated).isEqualTo(2);
        List<Booking> cancelled = bookingRepository.findByStatus(BookingStatus.CANCELLED);
        assertThat(cancelled).hasSize(2);
    }

    private User persistedUser(String email) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setPasswordHash("hash");
        return userRepository.save(user);
    }

    private PaymentMethod persistedPaymentMethod(User user) {
        PaymentMethod method = new PaymentMethod();
        method.setUser(user);
        method.setProvider(PaymentProvider.STRIPE);
        method.setType(PaymentMethodType.CARD);
        return paymentMethodRepository.save(method);
    }

    private Booking bookingFor(User user, UUID travelRef) {
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setTravelRefId(travelRef);
        booking.setAmount(new BigDecimal("50.00"));
        return booking;
    }
}
