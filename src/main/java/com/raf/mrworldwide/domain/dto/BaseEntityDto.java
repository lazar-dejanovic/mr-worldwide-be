package com.raf.mrworldwide.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;
import java.util.UUID;

public record BaseEntityDto(
        UUID id,

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        ZonedDateTime createdOn,

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        ZonedDateTime updatedOn
) {
}
