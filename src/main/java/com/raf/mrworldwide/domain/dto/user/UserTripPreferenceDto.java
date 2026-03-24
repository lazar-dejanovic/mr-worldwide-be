package com.raf.mrworldwide.domain.dto.user;

import com.raf.mrworldwide.domain.dto.BaseEntityDto;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record UserTripPreferenceDto(
        BaseEntityDto base,
        String name,
        List<String> interests,
        List<String> hobbies,
        List<String> favouriteDestinations
) {
    public UUID id() {
        return base != null ? base.id() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserTripPreferenceDto that)) return false;
        return id() != null && Objects.equals(id(), that.id());
    }

    @Override
    public int hashCode() {
        return id() != null ? Objects.hash(id()) : 0;
    }
}

