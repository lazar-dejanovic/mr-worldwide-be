package com.raf.mrworldwide.dao.repositories;

import com.raf.mrworldwide.domain.entities.plan.stay.Accommodation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AccommodationRepository extends JpaRepository<Accommodation, UUID> {
}
