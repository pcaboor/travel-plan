package com.travelplan.auth.api.dto;

import java.util.List;
import java.util.UUID;

import com.travelplan.auth.domain.AuthRole;
import com.travelplan.auth.domain.AuthUser;

public record UserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        List<String> roles) {

    public static UserResponse from(AuthUser user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRoles().stream().map(AuthRole::getName).sorted().toList());
    }
}
