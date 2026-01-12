package com.raf.mrworldwide.services.ums;

import com.raf.mrworldwide.dao.repositories.UserRepository;
import com.raf.mrworldwide.domain.dto.user.UserDto;
import com.raf.mrworldwide.domain.entities.user.Role;
import com.raf.mrworldwide.domain.entities.user.User;
import com.raf.mrworldwide.domain.mappers.UserMapper;
import com.raf.mrworldwide.exceptions.NotFoundException;
import com.raf.mrworldwide.security.UserRegisterRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDto getById(UUID id) {
        return UserMapper.INSTANCE.toDto(getEntityById(id));
    }

    @Transactional
    public UserDto register(UserRegisterRequest request) {
        User user = UserMapper.INSTANCE.fromRegisterRequest(request);
        user.setRole(Role.REGULAR_USER);
        user.setPassword(passwordEncoder.encode(request.password()));

        return UserMapper.INSTANCE.toDto(userRepository.save(user));
    }

    public User getEntityById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User with id " + id + " does not exist"));
    }
}
