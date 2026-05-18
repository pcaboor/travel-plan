package com.travelplan.admin.api.dto;

import com.travelplan.admin.domain.UserStatus;

import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @Size(max = 100) String firstName,
        @Size(max = 100) String lastName,
        UserStatus status) {
}
