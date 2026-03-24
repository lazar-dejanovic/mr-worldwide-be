package com.raf.mrworldwide.services.ums;

import com.raf.mrworldwide.dao.repositories.UserRepository;
import com.raf.mrworldwide.domain.dto.user.UserDto;
import com.raf.mrworldwide.domain.dto.user.UserUpdateRequest;
import com.raf.mrworldwide.domain.entities.user.Role;
import com.raf.mrworldwide.domain.entities.user.User;
import com.raf.mrworldwide.domain.entities.user.UserTripPreference;
import com.raf.mrworldwide.domain.mappers.UserMapper;
import com.raf.mrworldwide.exceptions.ForbiddenException;
import com.raf.mrworldwide.exceptions.NotFoundException;
import com.raf.mrworldwide.security.UserRegisterRequest;
import com.raf.mrworldwide.utils.AuthUtils;
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

    @Transactional
    public UserDto update(UUID id, UserUpdateRequest request) {
        User loggedUser = AuthUtils.getLoggedUser();
        User user = getEntityById(id);

        if (!loggedUser.getEmail().equals(user.getEmail())) {
            throw new ForbiddenException("You are not allowed to update this user");
        }

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());

        return UserMapper.INSTANCE.toDto(userRepository.save(user));
    }

    @Transactional
    public void softDelete(UUID id) {
        User loggedUser = AuthUtils.getLoggedUser();
        User user = getEntityById(id);

        boolean isSelf = loggedUser.getEmail().equals(user.getEmail());
        boolean isAdmin = loggedUser.getRole() == Role.SYSTEM_ADMIN || loggedUser.getRole() == Role.SUPER_ADMIN;

        if (!isSelf && !isAdmin) {
            throw new ForbiddenException("You are not allowed to delete this user");
        }

        user.setDeleted(true);
        userRepository.save(user);
    }

    @Transactional
    public void linkTripPreference(User user, UserTripPreference preference) {
        User managed = getEntityById(user.getId());
        managed.setUserTripPreference(preference);
        userRepository.save(managed);
    }

    public User getEntityById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User with id " + id + " does not exist"));
    }
}
