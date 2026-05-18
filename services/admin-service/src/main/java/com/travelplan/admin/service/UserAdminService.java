package com.travelplan.admin.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.travelplan.admin.api.dto.UserCreateRequest;
import com.travelplan.admin.api.dto.UserResponse;
import com.travelplan.admin.api.dto.UserUpdateRequest;
import com.travelplan.admin.domain.Role;
import com.travelplan.admin.domain.User;
import com.travelplan.admin.domain.UserStatus;
import com.travelplan.admin.repository.RoleRepository;
import com.travelplan.admin.repository.UserRepository;

@Service
@Transactional
public class UserAdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAdminService(UserRepository userRepository,
                            RoleRepository roleRepository,
                            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse create(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already in use: " + request.email());
        }
        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setStatus(Optional.ofNullable(request.status()).orElse(UserStatus.ACTIVE));
        if (request.roles() != null && !request.roles().isEmpty()) {
            user.setRoles(resolveRoles(request.roles()));
        }
        return UserResponse.from(userRepository.save(user));
    }

    public UserResponse update(UUID userId, UserUpdateRequest request) {
        User user = findOrThrow(userId);
        if (request.firstName() != null) {
            user.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            user.setLastName(request.lastName());
        }
        if (request.status() != null) {
            user.setStatus(request.status());
        }
        return UserResponse.from(user);
    }

    public void changePassword(UUID userId, String newPassword) {
        User user = findOrThrow(userId);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
    }

    public UserResponse assignRoles(UUID userId, Set<String> roleNames) {
        User user = findOrThrow(userId);
        user.setRoles(resolveRoles(roleNames));
        return UserResponse.from(user);
    }

    public UserResponse removeRole(UUID userId, String roleName) {
        User user = findOrThrow(userId);
        boolean removed = user.getRoles().removeIf(r -> r.getName().equalsIgnoreCase(roleName));
        if (!removed) {
            throw new NotFoundException("User does not have role: " + roleName);
        }
        return UserResponse.from(user);
    }

    public void delete(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found: " + userId);
        }
        userRepository.deleteById(userId);
    }

    @Transactional(readOnly = true)
    public UserResponse get(UUID userId) {
        return UserResponse.from(findOrThrow(userId));
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> list(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserResponse::from);
    }

    private User findOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }

    private Set<Role> resolveRoles(Set<String> roleNames) {
        Set<Role> resolved = new HashSet<>();
        List<String> missing = new java.util.ArrayList<>();
        for (String name : roleNames) {
            roleRepository.findByName(name).ifPresentOrElse(resolved::add, () -> missing.add(name));
        }
        if (!missing.isEmpty()) {
            throw new NotFoundException("Unknown roles: " + missing);
        }
        return resolved;
    }
}
