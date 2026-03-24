package com.raf.mrworldwide.domain.dto.trip;

import java.time.LocalDateTime;

public record AirplaneTransportRequest(
        String flightNumber,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime,
        String duration,
        Double price,
        String currency
) {}

