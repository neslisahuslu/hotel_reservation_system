package com.example.notification.listener;

import com.example.common.event.GenericEvent;
import com.example.common.event.payload.ReservationCreated;
import com.example.notification.serviceImpl.EmailService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationEventsListener {

    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "reservation.created",groupId = "notification-service")
    public void onReservationCreated( String message){

        try {
            GenericEvent<ReservationCreated> event =
                    objectMapper.readValue(message, new TypeReference<GenericEvent<ReservationCreated>>() {});

            emailService.send(
                    event.payload().email(), // rezervasyon sahibinin mail’i
                    "Rezervasyonunuz oluşturuldu",
                    """
                    Merhaba %s,
                    %s - %s tarihleri arasında oda %s için rezervasyonunuz oluşturuldu.
                    Rezervasyon No: %s
                    """.formatted(
                            event.payload().guestName(),
                            event.payload().checkInDate(),
                            event.payload().checkOutDate(),
                            event.payload().roomId(),
                            event.payload().reservationId()));

        } catch (Exception e) {
            log.error("Failed to process Hotel Created event", e);
        }

    }
}
