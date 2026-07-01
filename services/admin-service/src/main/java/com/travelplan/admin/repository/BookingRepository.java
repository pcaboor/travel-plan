package com.travelplan.admin.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.travelplan.admin.domain.Booking;
import com.travelplan.admin.domain.BookingStatus;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    List<Booking> findByUserId(UUID userId);

    List<Booking> findByTravelRefId(UUID travelRefId);

    List<Booking> findByUserIdAndTravelRefId(UUID userId, UUID travelRefId);

    List<Booking> findByStatus(BookingStatus status);

    @Modifying
    @Query("UPDATE Booking b SET b.status = :status WHERE b.travelRefId = :travelRefId")
    int updateStatusByTravelRefId(@Param("travelRefId") UUID travelRefId, @Param("status") BookingStatus status);
}
