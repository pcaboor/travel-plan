package com.travelplan.admin.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.travelplan.admin.domain.User;
import com.travelplan.admin.domain.UserStatus;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByStatus(UserStatus status);
}
