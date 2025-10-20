package com.example.reservation.mapper;

import com.example.reservation.dto.ReservationResponse;
import com.example.reservation.entity.Reservation;
import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Page;

@UtilityClass
public class ReservationMapper{
    public ReservationResponse toDto(Reservation reservation){
        return ReservationResponse.builder()
                .id(reservation.getId())
                .hotelId(reservation.getHotel().getId())
                .hotelName(reservation.getHotel().getName())
                .roomId(reservation.getRoom().getId())
                .roomNumber(reservation.getRoom().getRoomNumber())
                .guestName(reservation.getGuestName())
                .checkInDate(reservation.getCheckInDate())
                .checkOutDate(reservation.getCheckOutDate())
                .capacity(reservation.getRoom().getCapacity())
                .build();
    }

    public static Page<ReservationResponse> toPagedDto(Page<Reservation> reservations) {
        return reservations.map(reservation ->
                new ReservationResponse(
                        reservation.getId(),
                        reservation.getHotel().getId(),
                        reservation.getHotel().getName(),
                        reservation.getRoom().getId(),
                        reservation.getRoom().getRoomNumber(),
                        reservation.getCheckInDate(),
                        reservation.getCheckOutDate(),
                        reservation.getRoom().getCapacity(),
                        reservation.getGuestName()
                        )
        );
    }
}