package com.travelplan.admin.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.travelplan.admin.domain.Feedback;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, UUID> {

    List<Feedback> findByTravelRefId(UUID travelRefId);

    List<Feedback> findByAuthorUserId(UUID authorUserId);

    boolean existsByAuthorUserIdAndTravelRefId(UUID authorUserId, UUID travelRefId);
}
