package com.travelplan.admin.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ManagerScore(
        UUID managerId,
        BigDecimal income,
        long trips,
        long travelers,
        double averageRating,
        long reportCount,
        double score) {
}
