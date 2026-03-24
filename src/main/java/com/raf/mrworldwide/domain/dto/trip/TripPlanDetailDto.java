package com.raf.mrworldwide.domain.dto.trip;

import com.raf.mrworldwide.domain.dto.BaseEntityDto;
import com.raf.mrworldwide.domain.entities.trip.TripPlanStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record TripPlanDetailDto(
        BaseEntityDto base,
        String name,
        List<String> destinations,
        LocalDate startDate,
        LocalDate endDate,
        List<String> interests,
        TripPlanStatus status,
        List<TripSegmentDto> tripSegments
) {
    public UUID id() {
        return base != null ? base.id() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TripPlanDetailDto that)) return false;
        return id() != null && Objects.equals(id(), that.id());
    }

    @Override
    public int hashCode() {
        return id() != null ? Objects.hash(id()) : 0;
    }
}

