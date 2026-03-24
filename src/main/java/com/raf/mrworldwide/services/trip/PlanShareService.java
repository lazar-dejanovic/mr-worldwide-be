package com.raf.mrworldwide.services.trip;

import com.raf.mrworldwide.dao.repositories.PlanShareRepository;
import com.raf.mrworldwide.domain.dto.trip.PlanShareDto;
import com.raf.mrworldwide.domain.dto.trip.PlanShareRequest;
import com.raf.mrworldwide.domain.dto.trip.TripPlanDto;
import com.raf.mrworldwide.domain.entities.trip.PlanShare;
import com.raf.mrworldwide.domain.entities.trip.TripPlan;
import com.raf.mrworldwide.domain.entities.user.User;
import com.raf.mrworldwide.domain.mappers.PlanShareMapper;
import com.raf.mrworldwide.domain.mappers.TripPlanMapper;
import com.raf.mrworldwide.exceptions.BadRequestException;
import com.raf.mrworldwide.exceptions.ForbiddenException;
import com.raf.mrworldwide.exceptions.NotFoundException;
import com.raf.mrworldwide.services.ums.AuthService;
import com.raf.mrworldwide.utils.AuthUtils;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class PlanShareService {

    private final PlanShareRepository planShareRepository;
    private final TripPlanService tripPlanService;
    private final AuthService authService;

    @Transactional
    public PlanShareDto share(UUID tripId, PlanShareRequest request) {
        User owner = AuthUtils.getLoggedUser();
        TripPlan tripPlan = tripPlanService.getEntityById(tripId);
        tripPlanService.verifyOwnership(owner, tripPlan);

        User invitee = authService.getUserByEmail(request.email());

        if (invitee.getId().equals(owner.getId())) {
            throw new BadRequestException("Cannot share a plan with yourself");
        }
        if (planShareRepository.existsByTripPlanAndSharedWithUser(tripPlan, invitee)) {
            throw new BadRequestException("Plan is already shared with this user");
        }

        PlanShare planShare = PlanShare.builder()
                .tripPlan(tripPlan)
                .sharedWithUser(invitee)
                .accessType(request.accessType())
                .inviteSentAt(ZonedDateTime.now())
                .build();

        return PlanShareMapper.INSTANCE.toDto(planShareRepository.save(planShare));
    }

    public List<PlanShareDto> listShares(UUID tripId) {
        User user = AuthUtils.getLoggedUser();
        TripPlan tripPlan = tripPlanService.getEntityById(tripId);
        tripPlanService.verifyOwnership(user, tripPlan);

        return planShareRepository.findByTripPlan(tripPlan)
                .stream()
                .map(PlanShareMapper.INSTANCE::toDto)
                .toList();
    }

    @Transactional
    public void revoke(UUID tripId, UUID shareId) {
        User user = AuthUtils.getLoggedUser();
        TripPlan tripPlan = tripPlanService.getEntityById(tripId);
        tripPlanService.verifyOwnership(user, tripPlan);

        PlanShare share = planShareRepository.findById(shareId)
                .orElseThrow(() -> new NotFoundException("Share with id " + shareId + " not found"));

        if (!share.getTripPlan().getId().equals(tripId)) {
            throw new ForbiddenException("Share does not belong to this trip plan");
        }

        planShareRepository.delete(share);
    }

    public Page<TripPlanDto> getSharedWithMe(Pageable pageable) {
        User user = AuthUtils.getLoggedUser();
        List<PlanShare> shares = planShareRepository.findBySharedWithUser(user);
        // Manual pagination on the in-memory list; for large datasets consider a JPA query
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), shares.size());
        List<TripPlanDto> content = shares.subList(start > shares.size() ? shares.size() : start, end)
                .stream()
                .map(s -> TripPlanMapper.INSTANCE.toDto(s.getTripPlan()))
                .toList();
        return new org.springframework.data.domain.PageImpl<>(content, pageable, shares.size());
    }
}

