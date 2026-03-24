package com.raf.mrworldwide.services.ums;

import com.raf.mrworldwide.dao.repositories.UserTripPreferenceRepository;
import com.raf.mrworldwide.domain.dto.user.UserTripPreferenceDto;
import com.raf.mrworldwide.domain.dto.user.UserTripPreferenceRequest;
import com.raf.mrworldwide.domain.entities.user.User;
import com.raf.mrworldwide.domain.entities.user.UserTripPreference;
import com.raf.mrworldwide.domain.mappers.UserTripPreferenceMapper;
import com.raf.mrworldwide.exceptions.NotFoundException;
import com.raf.mrworldwide.utils.AuthUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class UserTripPreferenceService {

    private final UserTripPreferenceRepository userTripPreferenceRepository;
    private final UserService userService;

    public UserTripPreferenceDto get() {
        User user = userService.getEntityById(AuthUtils.getLoggedUserId());
        if (user.getUserTripPreference() == null) {
            throw new NotFoundException("Trip preferences not found for this user");
        }
        return UserTripPreferenceMapper.INSTANCE.toDto(user.getUserTripPreference());
    }

    @Transactional
    public UserTripPreferenceDto createOrUpdate(UserTripPreferenceRequest request) {
        User user = userService.getEntityById(AuthUtils.getLoggedUserId());
        UserTripPreference preference = user.getUserTripPreference();

        if (preference == null) {
            preference = new UserTripPreference();
        }

        preference.setName(request.name());
        preference.setInterests(request.interests());
        preference.setHobbies(request.hobbies());
        preference.setFavouriteDestinations(request.favouriteDestinations());

        preference = userTripPreferenceRepository.save(preference);
        userService.linkTripPreference(user, preference);

        return UserTripPreferenceMapper.INSTANCE.toDto(preference);
    }
}

