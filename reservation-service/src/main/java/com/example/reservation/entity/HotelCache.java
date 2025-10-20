package com.example.reservation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "hotel_cache")
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class HotelCache {
    @Id
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;
    private String name;
    private String address;
}