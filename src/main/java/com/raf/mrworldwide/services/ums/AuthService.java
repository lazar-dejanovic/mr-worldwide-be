package com.raf.mrworldwide.services.ums;

import com.raf.mrworldwide.dao.repositories.UserRepository;
import com.raf.mrworldwide.domain.dto.user.UserDto;
import com.raf.mrworldwide.domain.entities.user.User;
import com.raf.mrworldwide.domain.mappers.UserMapper;
import com.raf.mrworldwide.exceptions.AuthorizationException;
import com.raf.mrworldwide.exceptions.BadRequestException;
import com.raf.mrworldwide.exceptions.ForbiddenException;
import com.raf.mrworldwide.exceptions.NotFoundException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@AllArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final TokenAuthenticationService tokenAuthenticationService;
    private final PasswordEncoder passwordEncoder;

    public UserDto login(String email, String password) {
        log.info("Logging in, [{}]", email);
        User user = getUserByEmail(email);

        if (user.getDeleted()) throw new ForbiddenException("User is disabled!");
        if (!passwordEncoder.matches(password, user.getPassword())) throw new BadRequestException("Incorrect password");

        log.info("Logged in, [{}]", email);
        return UserMapper.INSTANCE.toDto(user, tokenAuthenticationService.generateToken(user));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("User with email " + email + " does not exist"));
    }

    public User getUserFromToken(String token) {
        String email = getEmailFromToken(token);
        return getUserByEmail(email);
    }

    public UserDto getUserDtoFromToken(String token) {
        User user = getUserFromToken(token);
        return UserMapper.INSTANCE.toDto(user);
    }

    public void resetPassword(String email, String newPassword) {
        User user = getUserByEmail(email);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private String getEmailFromToken(String token) {
        if (token == null) {
            throw new ForbiddenException("Token not provided");
        }

        try {
            return tokenAuthenticationService.getEmailFromToken(token);
        } catch (ExpiredJwtException e) {
            throw new AuthorizationException("Login has expired. Please login again.");
        } catch (UnsupportedJwtException e) {
            throw new AuthorizationException("Unsupported token format");
        } catch (MalformedJwtException e) {
            throw new AuthorizationException("Malformed token");
        } catch (Exception e) {
            throw new AuthorizationException(e.getMessage());
        }
    }

}
