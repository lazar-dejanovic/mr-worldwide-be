package com.raf.mrworldwide.security;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record UserRegisterRequest (
        @NotEmpty(message = "First name is required")
        String firstName,

        @NotEmpty(message = "Last name is required")
        String lastName,

        @Email(message = "Email must be in valid format [user@email.com]")
        String email,

        @Size(min = 8, message = "Password must be at least 8 characters long")
        String password
) {}
