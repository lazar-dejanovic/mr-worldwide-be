package com.raf.mrworldwide.domain.dto.trip;

import com.raf.mrworldwide.domain.dto.BaseEntityDto;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public record AirplaneTransportDto(
        BaseEntityDto base,
        String flightNumber,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime,
        String duration,
        Double price,
        String currency
) {
    public UUID id() {
        return base != null ? base.id() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AirplaneTransportDto that)) return false;
        return id() != null && Objects.equals(id(), that.id());
    }

    @Override
    public int hashCode() {
        return id() != null ? Objects.hash(id()) : 0;
    }
}

