package com.example.hotel.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RoomResponse(
        UUID id,
        UUID hotelId,
        String roomNumber,
        Integer capacity,
        BigDecimal pricePerNight,
        Instant createdDate,
        Instant updatedDate) {

    public static RoomResponse of(UUID id, UUID hotelId, String roomNumber, Integer capacity, BigDecimal pricePerNight, Instant createdDate, Instant updatedDate) {
        return new RoomResponse(id, hotelId, roomNumber, capacity, pricePerNight, createdDate, updatedDate);
    }
}