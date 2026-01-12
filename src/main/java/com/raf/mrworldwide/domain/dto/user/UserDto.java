package com.raf.mrworldwide.domain.dto.user;

import com.raf.mrworldwide.domain.dto.BaseEntityDto;
import com.raf.mrworldwide.domain.entities.user.Role;
import jakarta.annotation.Nonnull;

import java.util.Objects;
import java.util.UUID;

public record UserDto(
        BaseEntityDto base,
        String firstName,
        String lastName,
        String email,
        Role role,
        String accessToken
) {

    public UUID id() {
        return base != null ? base.id() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserDto that)) return false;
        return id() != null && Objects.equals(id(), that.id());
    }

    @Override
    public int hashCode() {
        return id() != null ? Objects.hash(id()) : 0;
    }

    @Override
    @Nonnull
    public String toString() {
        return "UserDto{" +
                "email='" + email + '\'' +
                ", role=" + role +
                ", id=" + id() +
                '}';
    }

}
