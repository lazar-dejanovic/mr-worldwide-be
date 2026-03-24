package com.raf.mrworldwide.dao.repositories;

import com.raf.mrworldwide.domain.entities.trip.TripPlan;
import com.raf.mrworldwide.domain.entities.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TripPlanRepository extends JpaRepository<TripPlan, UUID> {

    List<TripPlan> findByUser(User user);

    Page<TripPlan> findByUserOrderByCreatedOnDesc(User user, Pageable pageable);
}
