package com.raf.mrworldwide.web.controllers.user;

import com.raf.mrworldwide.domain.dto.accomodation.AccommodationDto;
import com.raf.mrworldwide.domain.dto.accomodation.AccommodationRequest;
import com.raf.mrworldwide.services.trip.AccommodationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/trips/{tripId}/segments/{segmentId}/accommodation")
@RequiredArgsConstructor
public class AccommodationController {

    private final AccommodationService accommodationService;

    @GetMapping
    public ResponseEntity<AccommodationDto> get(@PathVariable UUID tripId, @PathVariable UUID segmentId) {
        return ResponseEntity.ok(accommodationService.get(tripId, segmentId));
    }

    @PostMapping
    public ResponseEntity<AccommodationDto> create(@PathVariable UUID tripId,
                                                    @PathVariable UUID segmentId,
                                                    @RequestBody AccommodationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accommodationService.create(tripId, segmentId, request));
    }

    @PutMapping
    public ResponseEntity<AccommodationDto> update(@PathVariable UUID tripId,
                                                    @PathVariable UUID segmentId,
                                                    @RequestBody AccommodationRequest request) {
        return ResponseEntity.ok(accommodationService.update(tripId, segmentId, request));
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@PathVariable UUID tripId, @PathVariable UUID segmentId) {
        accommodationService.delete(tripId, segmentId);
        return ResponseEntity.noContent().build();
    }
}

