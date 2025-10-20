package com.example.reservation.dto;

import java.time.LocalDate;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {

    private UUID id;

    private UUID hotelId;

    private String hotelName;

    private UUID roomId;

    private String roomNumber;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private int capacity;

    private String guestName;
}
