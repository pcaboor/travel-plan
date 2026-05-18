package com.travelplan.admin.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.travelplan.admin.api.dto.BookingResponse;
import com.travelplan.admin.domain.Booking;
import com.travelplan.admin.domain.BookingStatus;
import com.travelplan.admin.repository.BookingRepository;
import com.travelplan.admin.repository.UserRepository;

@Service
@Transactional
public class BookingAdminService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public BookingAdminService(BookingRepository bookingRepository, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Page<BookingResponse> list(Pageable pageable) {
        return bookingRepository.findAll(pageable).map(BookingResponse::from);
    }

    @Transactional(readOnly = true)
    public BookingResponse get(UUID id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking not found: " + id));
        return BookingResponse.from(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> listByUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found: " + userId);
        }
        return bookingRepository.findByUserId(userId).stream()
                .map(BookingResponse::from)
                .toList();
    }

    public int cancelByTravel(UUID travelRefId) {
        return bookingRepository.updateStatusByTravelRefId(travelRefId, BookingStatus.CANCELLED);
    }
}
