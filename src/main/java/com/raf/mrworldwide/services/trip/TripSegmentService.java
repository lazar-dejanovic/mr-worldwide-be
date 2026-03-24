package com.raf.mrworldwide.services.trip;

import com.raf.mrworldwide.dao.repositories.TripSegmentRepository;
import com.raf.mrworldwide.domain.dto.trip.SegmentOrderItem;
import com.raf.mrworldwide.domain.dto.trip.TripSegmentDetailDto;
import com.raf.mrworldwide.domain.dto.trip.TripSegmentDto;
import com.raf.mrworldwide.domain.dto.trip.TripSegmentRequest;
import com.raf.mrworldwide.domain.entities.trip.TripPlan;
import com.raf.mrworldwide.domain.entities.trip.TripSegment;
import com.raf.mrworldwide.domain.entities.user.User;
import com.raf.mrworldwide.domain.mappers.TripSegmentMapper;
import com.raf.mrworldwide.exceptions.BadRequestException;
import com.raf.mrworldwide.exceptions.ForbiddenException;
import com.raf.mrworldwide.exceptions.NotFoundException;
import com.raf.mrworldwide.utils.AuthUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class TripSegmentService {

    private final TripSegmentRepository tripSegmentRepository;
    private final TripPlanService tripPlanService;

    public List<TripSegmentDto> getAllForTrip(UUID tripId) {
        User user = AuthUtils.getLoggedUser();
        TripPlan tripPlan = tripPlanService.getEntityById(tripId);
        tripPlanService.verifyOwnership(user, tripPlan);

        return tripSegmentRepository.findByTripPlanOrderByOrderIndexAsc(tripPlan)
                .stream()
                .map(TripSegmentMapper.INSTANCE::toDto)
                .toList();
    }

    public TripSegmentDetailDto getById(UUID tripId, UUID segmentId) {
        User user = AuthUtils.getLoggedUser();
        TripPlan tripPlan = tripPlanService.getEntityById(tripId);
        tripPlanService.verifyOwnership(user, tripPlan);

        TripSegment segment = getEntityById(segmentId);
        verifySegmentBelongsToTrip(segment, tripPlan);

        return TripSegmentMapper.INSTANCE.toDetailDto(segment);
    }

    @Transactional
    public TripSegmentDto create(UUID tripId, TripSegmentRequest request) {
        User user = AuthUtils.getLoggedUser();
        TripPlan tripPlan = tripPlanService.getEntityById(tripId);
        tripPlanService.verifyOwnership(user, tripPlan);

        validateSegmentDates(request);

        List<TripSegment> existing = tripSegmentRepository.findByTripPlanOrderByOrderIndexAsc(tripPlan);
        if (!existing.isEmpty()) {
            TripSegment last = existing.get(existing.size() - 1);
            if (last.getDepartureDate() != null && !last.getDepartureDate().equals(request.arrivalDate())) {
                throw new BadRequestException(
                        "New segment arrivalDate must equal the last segment's departureDate (" + last.getDepartureDate() + ")");
            }
        }

        TripSegment segment = TripSegment.builder()
                .departure(request.departure())
                .destination(request.destination())
                .arrivalDate(request.arrivalDate())
                .departureDate(request.departureDate())
                .destinationLatitude(request.destinationLatitude())
                .destinationLongitude(request.destinationLongitude())
                .orderIndex(existing.size())
                .tripPlan(tripPlan)
                .build();

        segment = tripSegmentRepository.save(segment);
        syncPlanMetadata(tripPlan);

        return TripSegmentMapper.INSTANCE.toDto(segment);
    }

    @Transactional
    public TripSegmentDto update(UUID tripId, UUID segmentId, TripSegmentRequest request) {
        User user = AuthUtils.getLoggedUser();
        TripPlan tripPlan = tripPlanService.getEntityById(tripId);
        tripPlanService.verifyOwnership(user, tripPlan);

        TripSegment segment = getEntityById(segmentId);
        verifySegmentBelongsToTrip(segment, tripPlan);
        validateSegmentDates(request);

        segment.setDeparture(request.departure());
        segment.setDestination(request.destination());
        segment.setArrivalDate(request.arrivalDate());
        segment.setDepartureDate(request.departureDate());
        segment.setDestinationLatitude(request.destinationLatitude());
        segment.setDestinationLongitude(request.destinationLongitude());

        segment = tripSegmentRepository.save(segment);
        syncPlanMetadata(tripPlan);

        return TripSegmentMapper.INSTANCE.toDto(segment);
    }

    @Transactional
    public void delete(UUID tripId, UUID segmentId) {
        User user = AuthUtils.getLoggedUser();
        TripPlan tripPlan = tripPlanService.getEntityById(tripId);
        tripPlanService.verifyOwnership(user, tripPlan);

        TripSegment segment = getEntityById(segmentId);
        verifySegmentBelongsToTrip(segment, tripPlan);

        tripSegmentRepository.delete(segment);

        // Re-compact order indices
        List<TripSegment> remaining = tripSegmentRepository.findByTripPlanOrderByOrderIndexAsc(tripPlan);
        for (int i = 0; i < remaining.size(); i++) {
            remaining.get(i).setOrderIndex(i);
        }
        tripSegmentRepository.saveAll(remaining);
        syncPlanMetadata(tripPlan);
    }

    @Transactional
    public List<TripSegmentDto> reorder(UUID tripId, List<SegmentOrderItem> orderItems) {
        User user = AuthUtils.getLoggedUser();
        TripPlan tripPlan = tripPlanService.getEntityById(tripId);
        tripPlanService.verifyOwnership(user, tripPlan);

        List<TripSegment> segments = tripSegmentRepository.findByTripPlan(tripPlan);
        Map<UUID, TripSegment> segmentMap = segments.stream()
                .collect(Collectors.toMap(TripSegment::getId, Function.identity()));

        for (SegmentOrderItem item : orderItems) {
            TripSegment seg = segmentMap.get(item.segmentId());
            if (seg == null) {
                throw new NotFoundException("Segment with id " + item.segmentId() + " not found in this trip");
            }
            seg.setOrderIndex(item.orderIndex());
        }

        tripSegmentRepository.saveAll(segments);

        return tripSegmentRepository.findByTripPlanOrderByOrderIndexAsc(tripPlan)
                .stream()
                .map(TripSegmentMapper.INSTANCE::toDto)
                .toList();
    }

    public TripSegment getEntityById(UUID segmentId) {
        return tripSegmentRepository.findById(segmentId)
                .orElseThrow(() -> new NotFoundException("Segment with id " + segmentId + " not found"));
    }

    private void verifySegmentBelongsToTrip(TripSegment segment, TripPlan tripPlan) {
        if (!segment.getTripPlan().getId().equals(tripPlan.getId())) {
            throw new ForbiddenException("Segment does not belong to this trip plan");
        }
    }

    private void validateSegmentDates(TripSegmentRequest request) {
        if (request.arrivalDate() != null && request.departureDate() != null
                && !request.arrivalDate().isBefore(request.departureDate())) {
            throw new BadRequestException("arrivalDate must be before departureDate");
        }
    }

    private void syncPlanMetadata(TripPlan tripPlan) {
        List<TripSegment> all = tripSegmentRepository.findByTripPlanOrderByOrderIndexAsc(tripPlan);
        if (all.isEmpty()) {
            tripPlan.setStartDate(null);
            tripPlan.setEndDate(null);
            tripPlan.setDestinations(List.of());
        } else {
            tripPlan.setStartDate(all.get(0).getArrivalDate());
            tripPlan.setEndDate(all.get(all.size() - 1).getDepartureDate());
            tripPlan.setDestinations(all.stream()
                    .map(TripSegment::getDestination)
                    .filter(d -> d != null && !d.isBlank())
                    .distinct()
                    .toList());
        }
    }
}

