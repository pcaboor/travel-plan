package com.travelplan.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.travelplan.auth.domain.AuthRole;

@Repository
public interface AuthRoleRepository extends JpaRepository<AuthRole, Long> {

    Optional<AuthRole> findByName(String name);
}
