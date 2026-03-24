package com.raf.mrworldwide.services.trip;

import com.raf.mrworldwide.dao.repositories.TripPlanRepository;
import com.raf.mrworldwide.domain.dto.trip.TripPlanDetailDto;
import com.raf.mrworldwide.domain.dto.trip.TripPlanDto;
import com.raf.mrworldwide.domain.dto.trip.TripPlanRequest;
import com.raf.mrworldwide.domain.entities.trip.TripPlan;
import com.raf.mrworldwide.domain.entities.trip.TripPlanStatus;
import com.raf.mrworldwide.domain.entities.user.User;
import com.raf.mrworldwide.domain.mappers.TripPlanMapper;
import com.raf.mrworldwide.exceptions.BadRequestException;
import com.raf.mrworldwide.exceptions.ForbiddenException;
import com.raf.mrworldwide.exceptions.NotFoundException;
import com.raf.mrworldwide.utils.AuthUtils;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class TripPlanService {

    private final TripPlanRepository tripPlanRepository;

    public TripPlanDetailDto getById(UUID id) {
        User user = AuthUtils.getLoggedUser();
        TripPlan tripPlan = getEntityById(id);

        if (!user.getEmail().equals(tripPlan.getCreatedBy())) {
            throw new ForbiddenException("You are not authorized to view this trip plan");
        }

        return TripPlanMapper.INSTANCE.toDetailDto(tripPlan);
    }

    public Page<TripPlanDto> getTripsForUser(Pageable pageable) {
        User user = AuthUtils.getLoggedUser();
        return tripPlanRepository.findByUserOrderByCreatedOnDesc(user, pageable)
                .map(TripPlanMapper.INSTANCE::toDto);
    }

    @Transactional
    public TripPlanDto create(TripPlanRequest request) {
        User user = AuthUtils.getLoggedUser();

        TripPlan tripPlan = TripPlan.builder()
                .name(request.name())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .interests(request.interests())
                .status(TripPlanStatus.DRAFT)
                .user(user)
                .build();

        return TripPlanMapper.INSTANCE.toDto(tripPlanRepository.save(tripPlan));
    }

    @Transactional
    public TripPlanDto update(UUID id, TripPlanRequest request) {
        User user = AuthUtils.getLoggedUser();
        TripPlan tripPlan = getEntityById(id);
        verifyOwnership(user, tripPlan);

        tripPlan.setName(request.name());
        tripPlan.setInterests(request.interests());
        if (request.startDate() != null) tripPlan.setStartDate(request.startDate());
        if (request.endDate() != null) tripPlan.setEndDate(request.endDate());

        return TripPlanMapper.INSTANCE.toDto(tripPlanRepository.save(tripPlan));
    }

    @Transactional
    public TripPlanDto updateStatus(UUID id, TripPlanStatus newStatus) {
        User user = AuthUtils.getLoggedUser();
        TripPlan tripPlan = getEntityById(id);
        verifyOwnership(user, tripPlan);

        validateStatusTransition(tripPlan.getStatus(), newStatus);
        tripPlan.setStatus(newStatus);

        return TripPlanMapper.INSTANCE.toDto(tripPlanRepository.save(tripPlan));
    }

    @Transactional
    public void delete(UUID id) {
        User user = AuthUtils.getLoggedUser();
        TripPlan tripPlan = getEntityById(id);
        verifyOwnership(user, tripPlan);

        tripPlanRepository.delete(tripPlan);
    }

    public TripPlan getEntityById(UUID id) {
        return tripPlanRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Trip with id " + id + " not found"));
    }

    public void verifyOwnership(User user, TripPlan tripPlan) {
        if (!user.getEmail().equals(tripPlan.getCreatedBy())) {
            throw new ForbiddenException("You are not authorized to modify this trip plan");
        }
    }

    private void validateStatusTransition(TripPlanStatus current, TripPlanStatus next) {
        boolean valid = switch (current) {
            case DRAFT -> next == TripPlanStatus.PLANNED;
            case PLANNED -> next == TripPlanStatus.BOOKED;
            case BOOKED -> next == TripPlanStatus.COMPLETED;
            case COMPLETED -> false;
        };
        if (!valid) {
            throw new BadRequestException("Invalid status transition from " + current + " to " + next);
        }
    }
}
