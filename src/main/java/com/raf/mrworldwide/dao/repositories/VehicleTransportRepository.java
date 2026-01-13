package com.raf.mrworldwide.dao.repositories;

import com.raf.mrworldwide.domain.entities.transport.VehicleTransport;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VehicleTransportRepository extends CrudRepository<VehicleTransport, UUID> {
}
