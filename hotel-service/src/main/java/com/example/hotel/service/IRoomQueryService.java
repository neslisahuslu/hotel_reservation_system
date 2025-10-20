package com.example.hotel.service;

import com.example.common.page.OrderDirection;
import com.example.hotel.dto.response.RoomResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface IRoomQueryService {

    RoomResponse getRoomById(UUID id);
    Page<RoomResponse> getAllRooms(int page, int size, String orderBy, OrderDirection orderDirection);
}
