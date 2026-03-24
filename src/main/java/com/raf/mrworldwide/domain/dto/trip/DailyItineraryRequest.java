package com.raf.mrworldwide.domain.dto.trip;

import java.time.LocalDate;
import java.time.LocalTime;

public record DailyItineraryRequest(
        String name,
        String category,
        String categoryIconUrl,
        String address,
        Double latitude,
        Double longitude,
        LocalDate day,
        LocalTime startTime,
        LocalTime endTime
) {}

