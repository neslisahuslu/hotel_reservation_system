package com.example.hotel.dto.response;

import java.time.Instant;
import java.util.UUID;

public record HotelResponse(
        UUID id,
        String name,
        String address,
        Integer starRating,
        Instant createdDate,
        Instant updatedDate) {

    public static HotelResponse of(UUID id, String name, String address, Integer starRating, Instant createdDate, Instant updatedDate) {
        return new HotelResponse(id, name, address, starRating, createdDate, updatedDate);
    }
}
