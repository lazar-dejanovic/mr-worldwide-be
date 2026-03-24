package com.raf.mrworldwide.dao.repositories;

import com.raf.mrworldwide.domain.entities.trip.DailyItinerary;
import com.raf.mrworldwide.domain.entities.trip.TripSegment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface DailyItineraryRepository extends JpaRepository<DailyItinerary, UUID> {

    Page<DailyItinerary> findByTripSegment(TripSegment tripSegment, Pageable pageable);

    List<DailyItinerary> findByTripSegmentAndDay(TripSegment tripSegment, LocalDate day);
}
