package com.raf.mrworldwide.web.controllers.user;

import com.raf.mrworldwide.domain.dto.trip.SegmentOrderItem;
import com.raf.mrworldwide.domain.dto.trip.TripSegmentDetailDto;
import com.raf.mrworldwide.domain.dto.trip.TripSegmentDto;
import com.raf.mrworldwide.domain.dto.trip.TripSegmentRequest;
import com.raf.mrworldwide.services.trip.TripSegmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/trips/{tripId}/segments")
@RequiredArgsConstructor
public class TripSegmentController {

    private final TripSegmentService tripSegmentService;

    @GetMapping
    public ResponseEntity<List<TripSegmentDto>> getAll(@PathVariable UUID tripId) {
        return ResponseEntity.ok(tripSegmentService.getAllForTrip(tripId));
    }

    @GetMapping("/{segmentId}")
    public ResponseEntity<TripSegmentDetailDto> getById(@PathVariable UUID tripId,
                                                         @PathVariable UUID segmentId) {
        return ResponseEntity.ok(tripSegmentService.getById(tripId, segmentId));
    }

    @PostMapping
    public ResponseEntity<TripSegmentDto> create(@PathVariable UUID tripId,
                                                  @RequestBody TripSegmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tripSegmentService.create(tripId, request));
    }

    @PutMapping("/{segmentId}")
    public ResponseEntity<TripSegmentDto> update(@PathVariable UUID tripId,
                                                  @PathVariable UUID segmentId,
                                                  @RequestBody TripSegmentRequest request) {
        return ResponseEntity.ok(tripSegmentService.update(tripId, segmentId, request));
    }

    @DeleteMapping("/{segmentId}")
    public ResponseEntity<Void> delete(@PathVariable UUID tripId, @PathVariable UUID segmentId) {
        tripSegmentService.delete(tripId, segmentId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/reorder")
    public ResponseEntity<List<TripSegmentDto>> reorder(@PathVariable UUID tripId,
                                                         @RequestBody List<SegmentOrderItem> orderItems) {
        return ResponseEntity.ok(tripSegmentService.reorder(tripId, orderItems));
    }
}

