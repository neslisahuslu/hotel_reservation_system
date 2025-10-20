package com.example.hotel.dto.request.room;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateRoomRequest(

        @NotNull
        UUID hotelId,
        Integer capactiy,
        BigDecimal pricePerNight,
        String roomNumber
) {
}
