package com.raf.mrworldwide.services.trip;

import com.raf.mrworldwide.dao.repositories.AccommodationRepository;
import com.raf.mrworldwide.domain.dto.accomodation.AccommodationDto;
import com.raf.mrworldwide.domain.dto.accomodation.AccommodationRequest;
import com.raf.mrworldwide.domain.entities.accomodation.Accommodation;
import com.raf.mrworldwide.domain.entities.transport.AirplaneTransport;
import com.raf.mrworldwide.domain.entities.transport.TransportType;
import com.raf.mrworldwide.domain.entities.trip.TripPlan;
import com.raf.mrworldwide.domain.entities.trip.TripSegment;
import com.raf.mrworldwide.domain.entities.user.User;
import com.raf.mrworldwide.domain.mappers.AccommodationMapper;
import com.raf.mrworldwide.exceptions.NotFoundException;
import com.raf.mrworldwide.utils.AuthUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.UUID;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class AccommodationService {

    private static final LocalTime LATE_NIGHT_START = LocalTime.of(22, 0);
    private static final LocalTime LATE_NIGHT_END = LocalTime.of(6, 0);

    private final AccommodationRepository accommodationRepository;
    private final TripPlanService tripPlanService;
    private final TripSegmentService tripSegmentService;

    public AccommodationDto get(UUID tripId, UUID segmentId) {
        User user = AuthUtils.getLoggedUser();
        TripPlan tripPlan = tripPlanService.getEntityById(tripId);
        tripPlanService.verifyOwnership(user, tripPlan);

        TripSegment segment = tripSegmentService.getEntityById(segmentId);
        if (segment.getAccommodation() == null) {
            throw new NotFoundException("No accommodation found for this segment");
        }
        return AccommodationMapper.INSTANCE.toDto(segment.getAccommodation());
    }

    @Transactional
    public AccommodationDto create(UUID tripId, UUID segmentId, AccommodationRequest request) {
        User user = AuthUtils.getLoggedUser();
        TripPlan tripPlan = tripPlanService.getEntityById(tripId);
        tripPlanService.verifyOwnership(user, tripPlan);

        TripSegment segment = tripSegmentService.getEntityById(segmentId);

        // Auto-fill check-in/check-out from segment dates if not provided
        var checkIn = request.checkIn() != null ? request.checkIn() : segment.getArrivalDate();
        var checkOut = request.checkOut() != null ? request.checkOut() : segment.getDepartureDate();

        // Apply late-night landing shift if applicable
        if (hasLateNightArrival(segment)) {
            checkIn = checkIn != null ? checkIn.minusDays(1) : null;
        }

        Accommodation accommodation = Accommodation.builder()
                .name(request.name())
                .address(request.address())
                .imageUrl(request.imageUrl())
                .bookingUrl(request.bookingUrl())
                .starRating(request.starRating())
                .reviewScore(request.reviewScore())
                .checkIn(checkIn)
                .checkOut(checkOut)
                .priceTotal(request.priceTotal())
                .currency(request.currency())
                .build();

        accommodation = accommodationRepository.save(accommodation);
        segment.setAccommodation(accommodation);

        return AccommodationMapper.INSTANCE.toDto(accommodation);
    }

    @Transactional
    public AccommodationDto update(UUID tripId, UUID segmentId, AccommodationRequest request) {
        User user = AuthUtils.getLoggedUser();
        TripPlan tripPlan = tripPlanService.getEntityById(tripId);
        tripPlanService.verifyOwnership(user, tripPlan);

        TripSegment segment = tripSegmentService.getEntityById(segmentId);
        if (segment.getAccommodation() == null) {
            throw new NotFoundException("No accommodation found for this segment");
        }

        Accommodation accommodation = segment.getAccommodation();
        accommodation.setName(request.name());
        accommodation.setAddress(request.address());
        accommodation.setImageUrl(request.imageUrl());
        accommodation.setBookingUrl(request.bookingUrl());
        accommodation.setStarRating(request.starRating());
        accommodation.setReviewScore(request.reviewScore());
        accommodation.setCheckIn(request.checkIn());
        accommodation.setCheckOut(request.checkOut());
        accommodation.setPriceTotal(request.priceTotal());
        accommodation.setCurrency(request.currency());

        return AccommodationMapper.INSTANCE.toDto(accommodationRepository.save(accommodation));
    }

    @Transactional
    public void delete(UUID tripId, UUID segmentId) {
        User user = AuthUtils.getLoggedUser();
        TripPlan tripPlan = tripPlanService.getEntityById(tripId);
        tripPlanService.verifyOwnership(user, tripPlan);

        TripSegment segment = tripSegmentService.getEntityById(segmentId);
        if (segment.getAccommodation() == null) {
            throw new NotFoundException("No accommodation found for this segment");
        }

        Accommodation accommodation = segment.getAccommodation();
        segment.setAccommodation(null);
        accommodationRepository.delete(accommodation);
    }

    private boolean hasLateNightArrival(TripSegment segment) {
        if (segment.getTransport() == null) return false;
        if (segment.getTransport().getTransportType() != TransportType.AIRPLANE) return false;
        AirplaneTransport airplane = segment.getTransport().getAirplaneTransport();
        if (airplane == null || airplane.getArrivalTime() == null) return false;
        LocalTime arrival = airplane.getArrivalTime().toLocalTime();
        return !arrival.isBefore(LATE_NIGHT_START) || arrival.isBefore(LATE_NIGHT_END);
    }
}

