package com.travelplan.admin.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.travelplan.admin.domain.Report;
import com.travelplan.admin.domain.ReportStatus;
import com.travelplan.admin.domain.ReportTargetType;

public record ReportResponse(
        UUID id,
        UUID reporterUserId,
        ReportTargetType targetType,
        UUID targetId,
        String reason,
        ReportStatus status,
        OffsetDateTime createdAt) {

    public static ReportResponse from(Report r) {
        return new ReportResponse(r.getId(), r.getReporterUserId(), r.getTargetType(),
                r.getTargetId(), r.getReason(), r.getStatus(), r.getCreatedAt());
    }
}
