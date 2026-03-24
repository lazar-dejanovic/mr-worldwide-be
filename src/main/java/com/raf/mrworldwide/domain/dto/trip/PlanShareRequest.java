package com.raf.mrworldwide.domain.dto.trip;

import com.raf.mrworldwide.domain.entities.trip.AccessType;

public record PlanShareRequest(
        String email,
        AccessType accessType
) {}

