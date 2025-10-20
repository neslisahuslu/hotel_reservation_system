package com.example.reservation.serviceImp;

import com.example.common.event.EventType;
import com.example.common.event.KafkaEventPublisher;
import com.example.common.event.payload.ReservationCreated;
import com.example.common.page.OrderDirection;
import com.example.reservation.dto.ReservationRequest;
import com.example.reservation.dto.ReservationResponse;
import com.example.reservation.entity.HotelCache;
import com.example.reservation.entity.Reservation;
import com.example.reservation.entity.RoomCache;
import com.example.reservation.mapper.ReservationMapper;
import com.example.reservation.repository.ReservationRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock private ReservationRepository repository;
    @Mock private KafkaEventPublisher publisher;
    @Mock private EntityManager entityManager;

    @InjectMocks
    private ReservationService service;

    @Test
    void create_success_persists_publishesEvent_andReturnsDto() {
        UUID hotelId = UUID.randomUUID();
        UUID roomId  = UUID.randomUUID();
        UUID userId  = UUID.randomUUID();
        UUID resId   = UUID.randomUUID();

        var req = new ReservationRequest(
                hotelId,
                roomId,
                userId,
                "Neslişah Uslu",
                LocalDate.of(2025, 10, 22),
                LocalDate.of(2025, 10, 24)
        );
        String email = "nes@example.com";

        when(entityManager.getReference(HotelCache.class, hotelId))
                .thenReturn(new HotelCache(hotelId, "Test Hotel", "Istanbul"));
        when(entityManager.getReference(RoomCache.class, roomId))
                .thenReturn(new RoomCache(roomId, hotelId, "101", 2, new BigDecimal("120.00")));

        when(repository.save(any(Reservation.class))).thenAnswer(inv -> {
            Reservation r = inv.getArgument(0);
            r.setId(resId);
            return r;
        });

        var expectedDto = new ReservationResponse(
                resId,
                hotelId,
                "Hilton",
                roomId,
                "101",
                LocalDate.of(2025, 10, 22),
                LocalDate.of(2025, 10, 24),
                2,
                "Neslişah Uslu"
        );

        try (MockedStatic<ReservationMapper> mocked = mockStatic(ReservationMapper.class)) {
            mocked.when(() -> ReservationMapper.toDto(any(Reservation.class)))
                    .thenReturn(expectedDto);

            var actual = service.create(req, email);

            assertNotNull(actual);
            assertEquals(expectedDto, actual);

            ArgumentCaptor<EventType> typeCap = ArgumentCaptor.forClass(EventType.class);
            ArgumentCaptor<String> keyCap     = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<ReservationCreated> payloadCap = ArgumentCaptor.forClass(ReservationCreated.class);

            verify(publisher, times(1))
                    .publish(typeCap.capture(), keyCap.capture(), payloadCap.capture());

            assertEquals(EventType.RESERVATION_CREATED, typeCap.getValue());
            assertEquals(resId.toString(), keyCap.getValue());

            ReservationCreated payload = payloadCap.getValue();
            assertEquals(resId, payload.reservationId());
            assertEquals(hotelId, payload.hotelId());
            assertEquals(roomId, payload.roomId());
            assertEquals("Neslişah Uslu", payload.guestName());
            assertEquals(email, payload.email());
            assertEquals(LocalDate.of(2025, 10, 22), payload.checkInDate());
            assertEquals(LocalDate.of(2025, 10, 24), payload.checkOutDate());
        }
    }

    @Test
    void listMyReservations_success_returnsMappedPage() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate"));

        var res1 = new Reservation(); res1.setId(UUID.randomUUID());
        var res2 = new Reservation(); res2.setId(UUID.randomUUID());
        var pageEntities = new PageImpl<>(List.of(res1, res2), pageable, 2);

        when(repository.findByUserId(eq(userId), any(Pageable.class)))
                .thenReturn(pageEntities);

        var dto1 = new ReservationResponse(res1.getId(), null, null, null, null, null, null, 0, null);
        var dto2 = new ReservationResponse(res2.getId(), null, null, null, null, null, null, 0, null);
        var expectedPage = new PageImpl<>(List.of(dto1, dto2), pageable, 2);

        try (MockedStatic<ReservationMapper> mocked = mockStatic(ReservationMapper.class)) {
            mocked.when(() -> ReservationMapper.toPagedDto(pageEntities))
                    .thenReturn(expectedPage);

            var actual = service.listMyReservations(0, 10, "createdDate", OrderDirection.DESC, userId);

            assertNotNull(actual);
            assertEquals(2, actual.getTotalElements());
            assertEquals(dto1, actual.getContent().get(0));
            assertEquals(dto2, actual.getContent().get(1));

            verify(repository, times(1))
                    .findByUserId(eq(userId), any(Pageable.class));
        }
    }
}
