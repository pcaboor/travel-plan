package com.travelplan.admin.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.travelplan.admin.domain.Role;

@DataJpaTest
@AutoConfigureTestDatabase
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void enforces_unique_role_names() {
        Role admin = new Role();
        admin.setName("ADMIN");
        roleRepository.save(admin);

        assertThat(roleRepository.existsByName("ADMIN")).isTrue();
        assertThat(roleRepository.findByName("ADMIN")).isPresent();
        assertThat(roleRepository.existsByName("UNKNOWN")).isFalse();
    }
}
