package com.raf.mrworldwide.dao.repositories;

import com.raf.mrworldwide.domain.entities.trip.TripPlan;
import com.raf.mrworldwide.domain.entities.trip.TripSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TripSegmentRepository extends JpaRepository<TripSegment, UUID> {

    List<TripSegment> findByTripPlan(TripPlan tripPlan);

    List<TripSegment> findByTripPlanOrderByOrderIndexAsc(TripPlan tripPlan);

    long countByTripPlan(TripPlan tripPlan);
}
