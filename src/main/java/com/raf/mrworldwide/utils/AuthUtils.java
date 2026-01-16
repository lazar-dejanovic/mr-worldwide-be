package com.raf.mrworldwide.utils;

import com.raf.mrworldwide.domain.entities.user.Role;
import com.raf.mrworldwide.domain.entities.user.User;
import com.raf.mrworldwide.exceptions.AuthorizationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public class AuthUtils {

    public static User getLoggedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return (User) authentication.getPrincipal();
        }

        throw new AuthorizationException("Not logged in!");
    }

    public static UUID getLoggedUserId() {
        return getLoggedUser().getId();
    }

    public static Role getLoggedUserRole() {
        return getLoggedUser().getRole();
    }
}
