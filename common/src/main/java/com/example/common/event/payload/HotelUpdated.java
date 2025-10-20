package com.example.common.event.payload;

import com.example.common.event.EventPayload;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record HotelUpdated(

        @NotNull
        UUID hotelId,

        @NotNull
        String name,

        @NotNull
        String address
)
        implements EventPayload {
}
