package com.raf.mrworldwide.dao.repositories;

import com.raf.mrworldwide.domain.entities.plan.itinerary.DailyItinerary;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DailyItineraryRepository extends CrudRepository<DailyItinerary, UUID> {
}
