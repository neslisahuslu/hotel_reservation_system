package com.example.reservation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "room_cache")
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class RoomCache {
    @Id
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;
    private UUID hotelId;
    private String RoomNumber;
    private int capacity;
    private BigDecimal pricePerNight;
}