package com.example.common.event;

import com.example.common.event.payload.*;
import lombok.Getter;

@Getter
public enum EventType {

    HOTEL_CREATED(HotelCreated.class,"hotel.created","reservation-service"),
    HOTEL_UPDATED(HotelUpdated.class,"hotel.updated","reservation-service"),
    ROOM_CREATED(RoomCreated.class,"room.created","reservation-service"),
    ROOM_UPDATED(RoomUpdated.class,"room.updated","reservation-service"),
    RESERVATION_CREATED(ReservationCreated.class,"reservation.created","notification-service")
    ;

    private final Class<? extends EventPayload> payloadType;
    private final String topics;
    private final String groupId;

    EventType(Class<? extends EventPayload> payloadType, String topics, String groupId) {
        this.payloadType = payloadType;
        this.topics = topics;
        this.groupId = groupId;

    }

}
