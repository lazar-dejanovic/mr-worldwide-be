package com.raf.mrworldwide.domain.dto.accomodation;

import com.raf.mrworldwide.domain.dto.BaseEntityDto;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public record AccommodationDto(
        BaseEntityDto base,
        String name,
        String address,
        String imageUrl,
        String bookingUrl,
        Double starRating,
        Double reviewScore,
        LocalDate checkIn,
        LocalDate checkOut,
        Double priceTotal,
        String currency
) {
    public UUID id() {
        return base != null ? base.id() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AccommodationDto that)) return false;
        return id() != null && Objects.equals(id(), that.id());
    }

    @Override
    public int hashCode() {
        return id() != null ? Objects.hash(id()) : 0;
    }
}

