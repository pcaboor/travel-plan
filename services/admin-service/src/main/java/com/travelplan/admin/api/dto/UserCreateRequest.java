package com.travelplan.admin.api.dto;

import java.util.Set;

import com.travelplan.admin.domain.UserStatus;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
        @Email @NotBlank @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 255) String password,
        @Size(max = 100) String firstName,
        @Size(max = 100) String lastName,
        UserStatus status,
        Set<String> roles) {
}
