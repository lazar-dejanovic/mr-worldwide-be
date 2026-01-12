package com.raf.mrworldwide.security;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public record UserLoginRequest (
        @NotEmpty( message = "Email is required")
        String email,
        @NotEmpty( message = "Password is required")
        String password
) {}
