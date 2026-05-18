package com.travelplan.admin.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.travelplan.admin.domain.Role;
import com.travelplan.admin.domain.User;
import com.travelplan.admin.domain.UserStatus;

@DataJpaTest
@AutoConfigureTestDatabase
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void persists_and_finds_user_by_email() {
        User user = newUser("alice@example.com");
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("alice@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isNotNull();
        assertThat(found.get().getCreatedAt()).isNotNull();
        assertThat(found.get().getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void counts_users_by_status() {
        userRepository.save(newUser("a@example.com"));
        userRepository.save(newUser("b@example.com"));
        User suspended = newUser("c@example.com");
        suspended.setStatus(UserStatus.SUSPENDED);
        userRepository.save(suspended);

        assertThat(userRepository.countByStatus(UserStatus.ACTIVE)).isEqualTo(2);
        assertThat(userRepository.countByStatus(UserStatus.SUSPENDED)).isEqualTo(1);
    }

    @Test
    void associates_roles_with_user() {
        Role admin = new Role();
        admin.setName("ADMIN");
        roleRepository.save(admin);

        User user = newUser("admin@example.com");
        user.getRoles().add(admin);
        userRepository.save(user);

        User reloaded = userRepository.findByEmail("admin@example.com").orElseThrow();
        assertThat(reloaded.getRoles()).extracting(Role::getName).containsExactly("ADMIN");
    }

    @Test
    void existsByEmail_returns_true_when_user_exists() {
        userRepository.save(newUser("present@example.com"));

        assertThat(userRepository.existsByEmail("present@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("missing@example.com")).isFalse();
    }

    private User newUser(String email) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setPasswordHash("$2a$10$placeholderhash");
        user.setFirstName("Test");
        user.setLastName("User");
        return user;
    }
}
