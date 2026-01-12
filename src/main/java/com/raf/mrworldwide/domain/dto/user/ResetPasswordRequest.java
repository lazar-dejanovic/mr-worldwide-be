package com.raf.mrworldwide.domain.dto.user;

import jakarta.validation.constraints.NotEmpty;

public record ResetPasswordRequest (
        @NotEmpty(message = "Token is required")
        String token,
        @NotEmpty(message = "New password is required")
        String newPassword,
        @NotEmpty(message = "Secret key is required")
        String secretKey
) {}
