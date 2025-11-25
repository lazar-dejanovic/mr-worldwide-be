package com.raf.mrworldwide.services.ums;

import com.raf.mrworldwide.dao.repositories.UserRepository;
import com.raf.mrworldwide.domain.dto.user.UserDto;
import com.raf.mrworldwide.domain.entities.user.User;
import com.raf.mrworldwide.domain.entities.user.UserType;
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

import java.util.UUID;

@Service
@Slf4j
@AllArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final TokenAuthenticationService tokenAuthenticationService;
    private final PasswordEncoder passwordEncoder;

    public User login(String email, String password) {
        log.info("Logging in, [{}]", email);
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> userDoesNotExistException(email));

        if (UserType.SYSTEM_ADMIN.equals(user.getUserType())) {
            throw new BadRequestException("System user can't login to platform");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadRequestException("Incorrect password");
        }
        log.info("Logged in, [{}]", email);

        user.setAccessToken(tokenAuthenticationService.generateToken(user));
        return user;
    }

    public User systemAdminLogin(String email, String password) {
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> userDoesNotExistException(email));

        if (!UserType.SYSTEM_ADMIN.equals(user.getUserType())) {
            throw new BadRequestException("Only system admin can login via this route");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadRequestException("Incorrect password");
        }
        user.setAccessToken(tokenAuthenticationService.generateToken(user));
        return user;
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> userDoesNotExistException(email));
    }

    public User getUserFromToken(String token) {
        String email = getEmailFromToken(token);
        return userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> userDoesNotExistException(email));
    }

    public UserDto getUserDtoFromToken(String token, UUID companyId) {
        User user = getUserFromToken(token);
        return UserMapper.INSTANCE.toDto(user);
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

    private NotFoundException userDoesNotExistException(String email) {
        String errorMessage = "User with email " + email + " does not exist";
        return new NotFoundException(errorMessage);
    }

}
