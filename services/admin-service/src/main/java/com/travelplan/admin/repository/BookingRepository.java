package com.travelplan.admin.repository;

import java.math.BigDecimal;
import java.util.Collection;
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

    long countByUserIdAndStatus(UUID userId, BookingStatus status);

    long countByUserIdAndStatusIn(UUID userId, Collection<BookingStatus> statuses);

    long countByManagerIdAndStatusIn(UUID managerId, Collection<BookingStatus> statuses);

    @Query("SELECT SUM(b.amount) FROM Booking b WHERE b.managerId = :managerId AND b.status IN :statuses")
    BigDecimal sumIncomeByManager(@Param("managerId") UUID managerId,
                                  @Param("statuses") Collection<BookingStatus> statuses);

    @Query("SELECT COUNT(DISTINCT b.travelRefId) FROM Booking b WHERE b.managerId = :managerId")
    long countDistinctTravelsByManager(@Param("managerId") UUID managerId);

    @Query("SELECT b.managerId, SUM(b.amount), COUNT(b), COUNT(DISTINCT b.travelRefId) FROM Booking b "
            + "WHERE b.managerId IS NOT NULL AND b.status IN :statuses GROUP BY b.managerId")
    List<Object[]> aggregateByManager(@Param("statuses") Collection<BookingStatus> statuses);

    @Modifying
    @Query("UPDATE Booking b SET b.status = :status WHERE b.travelRefId = :travelRefId")
    int updateStatusByTravelRefId(@Param("travelRefId") UUID travelRefId, @Param("status") BookingStatus status);
}
