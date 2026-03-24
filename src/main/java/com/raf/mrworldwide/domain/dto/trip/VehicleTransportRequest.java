package com.raf.mrworldwide.domain.dto.trip;

public record VehicleTransportRequest(
        Double distanceKm,
        Double estimatedFuelCost,
        Double tollCost
) {}

