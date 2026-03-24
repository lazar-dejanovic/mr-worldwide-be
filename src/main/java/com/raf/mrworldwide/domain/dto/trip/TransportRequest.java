package com.raf.mrworldwide.domain.dto.trip;

import com.raf.mrworldwide.domain.entities.transport.TransportType;

public record TransportRequest(
        TransportType transportType,
        AirplaneTransportRequest airplaneTransport,
        VehicleTransportRequest vehicleTransport
) {}

