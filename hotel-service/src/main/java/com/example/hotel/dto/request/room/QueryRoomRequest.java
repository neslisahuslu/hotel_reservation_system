package com.example.hotel.dto.request.room;

import com.example.common.page.OrderDirection;

import java.util.UUID;

public record QueryRoomRequest(


        UUID hotelId,
        UUID roomId, // olacak mı gerçekten
        Integer capacity,
        String roomNumber,
        Integer page,
        Integer size,
        OrderDirection orderDirection,
        String orderBy
) {
    /**
     * Varsayılan değerleri ayarla (null geldiyse)
     */
    public QueryRoomRequest {
        if (page == null) page = 0;
        if (size == null) size = 10;
        if (orderDirection == null) orderDirection = OrderDirection.DESC;
        if (orderBy == null || orderBy.isBlank()) orderBy = "createdDate";
    }
}

