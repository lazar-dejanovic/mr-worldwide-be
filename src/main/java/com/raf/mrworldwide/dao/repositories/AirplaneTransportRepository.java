package com.raf.mrworldwide.dao.repositories;

import com.raf.mrworldwide.domain.entities.transport.AirplaneTransport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AirplaneTransportRepository extends JpaRepository<AirplaneTransport, UUID> {
}
