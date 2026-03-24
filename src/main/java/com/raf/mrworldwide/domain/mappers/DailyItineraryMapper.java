package com.raf.mrworldwide.domain.mappers;

import com.raf.mrworldwide.domain.dto.BaseEntityDto;
import com.raf.mrworldwide.domain.dto.trip.DailyItineraryDto;
import com.raf.mrworldwide.domain.entities.trip.DailyItinerary;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DailyItineraryMapper {

    DailyItineraryMapper INSTANCE = Mappers.getMapper(DailyItineraryMapper.class);

    @Mapping(target = "base", source = "itinerary")
    DailyItineraryDto toDto(DailyItinerary itinerary);

    default BaseEntityDto toBase(DailyItinerary itinerary) {
        if (itinerary == null) return null;
        return new BaseEntityDto(itinerary.getId(), itinerary.getCreatedOn(), itinerary.getUpdatedOn());
    }
}

