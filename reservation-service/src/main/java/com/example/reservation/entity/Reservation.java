package com.example.reservation.entity;

import com.example.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "reservations", indexes = {@Index(columnList = "room_id,check_in_date,check_out_date")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hotel_id", nullable = false )
    private HotelCache hotel;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private RoomCache room;

    @NotNull
    @Column(name = "user_id")
    private UUID userId;

    @NotBlank
    @Column(name = "guest_name")
    private String guestName;

    @NotNull
    @Column(name = "check_in_date")
    private LocalDate checkInDate;

    @NotNull
    @Column(name = "check_out_date")
    private LocalDate checkOutDate;


}
