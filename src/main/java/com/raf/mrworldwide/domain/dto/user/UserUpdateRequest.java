package com.raf.mrworldwide.domain.dto.user;

import jakarta.validation.constraints.NotEmpty;

public record UserUpdateRequest (
        @NotEmpty
        String firstName,
        @NotEmpty
        String lastName
) {}
