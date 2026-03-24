package com.raf.mrworldwide.clients;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.raf.mrworldwide.domain.dto.trip.FlightOfferDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class AmadeusClient {

    private final WebClient webClient;
    private final String baseUrl;
    private final String apiKey;
    private final String apiSecret;

    public AmadeusClient(WebClient webClient,
                         @Value("${amadeus.api.base-url}") String baseUrl,
                         @Value("${amadeus.api.key}") String apiKey,
                         @Value("${amadeus.api.secret}") String apiSecret) {
        this.webClient = webClient;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    @Cacheable(value = "amadeusToken", unless = "#result == null")
    public String getAccessToken() {
        log.info("Fetching new Amadeus access token");
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", apiKey);
        form.add("client_secret", apiSecret);

        AmadeusTokenResponse response = webClient.post()
                .uri(baseUrl + "/v1/security/oauth2/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(BodyInserters.fromFormData(form))
                .retrieve()
                .bodyToMono(AmadeusTokenResponse.class)
                .block();

        return response != null ? response.accessToken() : null;
    }

    public List<FlightOfferDto> searchFlights(String origin, String destination,
                                              LocalDate departureDate, int adults) {
        String token = getAccessToken();
        if (token == null) {
            log.warn("Amadeus token unavailable, returning empty flight list");
            return Collections.emptyList();
        }

        try {
            AmadeusFlightSearchResponse response = webClient.get()
                    .uri(baseUrl + "/v2/shopping/flight-offers", uriBuilder -> uriBuilder
                            .queryParam("originLocationCode", origin)
                            .queryParam("destinationLocationCode", destination)
                            .queryParam("departureDate", departureDate.toString())
                            .queryParam("adults", adults)
                            .queryParam("max", 10)
                            .build())
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(AmadeusFlightSearchResponse.class)
                    .block();

            if (response == null || response.data() == null) return Collections.emptyList();
            return response.data().stream().map(this::mapOffer).toList();
        } catch (Exception e) {
            log.error("Error searching Amadeus flights: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private FlightOfferDto mapOffer(Map<String, Object> offer) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> itineraries = (List<Map<String, Object>>) offer.get("itineraries");
            @SuppressWarnings("unchecked")
            Map<String, Object> firstItinerary = itineraries.get(0);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> segments = (List<Map<String, Object>>) firstItinerary.get("segments");
            @SuppressWarnings("unchecked")
            Map<String, Object> firstSegment = segments.get(0);

            String carrierCode = (String) firstSegment.get("carrierCode");
            String number = (String) firstSegment.get("number");
            String flightNumber = carrierCode + " " + number;

            @SuppressWarnings("unchecked")
            Map<String, Object> departure = (Map<String, Object>) firstSegment.get("departure");
            @SuppressWarnings("unchecked")
            Map<String, Object> arrival = (Map<String, Object>) firstSegment.get("arrival");

            LocalDateTime departureTime = LocalDateTime.parse((String) departure.get("at"), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            LocalDateTime arrivalTime = LocalDateTime.parse((String) arrival.get("at"), DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            String rawDuration = (String) firstItinerary.get("duration");
            String duration = parseDuration(rawDuration);

            int stops = segments.size() - 1;

            @SuppressWarnings("unchecked")
            Map<String, Object> price = (Map<String, Object>) offer.get("price");
            double priceVal = Double.parseDouble(price.get("total").toString());
            String currency = (String) price.get("currency");

            return new FlightOfferDto(flightNumber, carrierCode, departureTime, arrivalTime,
                    duration, priceVal, currency, stops, null);
        } catch (Exception e) {
            log.warn("Failed to parse Amadeus flight offer: {}", e.getMessage());
            return null;
        }
    }

    /** Converts ISO 8601 duration (PT2H10M) to human-readable (2h 10m) */
    private String parseDuration(String iso) {
        if (iso == null) return null;
        iso = iso.replace("PT", "");
        String result = iso.replace("H", "h ").replace("M", "m").trim();
        return result.endsWith(" ") ? result.trim() : result;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record AmadeusTokenResponse(@JsonProperty("access_token") String accessToken) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record AmadeusFlightSearchResponse(@JsonProperty("data") List<Map<String, Object>> data) {}
}

