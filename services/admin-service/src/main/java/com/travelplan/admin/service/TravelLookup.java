package com.travelplan.admin.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Read-only view of a Travel owned by travel-service, fetched at subscribe time.
 * Abstracted behind an interface so it can be mocked in tests.
 */
public interface TravelLookup {

    Optional<TravelSnapshot> fetch(UUID travelId, String authorization);

    record TravelSnapshot(LocalDate startDate, BigDecimal price, String currency, String status, String managerId) {
    }
}
