package com.travelplan.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.travelplan.admin.api.dto.RoleAssignmentRequest;
import com.travelplan.admin.api.dto.UserCreateRequest;
import com.travelplan.admin.api.dto.UserResponse;
import com.travelplan.admin.api.dto.UserUpdateRequest;
import com.travelplan.admin.domain.Role;
import com.travelplan.admin.domain.UserStatus;
import com.travelplan.admin.repository.RoleRepository;
import com.travelplan.admin.repository.UserRepository;

@SpringBootTest
@Transactional
class UserAdminServiceTest {

    @Autowired
    private UserAdminService service;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void seedRoles() {
        if (roleRepository.findByName("ADMIN").isEmpty()) {
            Role admin = new Role();
            admin.setName("ADMIN");
            roleRepository.save(admin);
        }
        if (roleRepository.findByName("VIEWER").isEmpty()) {
            Role viewer = new Role();
            viewer.setName("VIEWER");
            roleRepository.save(viewer);
        }
    }

    @Test
    void creates_user_with_hashed_password_and_roles() {
        UserResponse created = service.create(new UserCreateRequest(
                "new-user@example.com", "Secret123", "Alice", "Smith",
                UserStatus.ACTIVE, Set.of("VIEWER")));

        assertThat(created.id()).isNotNull();
        assertThat(created.email()).isEqualTo("new-user@example.com");
        assertThat(created.roles()).containsExactly("VIEWER");

        var persisted = userRepository.findById(created.id()).orElseThrow();
        assertThat(passwordEncoder.matches("Secret123", persisted.getPasswordHash())).isTrue();
    }

    @Test
    void rejects_duplicate_email() {
        service.create(new UserCreateRequest(
                "dup@example.com", "Secret123", null, null, null, null));

        assertThatThrownBy(() -> service.create(new UserCreateRequest(
                "dup@example.com", "Other123", null, null, null, null)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void rejects_unknown_role() {
        assertThatThrownBy(() -> service.create(new UserCreateRequest(
                "unknown-role@example.com", "Secret123", null, null, null, Set.of("GHOST"))))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updates_user_fields_without_touching_password() {
        UserResponse created = service.create(new UserCreateRequest(
                "edit@example.com", "Secret123", "Old", "Name", null, null));
        String hashBefore = userRepository.findById(created.id()).orElseThrow().getPasswordHash();

        UserResponse updated = service.update(created.id(),
                new UserUpdateRequest("New", "Surname", UserStatus.SUSPENDED));

        assertThat(updated.firstName()).isEqualTo("New");
        assertThat(updated.lastName()).isEqualTo("Surname");
        assertThat(updated.status()).isEqualTo(UserStatus.SUSPENDED);
        assertThat(userRepository.findById(created.id()).orElseThrow().getPasswordHash())
                .isEqualTo(hashBefore);
    }

    @Test
    void changes_password_with_new_hash() {
        UserResponse created = service.create(new UserCreateRequest(
                "rotate@example.com", "Original1", null, null, null, null));

        service.changePassword(created.id(), "Rotated123");

        var user = userRepository.findById(created.id()).orElseThrow();
        assertThat(passwordEncoder.matches("Rotated123", user.getPasswordHash())).isTrue();
        assertThat(passwordEncoder.matches("Original1", user.getPasswordHash())).isFalse();
    }

    @Test
    void replaces_roles_via_assign() {
        UserResponse created = service.create(new UserCreateRequest(
                "roles@example.com", "Secret123", null, null, null, Set.of("VIEWER")));

        UserResponse updated = service.assignRoles(created.id(),
                new RoleAssignmentRequest(Set.of("ADMIN")).roles());

        assertThat(updated.roles()).containsExactly("ADMIN");
    }

    @Test
    void removes_a_single_role() {
        UserResponse created = service.create(new UserCreateRequest(
                "multi@example.com", "Secret123", null, null, null, Set.of("ADMIN", "VIEWER")));

        UserResponse updated = service.removeRole(created.id(), "VIEWER");

        assertThat(updated.roles()).containsExactly("ADMIN");
    }

    @Test
    void delete_removes_user() {
        UserResponse created = service.create(new UserCreateRequest(
                "delete@example.com", "Secret123", null, null, null, null));

        service.delete(created.id());

        assertThat(userRepository.existsById(created.id())).isFalse();
    }

    @Test
    void list_returns_pageable() {
        service.create(new UserCreateRequest("a@example.com", "Secret123", null, null, null, null));
        service.create(new UserCreateRequest("b@example.com", "Secret123", null, null, null, null));

        var page = service.list(PageRequest.of(0, 50));

        assertThat(page.getContent()).extracting(UserResponse::email)
                .contains("a@example.com", "b@example.com");
    }
}
