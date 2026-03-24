package com.raf.mrworldwide.web.controllers.user;

import com.raf.mrworldwide.domain.dto.trip.RouteCalculationRequest;
import com.raf.mrworldwide.domain.dto.trip.VehicleTransportDto;
import com.raf.mrworldwide.services.trip.RouteCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteCalculationController {

    private final RouteCalculationService routeCalculationService;

    @PostMapping("/calculate")
    public ResponseEntity<VehicleTransportDto> calculate(@RequestBody RouteCalculationRequest request) {
        return ResponseEntity.ok(routeCalculationService.calculate(request));
    }
}

