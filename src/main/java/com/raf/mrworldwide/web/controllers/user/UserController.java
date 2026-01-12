package com.raf.mrworldwide.web.controllers.user;

import com.raf.mrworldwide.domain.dto.user.ResetPasswordRequest;
import com.raf.mrworldwide.domain.dto.user.UserDto;
import com.raf.mrworldwide.domain.dto.user.UserUpdateRequest;
import com.raf.mrworldwide.security.UserLoginRequest;
import com.raf.mrworldwide.security.UserRegisterRequest;
import com.raf.mrworldwide.services.ums.AuthService;
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
    public ResponseEntity<UserDto> update(@PathVariable UUID id, @RequestBody @Valid UserUpdateRequest updateRequest) {
        return null;
    }

    @PostMapping(value = "/forgot-password", produces = MediaType.APPLICATION_JSON_VALUE)
    public void forgotPassword(@RequestParam String email) {
        // TODO - implement forgot password
    }

    @PostMapping(value = "/reset-password", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void confirmPasswordReset(@RequestBody @Valid ResetPasswordRequest request) {
        // TODO - implement reset password
    }

}
