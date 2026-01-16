package com.raf.mrworldwide.services.trip;

import com.raf.mrworldwide.dao.repositories.TripPlanRepository;
import com.raf.mrworldwide.domain.dto.trip.TripPlanDto;
import com.raf.mrworldwide.domain.entities.trip.TripPlan;
import com.raf.mrworldwide.domain.entities.user.User;
import com.raf.mrworldwide.domain.mappers.TripPlanMapper;
import com.raf.mrworldwide.exceptions.BadRequestException;
import com.raf.mrworldwide.exceptions.NotFoundException;
import com.raf.mrworldwide.utils.AuthUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class TripPlanService {

    private final TripPlanRepository tripPlanRepository;

    public TripPlanDto getById(UUID id) {
        User user = AuthUtils.getLoggedUser();
        TripPlan tripPlan = getEntityById(id);

        if (!user.getEmail().equals(tripPlan.getCreatedBy())) {
            throw new BadRequestException("You are not authorized to view this trip plan");
        }

        return TripPlanMapper.INSTANCE.toDto(tripPlan);
    }

    public List<TripPlanDto> getTripsForUser() {
        User user = AuthUtils.getLoggedUser();
        List<TripPlan> tripPlans = tripPlanRepository.findByUser(user);

        return tripPlans.stream().map(TripPlanMapper.INSTANCE::toDto).toList();
    }

    public TripPlan getEntityById(UUID id) {
        return tripPlanRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Trip with id " + id + " not found"));
    }
}
