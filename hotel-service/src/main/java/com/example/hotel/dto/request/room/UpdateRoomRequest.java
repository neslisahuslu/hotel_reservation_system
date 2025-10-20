package com.example.hotel.dto.request.room;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateRoomRequest(
        @NotNull
        UUID hotelId,
        BigDecimal pricePerNight,
        String roomNumber,
        Integer capacity
) {
}
