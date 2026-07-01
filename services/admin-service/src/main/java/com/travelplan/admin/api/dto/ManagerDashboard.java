package com.travelplan.admin.api.dto;

import java.math.BigDecimal;

public record ManagerDashboard(
        BigDecimal income,
        long trips,
        long travelers,
        double averageRating) {
}
