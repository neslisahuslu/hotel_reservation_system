package com.example.reservation.repository;

import com.example.reservation.entity.HotelCache;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface HotelCacheRepository extends JpaRepository<HotelCache, UUID> {
}
