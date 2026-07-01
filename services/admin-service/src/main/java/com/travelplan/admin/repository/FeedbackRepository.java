package com.travelplan.admin.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.travelplan.admin.domain.Feedback;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, UUID> {

    List<Feedback> findByTravelRefId(UUID travelRefId);

    List<Feedback> findByAuthorUserId(UUID authorUserId);

    boolean existsByAuthorUserIdAndTravelRefId(UUID authorUserId, UUID travelRefId);

    long countByAuthorUserId(UUID authorUserId);

    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.managerId = :managerId")
    Double avgRatingByManager(@Param("managerId") UUID managerId);

    @Query("SELECT f.managerId, AVG(f.rating) FROM Feedback f WHERE f.managerId IS NOT NULL GROUP BY f.managerId")
    List<Object[]> avgRatingByManagerGrouped();
}
