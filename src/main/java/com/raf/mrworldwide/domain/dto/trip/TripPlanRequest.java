package com.raf.mrworldwide.domain.dto.trip;

import java.time.LocalDate;
import java.util.List;

public record TripPlanRequest(
        String name,
        LocalDate startDate,
        LocalDate endDate,
        List<String> interests
) {}

