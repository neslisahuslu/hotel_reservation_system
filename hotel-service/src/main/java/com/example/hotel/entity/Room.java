package com.example.hotel.entity;

import com.example.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "rooms",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_hotel_room", columnNames = {"hotel_id", "room_number"})
        },
        indexes = {
                @Index(name = "idx_room_hotel_id", columnList = "hotel_id")
        }
)
public class Room extends BaseEntity {


    @Column(name = "hotel_id", updatable = false, insertable = false)
    private UUID hotelId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;

    @NotBlank
    @Size(max = 50)
    @Column(name = "room_number", nullable = false, length = 50)
    private String roomNumber;

    @NotNull
    @Min(1)
    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @NotNull
    @Digits(integer = 10, fraction = 2)
    @PositiveOrZero
    @Column(name = "price_per_night", nullable = false, precision = 12, scale = 2)
    private BigDecimal pricePerNight;

}