package com.raf.mrworldwide.web.controllers.user;

import com.raf.mrworldwide.domain.dto.user.ResetPasswordRequest;
import com.raf.mrworldwide.domain.dto.user.UserDto;
import com.raf.mrworldwide.domain.dto.user.UserUpdateRequest;
import com.raf.mrworldwide.security.UserLoginRequest;
import com.raf.mrworldwide.security.UserRegisterRequest;
import com.raf.mrworldwide.services.ums.AuthService;
import com.raf.mrworldwide.services.ums.ResetPasswordService;
import com.raf.mrworldwide.services.ums.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;
    private final UserService userService;
    private final ResetPasswordService resetPasswordService;

    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDto> me(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(authService.getUserDtoFromToken(token));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@RequestBody UserLoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest.email(), loginRequest.password()));
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@RequestBody @Valid UserRegisterRequest registerRequest) {
        return ResponseEntity.ok(userService.register(registerRequest));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> update(@PathVariable UUID id,
                                           @RequestBody @Valid UserUpdateRequest updateRequest) {
        return ResponseEntity.ok(userService.update(id, updateRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        userService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/forgot-password", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> forgotPassword(@RequestParam String email) {
        resetPasswordService.forgotPassword(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/reset-password", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> confirmPasswordReset(@RequestBody @Valid ResetPasswordRequest request) {
        resetPasswordService.resetPassword(request);
        return ResponseEntity.ok().build();
    }
}
