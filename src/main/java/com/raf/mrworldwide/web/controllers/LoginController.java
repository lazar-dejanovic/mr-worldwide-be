package com.raf.mrworldwide.web.controllers;

import com.raf.mrworldwide.domain.entities.user.User;
import com.raf.mrworldwide.exceptions.ForbiddenException;
import com.raf.mrworldwide.security.LoginRequest;
import com.raf.mrworldwide.services.ums.AuthService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Hidden
@RequestMapping("/api/login")
@RequiredArgsConstructor
public class LoginController {

    private final AuthService authService;

    @PostMapping
    public User login(@RequestBody LoginRequest loginRequest, HttpServletRequest httpRequest) {
        User user = authService.login(loginRequest.getUsername(), loginRequest.getPassword());
        if (user.getDeleted()) throw new ForbiddenException("User is disabled and cannot login!");
        return user;
    }

}
