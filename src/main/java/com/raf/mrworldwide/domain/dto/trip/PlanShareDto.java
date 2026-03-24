package com.raf.mrworldwide.domain.dto.trip;

import com.raf.mrworldwide.domain.dto.BaseEntityDto;
import com.raf.mrworldwide.domain.dto.user.UserDto;
import com.raf.mrworldwide.domain.entities.trip.AccessType;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public record PlanShareDto(
        BaseEntityDto base,
        UserDto sharedWithUser,
        AccessType accessType,
        ZonedDateTime inviteSentAt
) {
    public UUID id() {
        return base != null ? base.id() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlanShareDto that)) return false;
        return id() != null && Objects.equals(id(), that.id());
    }

    @Override
    public int hashCode() {
        return id() != null ? Objects.hash(id()) : 0;
    }
}

