package com.raf.mrworldwide.domain.mappers;

import com.raf.mrworldwide.domain.dto.BaseEntityDto;
import com.raf.mrworldwide.domain.dto.user.UserTripPreferenceDto;
import com.raf.mrworldwide.domain.entities.user.UserTripPreference;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserTripPreferenceMapper {

    UserTripPreferenceMapper INSTANCE = Mappers.getMapper(UserTripPreferenceMapper.class);

    @Mapping(target = "base", source = "preference")
    UserTripPreferenceDto toDto(UserTripPreference preference);

    default BaseEntityDto toBase(UserTripPreference preference) {
        if (preference == null) return null;
        return new BaseEntityDto(preference.getId(), preference.getCreatedOn(), preference.getUpdatedOn());
    }
}

