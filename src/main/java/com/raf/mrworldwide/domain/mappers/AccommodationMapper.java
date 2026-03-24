package com.raf.mrworldwide.domain.mappers;

import com.raf.mrworldwide.domain.dto.BaseEntityDto;
import com.raf.mrworldwide.domain.dto.accomodation.AccommodationDto;
import com.raf.mrworldwide.domain.entities.accomodation.Accommodation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AccommodationMapper {

    AccommodationMapper INSTANCE = Mappers.getMapper(AccommodationMapper.class);

    @Mapping(target = "base", source = "accommodation")
    AccommodationDto toDto(Accommodation accommodation);

    default BaseEntityDto toBase(Accommodation accommodation) {
        if (accommodation == null) return null;
        return new BaseEntityDto(accommodation.getId(), accommodation.getCreatedOn(), accommodation.getUpdatedOn());
    }
}

