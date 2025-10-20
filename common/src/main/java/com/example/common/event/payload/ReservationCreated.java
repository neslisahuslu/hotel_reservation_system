package com.example.common.event.payload;

import com.example.common.event.EventPayload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record ReservationCreated(
        @NotNull
        UUID reservationId,

        @NotNull
        UUID hotelId,

        @NotNull
        UUID roomId,

        @NotBlank
        String guestName,

        @NotBlank
        String email,

        @NotNull
        LocalDate checkInDate,

        @NotNull
        LocalDate checkOutDate
)
          implements EventPayload {
}
