package com.raf.mrworldwide.domain.entities.transport;

import com.raf.mrworldwide.domain.entities.BaseEntityUUID;
import com.raf.mrworldwide.domain.entities.trip.TripSegment;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "transport")
public class Transport extends BaseEntityUUID {

    @Enumerated(EnumType.STRING)
    private TransportType transportType;

    @OneToOne
    private AirplaneTransport airplaneTransport;

    @OneToOne
    private VehicleTransport vehicleTransport;

    @OneToOne(mappedBy = "transport")
    private TripSegment tripSegment;

}
