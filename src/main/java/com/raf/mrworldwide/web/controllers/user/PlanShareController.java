package com.raf.mrworldwide.web.controllers.user;

import com.raf.mrworldwide.domain.dto.trip.PlanShareDto;
import com.raf.mrworldwide.domain.dto.trip.PlanShareRequest;
import com.raf.mrworldwide.domain.dto.trip.TripPlanDto;
import com.raf.mrworldwide.services.trip.PlanShareService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PlanShareController {

    private final PlanShareService planShareService;

    @PostMapping("/api/trips/{tripId}/share")
    public ResponseEntity<PlanShareDto> share(@PathVariable UUID tripId,
                                               @RequestBody PlanShareRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(planShareService.share(tripId, request));
    }

    @GetMapping("/api/trips/{tripId}/share")
    public ResponseEntity<List<PlanShareDto>> listShares(@PathVariable UUID tripId) {
        return ResponseEntity.ok(planShareService.listShares(tripId));
    }

    @DeleteMapping("/api/trips/{tripId}/share/{shareId}")
    public ResponseEntity<Void> revoke(@PathVariable UUID tripId, @PathVariable UUID shareId) {
        planShareService.revoke(tripId, shareId);
        return ResponseEntity.noContent().build();
    }

    // Declared before /{id} to avoid path conflict — lives outside the tripId sub-path
    @GetMapping("/api/trips/shared-with-me")
    public ResponseEntity<Page<TripPlanDto>> sharedWithMe(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(planShareService.getSharedWithMe(pageable));
    }
}

