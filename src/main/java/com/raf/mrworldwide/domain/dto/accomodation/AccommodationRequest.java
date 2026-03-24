package com.raf.mrworldwide.domain.dto.accomodation;

import java.time.LocalDate;

public record AccommodationRequest(
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
) {}

