package com.example.hotel.dto.request.hotel;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DeleteHotelRequest(
        @NotNull
        UUID id
) {
}
