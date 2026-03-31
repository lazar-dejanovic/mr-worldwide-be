package com.raf.mrworldwide.domain.dto.user;

import jakarta.validation.constraints.NotEmpty;

import java.util.UUID;

public record ResetPasswordRequest (
        @NotEmpty(message = "Token is required")
        UUID token,
        @NotEmpty(message = "New password is required")
        String newPassword,
        @NotEmpty(message = "Secret key is required")
        String secretKey
) {}
