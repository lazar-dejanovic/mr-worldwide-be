package com.raf.mrworldwide.domain.mappers;

import com.raf.mrworldwide.domain.dto.BaseEntityDto;
import com.raf.mrworldwide.domain.dto.trip.AirplaneTransportDto;
import com.raf.mrworldwide.domain.dto.trip.TransportDto;
import com.raf.mrworldwide.domain.dto.trip.VehicleTransportDto;
import com.raf.mrworldwide.domain.entities.transport.AirplaneTransport;
import com.raf.mrworldwide.domain.entities.transport.Transport;
import com.raf.mrworldwide.domain.entities.transport.VehicleTransport;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TransportMapper {

    TransportMapper INSTANCE = Mappers.getMapper(TransportMapper.class);

    @Mapping(target = "base", source = "transport")
    TransportDto toDto(Transport transport);

    @Mapping(target = "base", source = "airplaneTransport")
    AirplaneTransportDto toDto(AirplaneTransport airplaneTransport);

    @Mapping(target = "base", source = "vehicleTransport")
    VehicleTransportDto toDto(VehicleTransport vehicleTransport);

    default BaseEntityDto toBase(Transport transport) {
        if (transport == null) return null;
        return new BaseEntityDto(transport.getId(), transport.getCreatedOn(), transport.getUpdatedOn());
    }

    default BaseEntityDto toBase(AirplaneTransport at) {
        if (at == null) return null;
        return new BaseEntityDto(at.getId(), at.getCreatedOn(), at.getUpdatedOn());
    }

    default BaseEntityDto toBase(VehicleTransport vt) {
        if (vt == null) return null;
        return new BaseEntityDto(vt.getId(), vt.getCreatedOn(), vt.getUpdatedOn());
    }
}

