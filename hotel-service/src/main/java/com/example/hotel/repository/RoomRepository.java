package com.example.hotel.repository;

import com.example.hotel.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {
    List<Room> findByHotelId(UUID hotelId);
    boolean existsByHotelIdAndRoomNumberIgnoreCase(UUID hotelId, String roomNumber);

}
