package com.raf.mrworldwide.domain.dto.trip;

import com.raf.mrworldwide.domain.dto.BaseEntityDto;
import com.raf.mrworldwide.domain.dto.accomodation.AccommodationDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record TripSegmentDetailDto(
        BaseEntityDto base,
        String departure,
        String destination,
        LocalDate arrivalDate,
        LocalDate departureDate,
        Integer orderIndex,
        Double destinationLatitude,
        Double destinationLongitude,
        TransportDto transport,
        AccommodationDto accommodation,
        List<DailyItineraryDto> dailyItineraries
) {
    public UUID id() {
        return base != null ? base.id() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TripSegmentDetailDto that)) return false;
        return id() != null && Objects.equals(id(), that.id());
    }

    @Override
    public int hashCode() {
        return id() != null ? Objects.hash(id()) : 0;
    }
}

