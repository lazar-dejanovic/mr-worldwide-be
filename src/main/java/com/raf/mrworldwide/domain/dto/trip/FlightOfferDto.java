package com.raf.mrworldwide.domain.dto.trip;

import java.time.LocalDateTime;

public record FlightOfferDto(
        String flightNumber,
        String carrier,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime,
        String duration,
        Double price,
        String currency,
        Integer stops,
        String bookingUrl
) {}

