package com.example.reservation.dto;

import java.time.LocalDate;
import java.util.UUID;

public record ReservationRequest(
        UUID hotelId,
        UUID roomId,
        UUID userId,
        String guestName,
        LocalDate checkInDate,
        LocalDate checkOutDate
) {
}
