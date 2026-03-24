package com.raf.mrworldwide.services.trip;

import com.raf.mrworldwide.dao.repositories.AirplaneTransportRepository;
import com.raf.mrworldwide.dao.repositories.TransportRepository;
import com.raf.mrworldwide.dao.repositories.VehicleTransportRepository;
import com.raf.mrworldwide.domain.dto.trip.TransportDto;
import com.raf.mrworldwide.domain.dto.trip.TransportRequest;
import com.raf.mrworldwide.domain.entities.accomodation.Accommodation;
import com.raf.mrworldwide.domain.entities.transport.AirplaneTransport;
import com.raf.mrworldwide.domain.entities.transport.Transport;
import com.raf.mrworldwide.domain.entities.transport.TransportType;
import com.raf.mrworldwide.domain.entities.transport.VehicleTransport;
import com.raf.mrworldwide.domain.entities.trip.TripPlan;
import com.raf.mrworldwide.domain.entities.trip.TripSegment;
import com.raf.mrworldwide.domain.entities.user.User;
import com.raf.mrworldwide.domain.mappers.TransportMapper;
import com.raf.mrworldwide.exceptions.BadRequestException;
import com.raf.mrworldwide.exceptions.NotFoundException;
import com.raf.mrworldwide.utils.AuthUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.UUID;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class TransportService {

    private static final LocalTime LATE_NIGHT_START = LocalTime.of(22, 0);
    private static final LocalTime LATE_NIGHT_END = LocalTime.of(6, 0);

    private final TransportRepository transportRepository;
    private final AirplaneTransportRepository airplaneTransportRepository;
    private final VehicleTransportRepository vehicleTransportRepository;
    private final TripPlanService tripPlanService;
    private final TripSegmentService tripSegmentService;

    public TransportDto get(UUID tripId, UUID segmentId) {
        User user = AuthUtils.getLoggedUser();
        TripPlan tripPlan = tripPlanService.getEntityById(tripId);
        tripPlanService.verifyOwnership(user, tripPlan);

        TripSegment segment = tripSegmentService.getEntityById(segmentId);
        if (segment.getTransport() == null) {
            throw new NotFoundException("No transport found for this segment");
        }
        return TransportMapper.INSTANCE.toDto(segment.getTransport());
    }

    @Transactional
    public TransportDto createOrUpdate(UUID tripId, UUID segmentId, TransportRequest request) {
        User user = AuthUtils.getLoggedUser();
        TripPlan tripPlan = tripPlanService.getEntityById(tripId);
        tripPlanService.verifyOwnership(user, tripPlan);

        TripSegment segment = tripSegmentService.getEntityById(segmentId);

        validateTransportRequest(request);

        // Remove existing transport if present
        if (segment.getTransport() != null) {
            deleteTransportChildren(segment.getTransport());
            segment.setTransport(null);
        }

        AirplaneTransport airplane = null;
        VehicleTransport vehicle = null;

        if (request.transportType() == TransportType.AIRPLANE && request.airplaneTransport() != null) {
            airplane = AirplaneTransport.builder()
                    .flightNumber(request.airplaneTransport().flightNumber())
                    .departureTime(request.airplaneTransport().departureTime())
                    .arrivalTime(request.airplaneTransport().arrivalTime())
                    .duration(request.airplaneTransport().duration())
                    .price(request.airplaneTransport().price())
                    .currency(request.airplaneTransport().currency())
                    .build();
            airplane = airplaneTransportRepository.save(airplane);
        }

        if (request.transportType() == TransportType.VEHICLE && request.vehicleTransport() != null) {
            vehicle = VehicleTransport.builder()
                    .distanceKm(request.vehicleTransport().distanceKm())
                    .estimatedFuelCost(request.vehicleTransport().estimatedFuelCost())
                    .tollCost(request.vehicleTransport().tollCost())
                    .build();
            vehicle = vehicleTransportRepository.save(vehicle);
        }

        Transport transport = Transport.builder()
                .transportType(request.transportType())
                .airplaneTransport(airplane)
                .vehicleTransport(vehicle)
                .build();
        transport = transportRepository.save(transport);

        segment.setTransport(transport);

        // Late-night landing check: if arrival is between 22:00 and 05:59, shift accommodation checkIn back one day
        if (airplane != null && airplane.getArrivalTime() != null) {
            LocalTime arrival = airplane.getArrivalTime().toLocalTime();
            if (isLateNight(arrival)) {
                applyLateNightCheckInShift(segment);
            }
        }

        return TransportMapper.INSTANCE.toDto(transport);
    }

    @Transactional
    public void delete(UUID tripId, UUID segmentId) {
        User user = AuthUtils.getLoggedUser();
        TripPlan tripPlan = tripPlanService.getEntityById(tripId);
        tripPlanService.verifyOwnership(user, tripPlan);

        TripSegment segment = tripSegmentService.getEntityById(segmentId);
        if (segment.getTransport() == null) {
            throw new NotFoundException("No transport found for this segment");
        }

        deleteTransportChildren(segment.getTransport());
        segment.setTransport(null);
    }

    private void deleteTransportChildren(Transport transport) {
        if (transport.getAirplaneTransport() != null) {
            airplaneTransportRepository.delete(transport.getAirplaneTransport());
        }
        if (transport.getVehicleTransport() != null) {
            vehicleTransportRepository.delete(transport.getVehicleTransport());
        }
        transportRepository.delete(transport);
    }

    private void applyLateNightCheckInShift(TripSegment segment) {
        Accommodation accommodation = segment.getAccommodation();
        if (accommodation != null && accommodation.getCheckIn() != null) {
            accommodation.setCheckIn(accommodation.getCheckIn().minusDays(1));
        }
    }

    private boolean isLateNight(LocalTime time) {
        return !time.isBefore(LATE_NIGHT_START) || time.isBefore(LATE_NIGHT_END);
    }

    private void validateTransportRequest(TransportRequest request) {
        if (request.transportType() == TransportType.AIRPLANE && request.airplaneTransport() == null) {
            throw new BadRequestException("airplaneTransport details are required for AIRPLANE type");
        }
        if (request.transportType() == TransportType.VEHICLE && request.vehicleTransport() == null) {
            throw new BadRequestException("vehicleTransport details are required for VEHICLE type");
        }
    }
}

