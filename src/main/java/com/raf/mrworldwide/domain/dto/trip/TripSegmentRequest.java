package com.raf.mrworldwide.domain.dto.trip;

import java.time.LocalDate;

public record TripSegmentRequest(
        String departure,
        String destination,
        LocalDate arrivalDate,
        LocalDate departureDate,
        Double destinationLatitude,
        Double destinationLongitude
) {}

