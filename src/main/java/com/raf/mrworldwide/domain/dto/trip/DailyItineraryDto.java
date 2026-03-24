package com.raf.mrworldwide.domain.dto.trip;

import com.raf.mrworldwide.domain.dto.BaseEntityDto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

public record DailyItineraryDto(
        BaseEntityDto base,
        String name,
        String category,
        String categoryIconUrl,
        String address,
        Double latitude,
        Double longitude,
        LocalDate day,
        LocalTime startTime,
        LocalTime endTime
) {
    public UUID id() {
        return base != null ? base.id() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DailyItineraryDto that)) return false;
        return id() != null && Objects.equals(id(), that.id());
    }

    @Override
    public int hashCode() {
        return id() != null ? Objects.hash(id()) : 0;
    }
}

