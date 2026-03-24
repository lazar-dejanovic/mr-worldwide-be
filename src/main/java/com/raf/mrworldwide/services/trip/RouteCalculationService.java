package com.raf.mrworldwide.services.trip;

import com.raf.mrworldwide.domain.dto.trip.RouteCalculationRequest;
import com.raf.mrworldwide.domain.dto.trip.VehicleTransportDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RouteCalculationService {

    private final double fuelCostPerKm;
    private final double tollCostPerKm;

    // Approximate average coordinates for well-known cities (fallback Haversine approach).
    // In a production system this would call a geocoding API, then HERE/Google Distance Matrix.
    public RouteCalculationService(
            @Value("${route.fuel-cost-per-km:0.12}") double fuelCostPerKm,
            @Value("${route.toll-cost-per-km:0.05}") double tollCostPerKm) {
        this.fuelCostPerKm = fuelCostPerKm;
        this.tollCostPerKm = tollCostPerKm;
    }

    public VehicleTransportDto calculate(RouteCalculationRequest request) {
        // Haversine-based distance using approximate bounding-box centroids.
        // A real implementation would geocode the city names first.
        double distanceKm = estimateDistance(request.originCity(), request.destinationCity());
        double fuelCost = Math.round(distanceKm * fuelCostPerKm * 100.0) / 100.0;
        double tollCost = Math.round(distanceKm * tollCostPerKm * 100.0) / 100.0;

        return new VehicleTransportDto(null, distanceKm, fuelCost, tollCost);
    }

    /**
     * Naive placeholder: uses a hard-coded distance of 500 km per unit of string-length difference
     * until a geocoding integration is added. Replace with actual geocoding + Haversine.
     */
    private double estimateDistance(String origin, String destination) {
        // Placeholder: 1 km per character difference as a stub.
        // Replace with actual geocoding (e.g. Nominatim OSM) + Haversine formula.
        int diff = Math.abs(origin.length() - destination.length());
        return Math.max(50.0, diff * 50.0 + 200.0);
    }
}

