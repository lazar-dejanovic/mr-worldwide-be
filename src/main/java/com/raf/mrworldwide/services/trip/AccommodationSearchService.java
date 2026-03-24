package com.raf.mrworldwide.services.trip;

import com.raf.mrworldwide.clients.StayApiClient;
import com.raf.mrworldwide.domain.dto.accomodation.AccommodationOfferDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class AccommodationSearchService {

    private final StayApiClient stayApiClient;

    public List<AccommodationOfferDto> search(String cityName, LocalDate checkIn,
                                              LocalDate checkOut, int adults) {
        return stayApiClient.searchAccommodation(cityName, checkIn, checkOut, adults);
    }
}

