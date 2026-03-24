package com.raf.mrworldwide.dao.repositories;

import com.raf.mrworldwide.domain.entities.trip.PlanShare;
import com.raf.mrworldwide.domain.entities.trip.TripPlan;
import com.raf.mrworldwide.domain.entities.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlanShareRepository extends JpaRepository<PlanShare, UUID> {

    List<PlanShare> findByTripPlan(TripPlan tripPlan);

    List<PlanShare> findBySharedWithUser(User user);

    boolean existsByTripPlanAndSharedWithUser(TripPlan tripPlan, User user);
}

