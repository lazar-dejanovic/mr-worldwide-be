package com.raf.mrworldwide.web.controllers.user;

import com.raf.mrworldwide.domain.dto.trip.DailyItineraryDto;
import com.raf.mrworldwide.domain.dto.trip.DailyItineraryRequest;
import com.raf.mrworldwide.domain.entities.trip.TripSegment;
import com.raf.mrworldwide.clients.FoursquareClient;
import com.raf.mrworldwide.services.trip.DailyItineraryService;
import com.raf.mrworldwide.services.trip.TripSegmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/trips/{tripId}/segments/{segmentId}/itineraries")
@RequiredArgsConstructor
public class DailyItineraryController {

    private final DailyItineraryService dailyItineraryService;
    private final TripSegmentService tripSegmentService;
    private final FoursquareClient foursquareClient;

    @GetMapping
    public ResponseEntity<Page<DailyItineraryDto>> getAll(
            @PathVariable UUID tripId,
            @PathVariable UUID segmentId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(dailyItineraryService.getAllForSegment(tripId, segmentId, pageable));
    }

    @GetMapping("/{itineraryId}")
    public ResponseEntity<DailyItineraryDto> getById(@PathVariable UUID tripId,
                                                      @PathVariable UUID segmentId,
                                                      @PathVariable UUID itineraryId) {
        return ResponseEntity.ok(dailyItineraryService.getById(tripId, segmentId, itineraryId));
    }

    @PostMapping
    public ResponseEntity<DailyItineraryDto> create(@PathVariable UUID tripId,
                                                     @PathVariable UUID segmentId,
                                                     @RequestBody DailyItineraryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(dailyItineraryService.create(tripId, segmentId, request));
    }

    @PutMapping("/{itineraryId}")
    public ResponseEntity<DailyItineraryDto> update(@PathVariable UUID tripId,
                                                     @PathVariable UUID segmentId,
                                                     @PathVariable UUID itineraryId,
                                                     @RequestBody DailyItineraryRequest request) {
        return ResponseEntity.ok(dailyItineraryService.update(tripId, segmentId, itineraryId, request));
    }

    @DeleteMapping("/{itineraryId}")
    public ResponseEntity<Void> delete(@PathVariable UUID tripId,
                                        @PathVariable UUID segmentId,
                                        @PathVariable UUID itineraryId) {
        dailyItineraryService.delete(tripId, segmentId, itineraryId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/suggest")
    public ResponseEntity<List<DailyItineraryDto>> suggest(
            @PathVariable UUID tripId,
            @PathVariable UUID segmentId,
            @RequestParam(required = false) String categories) {
        TripSegment segment = tripSegmentService.getEntityById(segmentId);
        List<String> categoryList = (categories != null && !categories.isBlank())
                ? Arrays.asList(categories.split(","))
                : List.of("MUSEUM", "PARK", "FOOD");
        return ResponseEntity.ok(foursquareClient.searchPlaces(
                segment.getDestinationLatitude(),
                segment.getDestinationLongitude(),
                categoryList, 5000));
    }
}

