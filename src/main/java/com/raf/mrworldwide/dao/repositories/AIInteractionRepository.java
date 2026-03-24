package com.raf.mrworldwide.dao.repositories;

import com.raf.mrworldwide.domain.entities.ai.AIInteraction;
import com.raf.mrworldwide.domain.entities.trip.TripPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AIInteractionRepository extends JpaRepository<AIInteraction, UUID> {

    List<AIInteraction> findByTripPlanOrderByTimestampAsc(TripPlan tripPlan);
}

