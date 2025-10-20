package com.example.reservation.listener;

import com.example.common.event.GenericEvent;
import com.example.common.event.payload.RoomCreated;
import com.example.common.event.payload.RoomUpdated;
import com.example.reservation.entity.RoomCache;
import com.example.reservation.repository.RoomCacheRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomEventsListener {

    private final RoomCacheRepository roomCacheRepository;
    private final ObjectMapper objectMapper;


    @KafkaListener(topics = "room.created", groupId = "reservation-service")
    public void handleRoomCreatedEvent(String message) {

        try {
            GenericEvent<RoomCreated> event =
                    objectMapper.readValue(message, new TypeReference<GenericEvent<RoomCreated>>() {});
            roomCacheRepository.save(new RoomCache(
                    event.payload().id(),
                    event.payload().hotelId(),
                    event.payload().roomNumber(),
                    event.payload().capacity(),
                    event.payload().pricePerNight()));

        } catch (Exception e) {
            log.error("Failed to process Hotel Created event", e);
        }

    }


    @KafkaListener(topics = "room.updated", groupId = "reservation-service")
    public void handleRoomUpdatedEvent(String message) {

        try {
            GenericEvent<RoomUpdated> event =
                    objectMapper.readValue(message, new TypeReference<GenericEvent<RoomUpdated>>() {});
            log.info("📅 Received Room Updated event: {}", event.payload());
            roomCacheRepository.save(new RoomCache(
                    event.payload().id(),
                    event.payload().hotelId(),
                    event.payload().roomNumber(),
                    event.payload().capacity(),
                    event.payload().pricePerNight()));
        } catch (Exception e) {
            log.error("Failed to process Room Updated event", e);
        }
    }
}
