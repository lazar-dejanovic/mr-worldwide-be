package com.raf.mrworldwide.clients;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.raf.mrworldwide.domain.dto.accomodation.AccommodationOfferDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class StayApiClient {

    private final WebClient webClient;
    private final String baseUrl;
    private final String apiKey;

    public StayApiClient(WebClient webClient,
                         @Value("${stayapi.base-url}") String baseUrl,
                         @Value("${stayapi.api.key}") String apiKey) {
        this.webClient = webClient;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }

    @Cacheable(value = "stayApiDestId", key = "#cityName.toLowerCase()")
    public String resolveDestinationId(String cityName) {
        log.info("Resolving StayAPI dest_id for city: {}", cityName);
        try {
            DestinationLookupResponse response = webClient.get()
                    .uri(baseUrl + "/v1/booking/destinations/lookup", uriBuilder -> uriBuilder
                            .queryParam("query", cityName)
                            .build())
                    .header("x-api-key", apiKey)
                    .retrieve()
                    .bodyToMono(DestinationLookupResponse.class)
                    .block();

            return response != null ? String.valueOf(response.destId()) : null;
        } catch (Exception e) {
            log.error("Error resolving StayAPI dest_id for {}: {}", cityName, e.getMessage());
            return null;
        }
    }

    public List<AccommodationOfferDto> searchAccommodation(String cityName, LocalDate checkIn,
                                                            LocalDate checkOut, int adults) {
        String destId = resolveDestinationId(cityName);
        if (destId == null) {
            log.warn("Could not resolve dest_id for city: {}", cityName);
            return Collections.emptyList();
        }

        try {
            AccommodationSearchResponse response = webClient.get()
                    .uri(baseUrl + "/v1/booking/search", uriBuilder -> uriBuilder
                            .queryParam("dest_id", destId)
                            .queryParam("checkin", checkIn.toString())
                            .queryParam("checkout", checkOut.toString())
                            .queryParam("adults", adults)
                            .queryParam("rooms", 1)
                            .build())
                    .header("x-api-key", apiKey)
                    .retrieve()
                    .bodyToMono(AccommodationSearchResponse.class)
                    .block();

            if (response == null || response.data() == null) return Collections.emptyList();
            return response.data().stream().map(this::mapHotel).toList();
        } catch (Exception e) {
            log.error("Error searching StayAPI accommodation: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private AccommodationOfferDto mapHotel(HotelResult h) {
        return new AccommodationOfferDto(
                h.hotelName(),
                h.address(),
                h.imageUrl(),
                h.url(),
                h.starRating() != null ? h.starRating().doubleValue() : null,
                h.reviewScore(),
                h.minTotalPrice(),
                h.currencyCode()
        );
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record DestinationLookupResponse(@JsonProperty("dest_id") long destId) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record AccommodationSearchResponse(@JsonProperty("data") List<HotelResult> data) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record HotelResult(
            @JsonProperty("hotel_name") String hotelName,
            @JsonProperty("url") String url,
            @JsonProperty("image_url") String imageUrl,
            @JsonProperty("star_rating") Integer starRating,
            @JsonProperty("review_score") Double reviewScore,
            @JsonProperty("address") String address,
            @JsonProperty("min_total_price") Double minTotalPrice,
            @JsonProperty("currency_code") String currencyCode
    ) {}
}

