package com.raf.mrworldwide.web.controllers.user;

import com.raf.mrworldwide.domain.dto.trip.TripPlanDto;
import com.raf.mrworldwide.services.trip.TripPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripPlanController {

    private final TripPlanService tripPlanService;

    @GetMapping("/{id}")
    public ResponseEntity<TripPlanDto> getTripById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(tripPlanService.getById(id));
    }

    @GetMapping()
    public ResponseEntity<List<TripPlanDto>> getTripsForUser() {
        return ResponseEntity.ok(tripPlanService.getTripsForUser());
    }
}
