package com.raf.mrworldwide.services;

import com.raf.mrworldwide.BaseServiceTest;
import com.raf.mrworldwide.dao.repositories.UserRepository;
import com.raf.mrworldwide.domain.dto.user.UserDto;
import com.raf.mrworldwide.domain.entities.user.Role;
import com.raf.mrworldwide.domain.entities.user.User;
import com.raf.mrworldwide.exceptions.BadRequestException;
import com.raf.mrworldwide.exceptions.ForbiddenException;
import com.raf.mrworldwide.exceptions.NotFoundException;
import com.raf.mrworldwide.services.ums.AuthService;
import com.raf.mrworldwide.services.ums.TokenAuthenticationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest extends BaseServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenAuthenticationService tokenAuthenticationService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    // -----------------------------------------------------------------------
    // login — success
    // -----------------------------------------------------------------------

    @Test
    void login_shouldReturnUserDtoWithToken_whenCredentialsAreValid() {
        User user = buildUser(USER_ID, USER_EMAIL, Role.REGULAR_USER);
        when(userRepository.findByEmailIgnoreCase(USER_EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", user.getPassword())).thenReturn(true);
        when(tokenAuthenticationService.generateToken(user)).thenReturn("jwt-token");

        UserDto result = authService.login(USER_EMAIL, "password123");

        assertThat(result.email()).isEqualTo(USER_EMAIL);
        assertThat(result.accessToken()).isEqualTo("jwt-token");
    }

    // -----------------------------------------------------------------------
    // login — wrong password
    // -----------------------------------------------------------------------

    @Test
    void login_shouldThrowBadRequestException_whenPasswordIsIncorrect() {
        User user = buildUser(USER_ID, USER_EMAIL, Role.REGULAR_USER);
        when(userRepository.findByEmailIgnoreCase(USER_EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", user.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(USER_EMAIL, "wrongpassword"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Incorrect password");
    }

    // -----------------------------------------------------------------------
    // login — deleted user
    // -----------------------------------------------------------------------

    @Test
    void login_shouldThrowForbiddenException_whenUserIsDeleted() {
        User user = buildUser(USER_ID, USER_EMAIL, Role.REGULAR_USER);
        user.setDeleted(true);
        when(userRepository.findByEmailIgnoreCase(USER_EMAIL)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(USER_EMAIL, "password123"))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("disabled");
    }

    // -----------------------------------------------------------------------
    // login — user not found
    // -----------------------------------------------------------------------

    @Test
    void login_shouldThrowNotFoundException_whenEmailDoesNotExist() {
        when(userRepository.findByEmailIgnoreCase("ghost@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login("ghost@example.com", "password123"))
                .isInstanceOf(NotFoundException.class);
    }

    // -----------------------------------------------------------------------
    // getUserFromToken
    // -----------------------------------------------------------------------

    @Test
    void getUserFromToken_shouldReturnUser_whenTokenIsValid() {
        User user = buildUser(USER_ID, USER_EMAIL, Role.REGULAR_USER);
        // TokenAuthenticationService is real — we must stub email extraction indirectly
        // by stubbing the repository to return the user for that email.
        when(tokenAuthenticationService.getEmailFromToken("Bearer valid-token"))
                .thenReturn(USER_EMAIL);
        when(userRepository.findByEmailIgnoreCase(USER_EMAIL)).thenReturn(Optional.of(user));

        User result = authService.getUserFromToken("Bearer valid-token");

        assertThat(result.getEmail()).isEqualTo(USER_EMAIL);
    }
}

