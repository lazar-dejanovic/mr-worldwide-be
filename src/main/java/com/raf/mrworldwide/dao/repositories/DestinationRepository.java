package com.raf.mrworldwide.dao.repositories;

import com.raf.mrworldwide.domain.entities.plan.Destination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DestinationRepository extends JpaRepository<Destination, UUID> {
}
