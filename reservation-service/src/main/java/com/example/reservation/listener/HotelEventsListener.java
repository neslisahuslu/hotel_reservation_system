package com.example.reservation.listener;

import com.example.common.event.GenericEvent;
import com.example.common.event.payload.HotelCreated;
import com.example.common.event.payload.HotelUpdated;
import com.example.reservation.entity.HotelCache;
import com.example.reservation.repository.HotelCacheRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HotelEventsListener {

    private final HotelCacheRepository hotelCacheRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "hotel.created", groupId = "reservation-service")
    public void handleHotelCreatedEvent(String message) {

        try {
            GenericEvent<HotelCreated> event =
                    objectMapper.readValue(message, new TypeReference<GenericEvent<HotelCreated>>() {});
            hotelCacheRepository.save(new HotelCache(event.payload().hotelId(), event.payload().name(),event.payload().address()));

        } catch (Exception e) {
            log.error("Failed to process Hotel Created event", e);
        }

    }

    @KafkaListener(topics = "hotel.updated", groupId = "reservation-service")
    public void handleHotelUpdatedEvent(String message) {

        try {
            GenericEvent<HotelUpdated> event =
                    objectMapper.readValue(message, new TypeReference<GenericEvent<HotelUpdated>>() {});
            log.info("Received Hotel Updated event: {}", event.payload());
            hotelCacheRepository.save(new HotelCache(event.payload().hotelId(), event.payload().name(),event.payload().address()));
        } catch (Exception e) {
            log.error("Failed to process Hotel Updated event", e);
        }

    }

}


