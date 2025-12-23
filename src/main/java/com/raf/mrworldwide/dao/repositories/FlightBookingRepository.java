package com.raf.mrworldwide.dao.repositories;

import com.raf.mrworldwide.domain.entities.plan.transportation.FlightBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FlightBookingRepository extends JpaRepository<FlightBooking, UUID> {
}
