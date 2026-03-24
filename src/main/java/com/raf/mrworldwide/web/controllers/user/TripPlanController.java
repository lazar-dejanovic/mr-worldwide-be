package com.raf.mrworldwide.web.controllers.user;

import com.raf.mrworldwide.domain.dto.trip.TripPlanDetailDto;
import com.raf.mrworldwide.domain.dto.trip.TripPlanDto;
import com.raf.mrworldwide.domain.dto.trip.TripPlanRequest;
import com.raf.mrworldwide.domain.entities.trip.TripPlanStatus;
import com.raf.mrworldwide.services.trip.TripPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripPlanController {

    private final TripPlanService tripPlanService;

    @GetMapping("/{id}")
    public ResponseEntity<TripPlanDetailDto> getTripById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(tripPlanService.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<TripPlanDto>> getTripsForUser(
            @PageableDefault(size = 20, sort = "createdOn") Pageable pageable) {
        return ResponseEntity.ok(tripPlanService.getTripsForUser(pageable));
    }

    @PostMapping
    public ResponseEntity<TripPlanDto> create(@RequestBody TripPlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tripPlanService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TripPlanDto> update(@PathVariable UUID id, @RequestBody TripPlanRequest request) {
        return ResponseEntity.ok(tripPlanService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TripPlanDto> updateStatus(@PathVariable UUID id,
                                                     @RequestParam TripPlanStatus status) {
        return ResponseEntity.ok(tripPlanService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        tripPlanService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
