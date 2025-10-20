package com.example.hotel.mappers;

import com.example.hotel.dto.response.RoomResponse;
import com.example.hotel.entity.Room;
import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Page;

@UtilityClass
public class RoomMapper {

    public static RoomResponse toDto(Room room) {
        return RoomResponse.of(
                room.getId(),
                room.getHotel().getId(),
                room.getRoomNumber(),
                room.getCapacity(),
                room.getPricePerNight(),
                room.getCreatedDate(),
                room.getUpdatedDate());
    }

    public static Page<RoomResponse> toPagedDto(Page<Room> rooms) {
        return rooms.map(room ->
                new RoomResponse(
                        room.getId(),
                        room.getHotel().getId(),
                        room.getRoomNumber(),
                        room.getCapacity(),
                        room.getPricePerNight(),
                        room.getCreatedDate(),
                        room.getUpdatedDate()
                )
        );
    }
}