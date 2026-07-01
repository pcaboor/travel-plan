package com.travelplan.admin.api.dto;

import com.travelplan.admin.domain.ReportStatus;

import jakarta.validation.constraints.NotNull;

public record ReportStatusUpdateRequest(@NotNull ReportStatus status) {
}
