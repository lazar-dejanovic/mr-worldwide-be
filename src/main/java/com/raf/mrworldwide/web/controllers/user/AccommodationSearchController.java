package com.raf.mrworldwide.web.controllers.user;

import com.raf.mrworldwide.domain.dto.accomodation.AccommodationOfferDto;
import com.raf.mrworldwide.services.trip.AccommodationSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/accommodation")
@RequiredArgsConstructor
public class AccommodationSearchController {

    private final AccommodationSearchService accommodationSearchService;

    @GetMapping("/search")
    public ResponseEntity<List<AccommodationOfferDto>> search(
            @RequestParam String cityName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(defaultValue = "2") int adults) {
        return ResponseEntity.ok(accommodationSearchService.search(cityName, checkIn, checkOut, adults));
    }
}

