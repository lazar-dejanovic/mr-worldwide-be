package com.raf.mrworldwide.domain.mappers;

import com.raf.mrworldwide.domain.dto.BaseEntityDto;
import com.raf.mrworldwide.domain.dto.trip.PlanShareDto;
import com.raf.mrworldwide.domain.entities.trip.PlanShare;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {UserMapper.class})
public interface PlanShareMapper {

    PlanShareMapper INSTANCE = Mappers.getMapper(PlanShareMapper.class);

    @Mapping(target = "base", source = "planShare")
    PlanShareDto toDto(PlanShare planShare);

    default BaseEntityDto toBase(PlanShare planShare) {
        if (planShare == null) return null;
        return new BaseEntityDto(planShare.getId(), planShare.getCreatedOn(), planShare.getUpdatedOn());
    }
}

