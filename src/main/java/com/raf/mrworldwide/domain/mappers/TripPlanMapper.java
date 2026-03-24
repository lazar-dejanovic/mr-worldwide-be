package com.raf.mrworldwide.domain.mappers;

import com.raf.mrworldwide.domain.dto.BaseEntityDto;
import com.raf.mrworldwide.domain.dto.trip.TripPlanDetailDto;
import com.raf.mrworldwide.domain.dto.trip.TripPlanDto;
import com.raf.mrworldwide.domain.entities.trip.TripPlan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {TripSegmentMapper.class})
public interface TripPlanMapper {

    TripPlanMapper INSTANCE = Mappers.getMapper(TripPlanMapper.class);

    @Mapping(target = "base", source = "tripPlan")
    TripPlanDto toDto(TripPlan tripPlan);

    @Mapping(target = "base", source = "tripPlan")
    TripPlanDetailDto toDetailDto(TripPlan tripPlan);

    default BaseEntityDto toBase(TripPlan tripPlan) {
        if (tripPlan == null) return null;
        return new BaseEntityDto(
                tripPlan.getId(),
                tripPlan.getCreatedOn(),
                tripPlan.getUpdatedOn()
        );
    }
}
