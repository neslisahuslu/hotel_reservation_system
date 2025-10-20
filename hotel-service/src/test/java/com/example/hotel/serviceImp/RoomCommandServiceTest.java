package com.example.hotel.serviceImp;

import com.example.common.event.EventType;
import com.example.common.event.KafkaEventPublisher;
import com.example.common.event.payload.RoomCreated;
import com.example.common.event.payload.RoomUpdated;
import com.example.common.exception.BaseException;
import com.example.common.exception.ExceptionErrorMessage;
import com.example.hotel.dto.request.room.CreateRoomRequest;
import com.example.hotel.dto.request.room.DeleteRoomRequest;
import com.example.hotel.dto.request.room.UpdateRoomRequest;
import com.example.hotel.dto.response.RoomResponse;
import com.example.hotel.entity.Hotel;
import com.example.hotel.entity.Room;
import com.example.hotel.mappers.RoomMapper;
import com.example.hotel.repository.HotelRepository;
import com.example.hotel.repository.RoomRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomCommandServiceTest {

    @Mock RoomRepository roomRepository;
    @Mock HotelRepository hotelRepository;
    @Mock KafkaEventPublisher publisher;

    private MockedStatic<RoomMapper> roomMapperStatic;

    @AfterEach
    void tearDown() {
        if (roomMapperStatic != null) {
            roomMapperStatic.close();
            roomMapperStatic = null;
        }
    }

    private RoomCommandService service() {
        return new RoomCommandService(roomRepository, hotelRepository, publisher);
    }

    private static Hotel hotel(UUID id) {
        var h = new Hotel();
        h.setId(id);
        h.setName("H");
        h.setAddress("A");
        h.setStarRating(5);
        h.setCreatedDate(Instant.parse("2025-10-10T10:00:00Z"));
        h.setUpdatedDate(Instant.parse("2025-10-10T10:00:00Z"));
        h.setVersion(0);
        return h;
    }

    // -------- createRoom --------

    @Test
    void createRoom_success_publishesEvent_andReturnsDto() {
        var hotelId = UUID.randomUUID();
        var h = hotel(hotelId);

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(h));
        when(roomRepository.existsByHotelIdAndRoomNumberIgnoreCase(eq(hotelId), eq("101"))).thenReturn(false);

        var newId = UUID.randomUUID();
        var created = Instant.parse("2025-10-20T08:00:00Z");

        when(roomRepository.save(any(Room.class))).thenAnswer(inv -> {
            Room r = inv.getArgument(0);
            r.setId(newId);
            r.setCreatedDate(created);
            r.setUpdatedDate(created);
            r.setVersion(0);
            return r;
        });

        roomMapperStatic = mockStatic(RoomMapper.class);
        roomMapperStatic.when(() -> RoomMapper.toDto(any(Room.class)))
                .thenAnswer(inv -> {
                    Room r = inv.getArgument(0);
                    return new RoomResponse(
                            r.getId(), r.getHotelId(), r.getRoomNumber(),
                            r.getCapacity(), r.getPricePerNight(),
                            r.getCreatedDate(), r.getUpdatedDate()
                    );
                });

        var req = new CreateRoomRequest(hotelId, 2, new BigDecimal("999.90"), "101");
        var resp = service().createRoom(req);

        // ---- response
        assertNotNull(resp);
        assertEquals(newId, resp.id());
        assertEquals(hotelId, resp.hotelId());
        assertEquals("101", resp.roomNumber());
        assertEquals(Integer.valueOf(2), resp.capacity());
        assertEquals(new BigDecimal("999.90"), resp.pricePerNight());

        // ---- event

        var typeCap = ArgumentCaptor.forClass(EventType.class);
        var keyCap = ArgumentCaptor.forClass(String.class);
        var payloadCap = ArgumentCaptor.forClass(RoomCreated.class);

        verify(publisher).publish(typeCap.capture(), keyCap.capture(), payloadCap.capture());

        assertEquals(EventType.ROOM_CREATED, typeCap.getValue());

        assertNotNull(keyCap.getValue());
        assertNotNull(payloadCap.getValue().id());
        assertEquals(payloadCap.getValue().id().toString(), keyCap.getValue());

        assertEquals(resp.id(),                 payloadCap.getValue().id());
        assertEquals(hotelId,                   payloadCap.getValue().hotelId());
        assertEquals("101",                     payloadCap.getValue().roomNumber());
        assertEquals(Integer.valueOf(2),        payloadCap.getValue().capacity());
        assertEquals(new BigDecimal("999.90"),  payloadCap.getValue().pricePerNight());

    }


    @Test
    void createRoom_hotelNotFound_throwsBaseException() {
        var hotelId = UUID.randomUUID();
        when(hotelRepository.findById(hotelId)).thenReturn(Optional.empty());

        var req = new CreateRoomRequest(hotelId, 2, new BigDecimal("100.00"), "101");
        var ex = assertThrows(BaseException.class, () -> service().createRoom(req));
        assertEquals(ExceptionErrorMessage.HOTEL_NOT_FOUND, ex.getErrorMessage());

        verify(roomRepository, never()).save(any());
        verify(publisher, never()).publish(any(), anyString(), any());
    }

    @Test
    void createRoom_numberExists_throwsBaseException() {
        var hotelId = UUID.randomUUID();
        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel(hotelId)));
        when(roomRepository.existsByHotelIdAndRoomNumberIgnoreCase(hotelId, "101")).thenReturn(true);

        var req = new CreateRoomRequest(hotelId, 2, new BigDecimal("100.00"), "101");
        var ex = assertThrows(BaseException.class, () -> service().createRoom(req));
        assertEquals(ExceptionErrorMessage.ROOM_NUMBER_ALREADY_EXISTS, ex.getErrorMessage());

        verify(roomRepository, never()).save(any());
        verify(publisher, never()).publish(any(), anyString(), any());
    }

    // -------- updateRoom --------

    @Test
    void updateRoom_success_publishesEvent_andReturnsDto() {
        var hotelId = UUID.randomUUID();
        var h = hotel(hotelId);

        var roomId = UUID.randomUUID();
        var existing = new Room();
        existing.setId(roomId);
        existing.setHotel(h);
        existing.setHotelId(hotelId);
        existing.setRoomNumber("100");
        existing.setCapacity(1);
        existing.setPricePerNight(new BigDecimal("50.00"));
        existing.setCreatedDate(Instant.parse("2025-10-10T08:00:00Z"));
        existing.setUpdatedDate(Instant.parse("2025-10-10T08:00:00Z"));
        existing.setVersion(0);

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(h));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(existing));
        when(roomRepository.existsByHotelIdAndRoomNumberIgnoreCase(hotelId, "101")).thenReturn(false);

        var newUpdated = Instant.parse("2025-10-21T10:00:00Z");
        when(roomRepository.save(any(Room.class))).thenAnswer(inv -> {
            Room r = inv.getArgument(0);
            r.setUpdatedDate(newUpdated);
            r.setVersion(1);
            return r;
        });

        roomMapperStatic = mockStatic(RoomMapper.class);
        roomMapperStatic.when(() -> RoomMapper.toDto(any(Room.class)))
                .thenAnswer(inv -> {
                    Room r = inv.getArgument(0);
                    return new RoomResponse(
                            r.getId(), r.getHotelId(), r.getRoomNumber(),
                            r.getCapacity(), r.getPricePerNight(),
                            r.getCreatedDate(), r.getUpdatedDate()
                    );
                });

        var req = new UpdateRoomRequest(hotelId, new BigDecimal("120.00"), "101", 2);
        var resp = service().updateRoom(roomId, req);

        assertEquals(roomId, resp.id());
        assertEquals(hotelId, resp.hotelId());
        assertEquals("101", resp.roomNumber());
        assertEquals(Integer.valueOf(2), resp.capacity());
        assertEquals(new BigDecimal("120.00"), resp.pricePerNight());
        assertEquals(newUpdated, resp.updatedDate());

        var typeCap = ArgumentCaptor.forClass(EventType.class);
        var keyCap = ArgumentCaptor.forClass(String.class);
        var payloadCap = ArgumentCaptor.forClass(RoomUpdated.class);

        verify(publisher).publish(typeCap.capture(), keyCap.capture(), payloadCap.capture());
        assertEquals(EventType.ROOM_UPDATED, typeCap.getValue());
        assertNotNull(keyCap.getValue());
        assertEquals(payloadCap.getValue().id().toString(), keyCap.getValue());

        assertEquals(roomId,  payloadCap.getValue().id());
        assertEquals(hotelId, payloadCap.getValue().hotelId());
        assertEquals("101",  payloadCap.getValue().roomNumber());
        assertEquals(Integer.valueOf(2), payloadCap.getValue().capacity());
        assertEquals(new BigDecimal("120.00"), payloadCap.getValue().pricePerNight());
    }


    @Test
    void updateRoom_hotelNotFound_throwsBaseException() {
        var hotelId = UUID.randomUUID();
        when(hotelRepository.findById(hotelId)).thenReturn(Optional.empty());

        var ex = assertThrows(BaseException.class,
                () -> service().updateRoom(UUID.randomUUID(), new UpdateRoomRequest(hotelId, BigDecimal.TEN, "101", 2)));
        assertEquals(ExceptionErrorMessage.HOTEL_NOT_FOUND, ex.getErrorMessage());

        verify(roomRepository, never()).findById(any());
        verify(roomRepository, never()).save(any());
        verify(publisher, never()).publish(any(), anyString(), any());
    }

    @Test
    void updateRoom_roomNotFound_throwsBaseException() {
        var hotelId = UUID.randomUUID();
        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel(hotelId)));
        when(roomRepository.findById(any())).thenReturn(Optional.empty());

        var ex = assertThrows(BaseException.class,
                () -> service().updateRoom(UUID.randomUUID(), new UpdateRoomRequest(hotelId, BigDecimal.TEN, "101", 2)));
        assertEquals(ExceptionErrorMessage.ROOM_NOT_FOUND, ex.getErrorMessage());

        verify(roomRepository, never()).save(any());
        verify(publisher, never()).publish(any(), anyString(), any());
    }

    @Test
    void updateRoom_numberExists_throwsBaseException() {
        var hotelId = UUID.randomUUID();
        var h = hotel(hotelId);

        var roomId = UUID.randomUUID();
        var existing = new Room();
        existing.setId(roomId);
        existing.setHotel(h);
        existing.setRoomNumber("100");
        existing.setCapacity(1);
        existing.setPricePerNight(new BigDecimal("50.00"));

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(h));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(existing));
        when(roomRepository.existsByHotelIdAndRoomNumberIgnoreCase(hotelId, "101")).thenReturn(true);

        var ex = assertThrows(BaseException.class,
                () -> service().updateRoom(roomId, new UpdateRoomRequest(hotelId, BigDecimal.TEN, "101", 2)));
        assertEquals(ExceptionErrorMessage.ROOM_NUMBER_ALREADY_EXISTS, ex.getErrorMessage());

        verify(roomRepository, never()).save(any());
        verify(publisher, never()).publish(any(), anyString(), any());
    }

    // -------- deleteRoom --------

    @Test
    void deleteRoom_success_deletesById() {
        var roomId = UUID.randomUUID();
        var h = hotel(UUID.randomUUID());

        var existing = new Room();
        existing.setId(roomId);
        existing.setHotel(h);
        existing.setRoomNumber("100");
        existing.setCapacity(1);
        existing.setPricePerNight(new BigDecimal("50.00"));

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(existing));

        service().deleteRoom(new DeleteRoomRequest(roomId));
        verify(roomRepository).deleteById(roomId);
    }

    @Test
    void deleteRoom_notFound_throwsNoSuchElementException() {
        var roomId = UUID.randomUUID();
        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service().deleteRoom(new DeleteRoomRequest(roomId)));
        verify(roomRepository, never()).deleteById(any());
    }
}
