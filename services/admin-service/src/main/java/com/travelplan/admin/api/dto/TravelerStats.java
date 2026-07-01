package com.travelplan.admin.api.dto;

import java.util.List;

public record TravelerStats(
        long participations,
        long cancellations,
        long reportsFiled,
        long feedbackGiven,
        List<String> paymentProviders) {
}
