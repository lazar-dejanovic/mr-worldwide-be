package com.raf.mrworldwide.services.trip;

import com.raf.mrworldwide.clients.AmadeusClient;
import com.raf.mrworldwide.domain.dto.trip.FlightOfferDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class FlightSearchService {

    private final AmadeusClient amadeusClient;

    public List<FlightOfferDto> search(String origin, String destination,
                                       LocalDate departureDate, int adults) {
        return amadeusClient.searchFlights(origin, destination, departureDate, adults);
    }
}

