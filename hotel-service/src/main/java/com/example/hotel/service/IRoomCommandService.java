package com.example.hotel.service;

import com.example.hotel.dto.request.room.CreateRoomRequest;
import com.example.hotel.dto.request.room.DeleteRoomRequest;
import com.example.hotel.dto.request.room.UpdateRoomRequest;
import com.example.hotel.dto.response.RoomResponse;

import java.util.UUID;

public interface IRoomCommandService {

    RoomResponse createRoom(CreateRoomRequest request);
    RoomResponse updateRoom(UUID roomId, UpdateRoomRequest request);
    void deleteRoom( DeleteRoomRequest request);

}
