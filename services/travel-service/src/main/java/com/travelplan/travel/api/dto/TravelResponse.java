package com.travelplan.travel.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import com.travelplan.travel.domain.Accommodation;
import com.travelplan.travel.domain.AccommodationType;
import com.travelplan.travel.domain.Activity;
import com.travelplan.travel.domain.Destination;
import com.travelplan.travel.domain.Transportation;
import com.travelplan.travel.domain.TransportationType;
import com.travelplan.travel.domain.Travel;
import com.travelplan.travel.domain.TravelStatus;

public record TravelResponse(
        String id,
        String title,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        Integer durationDays,
        BigDecimal price,
        String currency,
        TravelStatus status,
        String managerId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<DestinationView> destinations,
        List<ActivityView> activities,
        List<AccommodationView> accommodations,
        List<TransportationView> transportations) {

    public record DestinationView(String id, String name, String country, Double latitude,
                                  Double longitude, Integer order) {
        public static DestinationView from(Destination dest, Integer order) {
            return new DestinationView(dest.getId(), dest.getName(), dest.getCountry(),
                    dest.getLatitude(), dest.getLongitude(), order);
        }
    }

    public record ActivityView(String id, String name, String description, String category,
                               Integer durationMinutes) {
        public static ActivityView from(Activity a) {
            return new ActivityView(a.getId(), a.getName(), a.getDescription(), a.getCategory(),
                    a.getDurationMinutes());
        }
    }

    public record AccommodationView(String id, String name, AccommodationType type, String address) {
        public static AccommodationView from(Accommodation a) {
            return new AccommodationView(a.getId(), a.getName(), a.getType(), a.getAddress());
        }
    }

    public record TransportationView(String id, TransportationType type, String provider,
                                     String departureLocation, String arrivalLocation,
                                     OffsetDateTime departureTime, OffsetDateTime arrivalTime) {
        public static TransportationView from(Transportation t) {
            return new TransportationView(t.getId(), t.getType(), t.getProvider(),
                    t.getDepartureLocation(), t.getArrivalLocation(),
                    t.getDepartureTime(), t.getArrivalTime());
        }
    }

    public static TravelResponse from(Travel t) {
        return new TravelResponse(
                t.getId(), t.getTitle(), t.getDescription(),
                t.getStartDate(), t.getEndDate(), t.getDurationDays(),
                t.getPrice(), t.getCurrency(), t.getStatus(),
                t.getManagerId(), t.getCreatedAt(), t.getUpdatedAt(),
                t.getDestinations().stream()
                        .map(v -> DestinationView.from(v.getDestination(), v.getOrder()))
                        .toList(),
                t.getActivities().stream().map(ActivityView::from).toList(),
                t.getAccommodations().stream().map(AccommodationView::from).toList(),
                t.getTransportations().stream().map(TransportationView::from).toList());
    }
}
