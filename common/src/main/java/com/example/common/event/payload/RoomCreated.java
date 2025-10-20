package com.example.common.event.payload;


import com.example.common.event.EventPayload;

import java.math.BigDecimal;
import java.util.UUID;

public record RoomCreated(
        UUID id,
        UUID hotelId,
        String roomNumber,
        int capacity,
        BigDecimal pricePerNight
)
        implements EventPayload {
}
