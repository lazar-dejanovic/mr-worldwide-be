package com.raf.mrworldwide.domain.dto.trip;

import com.raf.mrworldwide.domain.dto.BaseEntityDto;
import com.raf.mrworldwide.domain.entities.transport.TransportType;

import java.util.Objects;
import java.util.UUID;

public record TransportDto(
        BaseEntityDto base,
        TransportType transportType,
        AirplaneTransportDto airplaneTransport,
        VehicleTransportDto vehicleTransport
) {
    public UUID id() {
        return base != null ? base.id() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransportDto that)) return false;
        return id() != null && Objects.equals(id(), that.id());
    }

    @Override
    public int hashCode() {
        return id() != null ? Objects.hash(id()) : 0;
    }
}

