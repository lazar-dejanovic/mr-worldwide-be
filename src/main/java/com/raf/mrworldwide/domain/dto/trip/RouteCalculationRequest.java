package com.raf.mrworldwide.domain.dto.trip;

public record RouteCalculationRequest(
        String originCity,
        String destinationCity
) {}

