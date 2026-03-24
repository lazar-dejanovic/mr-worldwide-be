package com.raf.mrworldwide.domain.mappers;

import com.raf.mrworldwide.domain.dto.BaseEntityDto;
import com.raf.mrworldwide.domain.dto.trip.TripSegmentDetailDto;
import com.raf.mrworldwide.domain.dto.trip.TripSegmentDto;
import com.raf.mrworldwide.domain.entities.trip.TripSegment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {TransportMapper.class, AccommodationMapper.class, DailyItineraryMapper.class})
public interface TripSegmentMapper {

    TripSegmentMapper INSTANCE = Mappers.getMapper(TripSegmentMapper.class);

    @Mapping(target = "base", source = "segment")
    TripSegmentDto toDto(TripSegment segment);

    @Mapping(target = "base", source = "segment")
    TripSegmentDetailDto toDetailDto(TripSegment segment);

    default BaseEntityDto toBase(TripSegment segment) {
        if (segment == null) return null;
        return new BaseEntityDto(segment.getId(), segment.getCreatedOn(), segment.getUpdatedOn());
    }
}

