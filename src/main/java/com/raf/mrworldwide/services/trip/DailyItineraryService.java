package com.raf.mrworldwide.services.trip;

import com.raf.mrworldwide.dao.repositories.DailyItineraryRepository;
import com.raf.mrworldwide.domain.dto.trip.DailyItineraryDto;
import com.raf.mrworldwide.domain.dto.trip.DailyItineraryRequest;
import com.raf.mrworldwide.domain.entities.trip.DailyItinerary;
import com.raf.mrworldwide.domain.entities.trip.TripPlan;
import com.raf.mrworldwide.domain.entities.trip.TripSegment;
import com.raf.mrworldwide.domain.entities.user.User;
import com.raf.mrworldwide.domain.mappers.DailyItineraryMapper;
import com.raf.mrworldwide.exceptions.BadRequestException;
import com.raf.mrworldwide.exceptions.ForbiddenException;
import com.raf.mrworldwide.exceptions.NotFoundException;
import com.raf.mrworldwide.utils.AuthUtils;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class DailyItineraryService {

    private final DailyItineraryRepository dailyItineraryRepository;
    private final TripPlanService tripPlanService;
    private final TripSegmentService tripSegmentService;

    public Page<DailyItineraryDto> getAllForSegment(UUID tripId, UUID segmentId, Pageable pageable) {
        TripSegment segment = resolveAndVerify(tripId, segmentId);
        return dailyItineraryRepository.findByTripSegment(segment, pageable)
                .map(DailyItineraryMapper.INSTANCE::toDto);
    }

    public DailyItineraryDto getById(UUID tripId, UUID segmentId, UUID itineraryId) {
        TripSegment segment = resolveAndVerify(tripId, segmentId);
        DailyItinerary itinerary = getEntityById(itineraryId);
        verifyItineraryBelongsToSegment(itinerary, segment);
        return DailyItineraryMapper.INSTANCE.toDto(itinerary);
    }

    @Transactional
    public DailyItineraryDto create(UUID tripId, UUID segmentId, DailyItineraryRequest request) {
        TripSegment segment = resolveAndVerify(tripId, segmentId);
        validateDay(request, segment);
        validateNoTimeOverlap(segment, request, null);

        DailyItinerary itinerary = DailyItinerary.builder()
                .name(request.name())
                .category(request.category())
                .categoryIconUrl(request.categoryIconUrl())
                .address(request.address())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .day(request.day())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .tripSegment(segment)
                .build();

        return DailyItineraryMapper.INSTANCE.toDto(dailyItineraryRepository.save(itinerary));
    }

    @Transactional
    public DailyItineraryDto update(UUID tripId, UUID segmentId, UUID itineraryId, DailyItineraryRequest request) {
        TripSegment segment = resolveAndVerify(tripId, segmentId);
        DailyItinerary itinerary = getEntityById(itineraryId);
        verifyItineraryBelongsToSegment(itinerary, segment);
        validateDay(request, segment);
        validateNoTimeOverlap(segment, request, itineraryId);

        itinerary.setName(request.name());
        itinerary.setCategory(request.category());
        itinerary.setCategoryIconUrl(request.categoryIconUrl());
        itinerary.setAddress(request.address());
        itinerary.setLatitude(request.latitude());
        itinerary.setLongitude(request.longitude());
        itinerary.setDay(request.day());
        itinerary.setStartTime(request.startTime());
        itinerary.setEndTime(request.endTime());

        return DailyItineraryMapper.INSTANCE.toDto(dailyItineraryRepository.save(itinerary));
    }

    @Transactional
    public void delete(UUID tripId, UUID segmentId, UUID itineraryId) {
        TripSegment segment = resolveAndVerify(tripId, segmentId);
        DailyItinerary itinerary = getEntityById(itineraryId);
        verifyItineraryBelongsToSegment(itinerary, segment);
        dailyItineraryRepository.delete(itinerary);
    }

    private TripSegment resolveAndVerify(UUID tripId, UUID segmentId) {
        User user = AuthUtils.getLoggedUser();
        TripPlan tripPlan = tripPlanService.getEntityById(tripId);
        tripPlanService.verifyOwnership(user, tripPlan);
        return tripSegmentService.getEntityById(segmentId);
    }

    private void validateDay(DailyItineraryRequest request, TripSegment segment) {
        if (request.day() == null) return;
        if (segment.getArrivalDate() != null && request.day().isBefore(segment.getArrivalDate())) {
            throw new BadRequestException("Activity day cannot be before segment arrivalDate");
        }
        if (segment.getDepartureDate() != null && !request.day().isBefore(segment.getDepartureDate())) {
            throw new BadRequestException("Activity day must be before segment departureDate");
        }
    }

    private void validateNoTimeOverlap(TripSegment segment, DailyItineraryRequest request, UUID excludeId) {
        if (request.day() == null || request.startTime() == null || request.endTime() == null) return;

        List<DailyItinerary> same = dailyItineraryRepository.findByTripSegmentAndDay(segment, request.day());
        for (DailyItinerary existing : same) {
            if (excludeId != null && existing.getId().equals(excludeId)) continue;
            if (existing.getStartTime() == null || existing.getEndTime() == null) continue;
            boolean overlaps = request.startTime().isBefore(existing.getEndTime())
                    && request.endTime().isAfter(existing.getStartTime());
            if (overlaps) {
                throw new BadRequestException("Time slot overlaps with existing activity: " + existing.getName());
            }
        }
    }

    private void verifyItineraryBelongsToSegment(DailyItinerary itinerary, TripSegment segment) {
        if (!itinerary.getTripSegment().getId().equals(segment.getId())) {
            throw new ForbiddenException("Itinerary does not belong to this segment");
        }
    }

    public DailyItinerary getEntityById(UUID id) {
        return dailyItineraryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Itinerary with id " + id + " not found"));
    }
}

