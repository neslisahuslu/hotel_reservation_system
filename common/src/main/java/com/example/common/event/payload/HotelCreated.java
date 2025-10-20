package com.example.common.event.payload;

import com.example.common.event.EventPayload;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record HotelCreated(
        @NotNull
        UUID hotelId,

        @NotNull
        String name,

        @NotNull
        String address
)
        implements EventPayload {
}
