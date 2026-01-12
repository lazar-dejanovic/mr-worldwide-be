package com.raf.mrworldwide.domain.mappers;

import com.raf.mrworldwide.domain.dto.BaseEntityDto;
import com.raf.mrworldwide.domain.dto.user.UserDto;
import com.raf.mrworldwide.domain.entities.user.User;
import com.raf.mrworldwide.security.UserRegisterRequest;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserDto toDto(User user);

    @Mapping(target = "base", source = "user")
    @Mapping(target = "accessToken", source = "token")
    UserDto toDto(User user, String token);

    User fromRegisterRequest(UserRegisterRequest request);

    default BaseEntityDto toBase(User user) {
        if (user == null) return null;
        return new BaseEntityDto(
                user.getId(),
                user.getCreatedOn(),
                user.getUpdatedOn()
        );
    }

}
