package com.travelplan.admin.api.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.travelplan.admin.domain.Role;
import com.travelplan.admin.domain.User;
import com.travelplan.admin.domain.UserStatus;

public record UserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        UserStatus status,
        List<String> roles,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getStatus(),
                user.getRoles().stream().map(Role::getName).sorted().toList(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}
