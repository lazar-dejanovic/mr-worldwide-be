package com.raf.mrworldwide.domain.dto.ai;

import com.raf.mrworldwide.domain.dto.BaseEntityDto;
import com.raf.mrworldwide.domain.entities.ai.SenderType;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public record AIInteractionDto(
        BaseEntityDto base,
        String message,
        SenderType senderType,
        ZonedDateTime timestamp
) {
    public UUID id() {
        return base != null ? base.id() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AIInteractionDto that)) return false;
        return id() != null && Objects.equals(id(), that.id());
    }

    @Override
    public int hashCode() {
        return id() != null ? Objects.hash(id()) : 0;
    }
}

