package com.travelplan.travel.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.travelplan.travel.api.dto.AccommodationInput;
import com.travelplan.travel.api.dto.ActivityInput;
import com.travelplan.travel.api.dto.DestinationInput;
import com.travelplan.travel.api.dto.TransportationInput;
import com.travelplan.travel.api.dto.TravelCreateRequest;
import com.travelplan.travel.api.dto.TravelResponse;
import com.travelplan.travel.api.dto.TravelUpdateRequest;
import com.travelplan.travel.domain.Accommodation;
import com.travelplan.travel.domain.Activity;
import com.travelplan.travel.domain.Destination;
import com.travelplan.travel.domain.DestinationVisit;
import com.travelplan.travel.domain.Transportation;
import com.travelplan.travel.domain.Travel;
import com.travelplan.travel.domain.TravelStatus;
import com.travelplan.travel.repository.TravelRepository;

@Service
@Transactional
public class TravelService {

    private final TravelRepository travelRepository;

    public TravelService(TravelRepository travelRepository) {
        this.travelRepository = travelRepository;
    }

    public TravelResponse create(TravelCreateRequest request) {
        Travel travel = new Travel();
        apply(travel, request.title(), request.description(), request.startDate(), request.endDate(),
                request.durationDays(), request.price(), request.currency(),
                Optional.ofNullable(request.status()).orElse(TravelStatus.DRAFT));
        travel.setDestinations(buildDestinations(request.destinations()));
        travel.setActivities(buildActivities(request.activities()));
        travel.setAccommodations(buildAccommodations(request.accommodations()));
        travel.setTransportations(buildTransportations(request.transportations()));
        OffsetDateTime now = OffsetDateTime.now();
        travel.setCreatedAt(now);
        travel.setUpdatedAt(now);
        return TravelResponse.from(travelRepository.save(travel));
    }

    public TravelResponse update(String id, TravelUpdateRequest request) {
        Travel travel = findOrThrow(id);
        if (request.title() != null) {
            travel.setTitle(request.title());
        }
        if (request.description() != null) {
            travel.setDescription(request.description());
        }
        if (request.startDate() != null) {
            travel.setStartDate(request.startDate());
        }
        if (request.endDate() != null) {
            travel.setEndDate(request.endDate());
        }
        if (request.durationDays() != null) {
            travel.setDurationDays(request.durationDays());
        }
        if (request.price() != null) {
            travel.setPrice(request.price());
        }
        if (request.currency() != null) {
            travel.setCurrency(request.currency());
        }
        if (request.status() != null) {
            travel.setStatus(request.status());
        }
        if (request.destinations() != null) {
            travel.setDestinations(buildDestinations(request.destinations()));
        }
        if (request.activities() != null) {
            travel.setActivities(buildActivities(request.activities()));
        }
        if (request.accommodations() != null) {
            travel.setAccommodations(buildAccommodations(request.accommodations()));
        }
        if (request.transportations() != null) {
            travel.setTransportations(buildTransportations(request.transportations()));
        }
        travel.setUpdatedAt(OffsetDateTime.now());
        return TravelResponse.from(travelRepository.save(travel));
    }

    public void delete(String id) {
        if (!travelRepository.existsById(id)) {
            throw new NotFoundException("Travel not found: " + id);
        }
        travelRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public TravelResponse get(String id) {
        return TravelResponse.from(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<TravelResponse> list(Pageable pageable) {
        return travelRepository.findAll(pageable).map(TravelResponse::from);
    }

    private Travel findOrThrow(String id) {
        return travelRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Travel not found: " + id));
    }

    private void apply(Travel travel, String title, String description, java.time.LocalDate start,
                       java.time.LocalDate end, Integer duration, java.math.BigDecimal price,
                       String currency, TravelStatus status) {
        travel.setTitle(title);
        travel.setDescription(description);
        travel.setStartDate(start);
        travel.setEndDate(end);
        travel.setDurationDays(duration);
        travel.setPrice(price);
        travel.setCurrency(Optional.ofNullable(currency).orElse("EUR"));
        travel.setStatus(status);
    }

    private List<DestinationVisit> buildDestinations(List<DestinationInput> inputs) {
        if (inputs == null) {
            return new ArrayList<>();
        }
        List<DestinationVisit> visits = new ArrayList<>(inputs.size());
        for (DestinationInput input : inputs) {
            Destination destination = new Destination();
            destination.setName(input.name());
            destination.setCountry(input.country());
            destination.setLatitude(input.latitude());
            destination.setLongitude(input.longitude());
            visits.add(new DestinationVisit(destination, input.order()));
        }
        return visits;
    }

    private List<Activity> buildActivities(List<ActivityInput> inputs) {
        if (inputs == null) {
            return new ArrayList<>();
        }
        return inputs.stream().map(input -> {
            Activity a = new Activity();
            a.setName(input.name());
            a.setDescription(input.description());
            a.setCategory(input.category());
            a.setDurationMinutes(input.durationMinutes());
            return a;
        }).toList();
    }

    private List<Accommodation> buildAccommodations(List<AccommodationInput> inputs) {
        if (inputs == null) {
            return new ArrayList<>();
        }
        return inputs.stream().map(input -> {
            Accommodation a = new Accommodation();
            a.setName(input.name());
            a.setType(input.type());
            a.setAddress(input.address());
            return a;
        }).toList();
    }

    private List<Transportation> buildTransportations(List<TransportationInput> inputs) {
        if (inputs == null) {
            return new ArrayList<>();
        }
        return inputs.stream().map(input -> {
            Transportation t = new Transportation();
            t.setType(input.type());
            t.setProvider(input.provider());
            t.setDepartureLocation(input.departureLocation());
            t.setArrivalLocation(input.arrivalLocation());
            t.setDepartureTime(input.departureTime());
            t.setArrivalTime(input.arrivalTime());
            return t;
        }).toList();
    }
}
