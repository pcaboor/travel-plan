package com.travelplan.admin.api.dto;

import java.util.Set;

import jakarta.validation.constraints.NotEmpty;

public record RoleAssignmentRequest(
        @NotEmpty Set<String> roles) {
}
