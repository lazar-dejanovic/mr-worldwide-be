package com.raf.mrworldwide.clients;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.raf.mrworldwide.domain.dto.trip.DailyItineraryDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class FoursquareClient {

    // Category name → Foursquare fsq_category_id mapping
    private static final Map<String, String> CATEGORY_MAP = Map.of(
            "MUSEUM",    "4bf58dd8d48988d18f941735",
            "PARK",      "4bf58dd8d48988d163941735",
            "FOOD",      "4d4b7105d754a06374d81259",
            "NIGHTLIFE", "4d4b7105d754a06376d81259",
            "SHOPPING",  "4d4b7105d754a06378d81259",
            "THEATER",   "4bf58dd8d48988d137941735",
            "RESTAURANT","4bf58dd8d48988d1d5941735"
    );

    private final WebClient webClient;
    private final String baseUrl;
    private final String apiKey;

    public FoursquareClient(WebClient webClient,
                             @Value("${foursquare.base-url}") String baseUrl,
                             @Value("${foursquare.api.key}") String apiKey) {
        this.webClient = webClient;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }

    public List<DailyItineraryDto> searchPlaces(Double latitude, Double longitude,
                                                List<String> categories, int radius) {
        if (latitude == null || longitude == null) return Collections.emptyList();

        String categoryIds = categories.stream()
                .map(c -> CATEGORY_MAP.getOrDefault(c.toUpperCase(), c))
                .reduce((a, b) -> a + "," + b)
                .orElse("");

        try {
            FoursquareSearchResponse response = webClient.get()
                    .uri(baseUrl + "/places/search", uriBuilder -> uriBuilder
                            .queryParam("ll", latitude + "," + longitude)
                            .queryParam("fsq_category_ids", categoryIds)
                            .queryParam("radius", radius)
                            .queryParam("sort", "RATING")
                            .queryParam("limit", 20)
                            .build())
                    .header("Authorization", apiKey)
                    .retrieve()
                    .bodyToMono(FoursquareSearchResponse.class)
                    .block();

            if (response == null || response.results() == null) return Collections.emptyList();
            return response.results().stream().map(this::mapPlace).toList();
        } catch (Exception e) {
            log.error("Error searching Foursquare places: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private DailyItineraryDto mapPlace(PlaceResult place) {
        String categoryName = null;
        String categoryIconUrl = null;

        if (place.categories() != null && !place.categories().isEmpty()) {
            CategoryResult cat = place.categories().get(0);
            categoryName = cat.name();
            if (cat.icon() != null) {
                categoryIconUrl = cat.icon().prefix() + "bg_64" + cat.icon().suffix();
            }
        }

        String address = place.location() != null ? place.location().formattedAddress() : null;

        return new DailyItineraryDto(
                null, // transient — no base
                place.name(),
                categoryName,
                categoryIconUrl,
                address,
                place.latitude(),
                place.longitude(),
                null, null, null
        );
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record FoursquareSearchResponse(@JsonProperty("results") List<PlaceResult> results) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record PlaceResult(
            @JsonProperty("name") String name,
            @JsonProperty("latitude") Double latitude,
            @JsonProperty("longitude") Double longitude,
            @JsonProperty("categories") List<CategoryResult> categories,
            @JsonProperty("location") LocationResult location
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record CategoryResult(
            @JsonProperty("name") String name,
            @JsonProperty("icon") IconResult icon
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record IconResult(
            @JsonProperty("prefix") String prefix,
            @JsonProperty("suffix") String suffix
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record LocationResult(@JsonProperty("formatted_address") String formattedAddress) {}
}

