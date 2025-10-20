package com.example.hotel.serviceImp;

import com.example.common.exception.BaseException;
import com.example.common.exception.ExceptionErrorMessage;
import com.example.common.page.OrderDirection;
import com.example.hotel.dto.response.RoomResponse;
import com.example.hotel.entity.Room;
import com.example.hotel.mappers.RoomMapper;
import com.example.hotel.repository.RoomRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomQueryServiceTest {

    @Mock
    RoomRepository roomRepository;

    private MockedStatic<RoomMapper> roomMapperStatic;

    @AfterEach
    void tearDown() {
        if (roomMapperStatic != null) {
            roomMapperStatic.close();
            roomMapperStatic = null;
        }
    }

    private RoomQueryService service() {
        return new RoomQueryService(roomRepository);
    }

    // ---------- getRoomById ----------

    @Test
    void getRoomById_success_returnsDto() {
        var id = UUID.randomUUID();

        var room = new Room();
        room.setId(id);
        room.setHotelId(UUID.randomUUID());
        room.setRoomNumber("101");
        room.setCapacity(2);
        room.setPricePerNight(new BigDecimal("123.45"));
        room.setCreatedDate(Instant.parse("2025-10-10T10:00:00Z"));
        room.setUpdatedDate(Instant.parse("2025-10-10T10:00:00Z"));
        room.setVersion(1);

        when(roomRepository.findById(id)).thenReturn(Optional.of(room));

        var expected = new RoomResponse(
                room.getId(),
                room.getHotelId(),
                room.getRoomNumber(),
                room.getCapacity(),
                room.getPricePerNight(),
                room.getCreatedDate(),
                room.getUpdatedDate()
        );

        roomMapperStatic = mockStatic(RoomMapper.class);
        roomMapperStatic.when(() -> RoomMapper.toDto(room)).thenReturn(expected);

        var resp = service().getRoomById(id);

        assertEquals(expected, resp);
        verify(roomRepository).findById(id);
    }

    @Test
    void getRoomById_notFound_throwsBaseException() {
        var id = UUID.randomUUID();
        when(roomRepository.findById(id)).thenReturn(Optional.empty());

        var ex = assertThrows(BaseException.class, () -> service().getRoomById(id));
        // Serviste şu an HOTEL_NOT_FOUND fırlatılıyor (ROOM_NOT_FOUND daha anlamlı olabilir),
        // mevcut davranışı test ediyoruz:
        assertEquals(ExceptionErrorMessage.HOTEL_NOT_FOUND, ex.getErrorMessage());

        verify(roomRepository).findById(id);
    }

    // ---------- getAllRooms ----------

    @Test
    void getAllRooms_returnsMappedPage() {
        int page = 0, size = 2;
        Pageable pageable = PageRequest.of(page, size);

        var r1 = new Room();
        r1.setId(UUID.randomUUID());
        r1.setHotelId(UUID.randomUUID());
        r1.setRoomNumber("101");
        r1.setCapacity(2);
        r1.setPricePerNight(new BigDecimal("100.00"));
        r1.setCreatedDate(Instant.parse("2025-10-01T09:00:00Z"));
        r1.setUpdatedDate(Instant.parse("2025-10-02T09:00:00Z"));
        r1.setVersion(1);

        var r2 = new Room();
        r2.setId(UUID.randomUUID());
        r2.setHotelId(UUID.randomUUID());
        r2.setRoomNumber("102");
        r2.setCapacity(3);
        r2.setPricePerNight(new BigDecimal("150.00"));
        r2.setCreatedDate(Instant.parse("2025-10-03T09:00:00Z"));
        r2.setUpdatedDate(Instant.parse("2025-10-04T09:00:00Z"));
        r2.setVersion(1);

        Page<Room> roomPage = new PageImpl<>(List.of(r1, r2), pageable, 2);
        when(roomRepository.findAll(pageable)).thenReturn(roomPage);

        var d1 = new RoomResponse(r1.getId(), r1.getHotelId(), "101", 2, new BigDecimal("100.00"), r1.getCreatedDate(), r1.getUpdatedDate());
        var d2 = new RoomResponse(r2.getId(), r2.getHotelId(), "102", 3, new BigDecimal("150.00"), r2.getCreatedDate(), r2.getUpdatedDate());
        Page<RoomResponse> expected = new PageImpl<>(List.of(d1, d2), pageable, 2);

        roomMapperStatic = mockStatic(RoomMapper.class);
        roomMapperStatic.when(() -> RoomMapper.toPagedDto(roomPage)).thenReturn(expected);

        var resp = service().getAllRooms(page, size, "ignored", OrderDirection.ASC);

        assertEquals(2, resp.getTotalElements());
        assertEquals(2, resp.getContent().size());
        assertEquals(d1, resp.getContent().get(0));
        assertEquals(d2, resp.getContent().get(1));

        verify(roomRepository).findAll(pageable);
    }
}
