package com.travelplan.admin.api.dto;

import java.util.UUID;

import com.travelplan.admin.domain.ReportTargetType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReportCreateRequest(
        @NotNull ReportTargetType targetType,
        @NotNull UUID targetId,
        @NotBlank @Size(max = 2000) String reason) {
}
