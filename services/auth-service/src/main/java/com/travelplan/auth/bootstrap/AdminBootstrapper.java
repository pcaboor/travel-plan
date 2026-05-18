package com.travelplan.auth.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.travelplan.auth.domain.AuthRole;
import com.travelplan.auth.domain.AuthUser;
import com.travelplan.auth.repository.AuthRoleRepository;
import com.travelplan.auth.repository.AuthUserRepository;

@Component
public class AdminBootstrapper implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapper.class);
    private static final String ADMIN_ROLE = "ADMIN";

    private final AuthUserRepository userRepository;
    private final AuthRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final BootstrapProperties properties;

    public AdminBootstrapper(AuthUserRepository userRepository,
                             AuthRoleRepository roleRepository,
                             PasswordEncoder passwordEncoder,
                             BootstrapProperties properties) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (properties.adminEmail() == null || properties.adminPassword() == null) {
            log.info("Admin bootstrap disabled (no credentials configured)");
            return;
        }
        if (userRepository.existsByEmail(properties.adminEmail())) {
            log.info("Admin bootstrap skipped: user {} already exists", properties.adminEmail());
            return;
        }
        AuthRole adminRole = roleRepository.findByName(ADMIN_ROLE)
                .orElseThrow(() -> new IllegalStateException(
                        "ADMIN role is missing — run admin-service Flyway migrations first"));

        AuthUser admin = new AuthUser();
        admin.setEmail(properties.adminEmail());
        admin.setPasswordHash(passwordEncoder.encode(properties.adminPassword()));
        admin.setFirstName("Travel");
        admin.setLastName("Admin");
        admin.getRoles().add(adminRole);
        userRepository.save(admin);

        log.warn("Bootstrap admin {} created with default password — change it as soon as possible",
                properties.adminEmail());
    }
}
