package com.raf.mrworldwide.domain.dto.accomodation;

public record AccommodationOfferDto(
        String name,
        String address,
        String imageUrl,
        String bookingUrl,
        Double starRating,
        Double reviewScore,
        Double priceTotal,
        String currency
) {}

