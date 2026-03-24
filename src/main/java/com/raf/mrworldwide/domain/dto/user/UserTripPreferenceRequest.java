package com.raf.mrworldwide.domain.dto.user;

import java.util.List;

public record UserTripPreferenceRequest(
        String name,
        List<String> interests,
        List<String> hobbies,
        List<String> favouriteDestinations
) {}

