package com.raf.mrworldwide.services;

import com.raf.mrworldwide.BaseServiceTest;
import com.raf.mrworldwide.dao.repositories.UserRepository;
import com.raf.mrworldwide.domain.dto.user.UserDto;
import com.raf.mrworldwide.domain.dto.user.UserUpdateRequest;
import com.raf.mrworldwide.domain.entities.user.Role;
import com.raf.mrworldwide.domain.entities.user.User;
import com.raf.mrworldwide.exceptions.ForbiddenException;
import com.raf.mrworldwide.exceptions.NotFoundException;
import com.raf.mrworldwide.security.UserRegisterRequest;
import com.raf.mrworldwide.services.ums.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest extends BaseServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    // -----------------------------------------------------------------------
    // register
    // -----------------------------------------------------------------------

    @Test
    void register_shouldSaveUserWithRegularRoleAndEncodedPassword() {
        UserRegisterRequest request = new UserRegisterRequest(
                "Alice", "Smith", "alice@example.com", "password123");

        User savedUser = User.builder()
                .email(request.email())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .password("encodedPassword")
                .role(Role.REGULAR_USER)
                .build();
        ReflectionTestUtils.setField(savedUser, "id", UUID.randomUUID());

        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserDto result = userService.register(request);

        assertThat(result.email()).isEqualTo("alice@example.com");
        assertThat(result.role()).isEqualTo(Role.REGULAR_USER);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(Role.REGULAR_USER);
        assertThat(captor.getValue().getPassword()).isEqualTo("encodedPassword");
    }

    // -----------------------------------------------------------------------
    // update
    // -----------------------------------------------------------------------

    @Test
    void update_shouldApplyChanges_whenUserUpdatesOwnProfile() {
        UUID targetId = USER_ID;
        User target = buildUser(targetId, USER_EMAIL, Role.REGULAR_USER);
        when(userRepository.findById(targetId)).thenReturn(Optional.of(target));
        when(userRepository.save(any(User.class))).thenReturn(target);

        UserUpdateRequest request = new UserUpdateRequest("NewFirst", "NewLast");

        UserDto result = userService.update(targetId, request);

        assertThat(result.firstName()).isEqualTo("NewFirst");
        assertThat(result.lastName()).isEqualTo("NewLast");
        verify(userRepository).save(target);
    }

    @Test
    void update_shouldThrowForbiddenException_whenUserUpdatesAnotherProfile() {
        UUID otherId = UUID.randomUUID();
        User otherUser = buildUser(otherId, "other@example.com", Role.REGULAR_USER);
        when(userRepository.findById(otherId)).thenReturn(Optional.of(otherUser));

        UserUpdateRequest request = new UserUpdateRequest("X", "Y");

        assertThatThrownBy(() -> userService.update(otherId, request))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("not allowed");

        verify(userRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // softDelete
    // -----------------------------------------------------------------------

    @Test
    void softDelete_shouldMarkAsDeleted_whenUserDeletesOwnAccount() {
        User target = buildUser(USER_ID, USER_EMAIL, Role.REGULAR_USER);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(target));
        when(userRepository.save(any(User.class))).thenReturn(target);

        userService.softDelete(USER_ID);

        assertThat(target.getDeleted()).isTrue();
        verify(userRepository).save(target);
    }

    @Test
    void softDelete_shouldMarkAsDeleted_whenAdminDeletesAnotherAccount() {
        // Override the logged-in user with an admin
        User admin = buildUser(USER_ID, USER_EMAIL, Role.SYSTEM_ADMIN);
        setLoggedUser(admin);

        UUID victimId = UUID.randomUUID();
        User victim = buildUser(victimId, "victim@example.com", Role.REGULAR_USER);
        when(userRepository.findById(victimId)).thenReturn(Optional.of(victim));
        when(userRepository.save(any(User.class))).thenReturn(victim);

        userService.softDelete(victimId);

        assertThat(victim.getDeleted()).isTrue();
        verify(userRepository).save(victim);
    }

    @Test
    void softDelete_shouldThrowForbiddenException_whenRegularUserDeletesAnotherAccount() {
        UUID otherId = UUID.randomUUID();
        User otherUser = buildUser(otherId, "other@example.com", Role.REGULAR_USER);
        when(userRepository.findById(otherId)).thenReturn(Optional.of(otherUser));

        assertThatThrownBy(() -> userService.softDelete(otherId))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("not allowed");

        verify(userRepository, never()).save(any());
    }

    @Test
    void getById_shouldThrowNotFoundException_whenUserDoesNotExist() {
        UUID unknownId = UUID.randomUUID();
        when(userRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(unknownId))
                .isInstanceOf(NotFoundException.class);
    }

    // -----------------------------------------------------------------------
    // Helper — swap the security context principal mid-test
    // -----------------------------------------------------------------------

    private void setLoggedUser(User user) {
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        user, null, user.getAuthorities());
        org.springframework.security.core.context.SecurityContextHolder
                .getContext().setAuthentication(auth);
        this.loggedUser = user;
    }
}

