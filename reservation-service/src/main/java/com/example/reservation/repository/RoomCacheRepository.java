package com.example.reservation.repository;

import com.example.reservation.entity.RoomCache;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RoomCacheRepository extends JpaRepository<RoomCache, UUID> {
}
