package com.raf.mrworldwide.web.controllers.user;

import com.raf.mrworldwide.domain.dto.trip.TransportDto;
import com.raf.mrworldwide.domain.dto.trip.TransportRequest;
import com.raf.mrworldwide.services.trip.TransportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/trips/{tripId}/segments/{segmentId}/transport")
@RequiredArgsConstructor
public class TransportController {

    private final TransportService transportService;

    @GetMapping
    public ResponseEntity<TransportDto> get(@PathVariable UUID tripId, @PathVariable UUID segmentId) {
        return ResponseEntity.ok(transportService.get(tripId, segmentId));
    }

    @PostMapping
    public ResponseEntity<TransportDto> create(@PathVariable UUID tripId,
                                                @PathVariable UUID segmentId,
                                                @RequestBody TransportRequest request) {
        return ResponseEntity.ok(transportService.createOrUpdate(tripId, segmentId, request));
    }

    @PutMapping
    public ResponseEntity<TransportDto> update(@PathVariable UUID tripId,
                                                @PathVariable UUID segmentId,
                                                @RequestBody TransportRequest request) {
        return ResponseEntity.ok(transportService.createOrUpdate(tripId, segmentId, request));
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@PathVariable UUID tripId, @PathVariable UUID segmentId) {
        transportService.delete(tripId, segmentId);
        return ResponseEntity.noContent().build();
    }
}

