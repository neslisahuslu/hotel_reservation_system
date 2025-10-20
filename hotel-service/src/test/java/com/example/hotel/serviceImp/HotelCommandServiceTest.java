package com.example.hotel.serviceImp;

import com.example.common.event.EventType;
import com.example.common.event.KafkaEventPublisher;
import com.example.common.event.payload.HotelCreated;
import com.example.common.event.payload.HotelUpdated;
import com.example.common.exception.BaseException;
import com.example.common.exception.ExceptionErrorMessage;
import com.example.hotel.dto.request.hotel.CreateHotelRequest;
import com.example.hotel.dto.request.hotel.DeleteHotelRequest;
import com.example.hotel.dto.request.hotel.UpdateHotelRequest;
import com.example.hotel.dto.response.HotelResponse;
import com.example.hotel.entity.Hotel;
import com.example.hotel.mappers.HotelMapper;
import com.example.hotel.repository.HotelRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelCommandServiceTest {

    @Mock
    KafkaEventPublisher publisher;

    @Mock
    HotelRepository hotelRepository;

    private MockedStatic<HotelMapper> hotelMapperStatic;

    @AfterEach
    void tearDown() {
        if (hotelMapperStatic != null) {
            hotelMapperStatic.close();
            hotelMapperStatic = null;
        }
    }

    private HotelCommandService service() {
        return new HotelCommandService(publisher, hotelRepository);
    }

    // -------- createHotel --------

    @Test
    void createHotel_success_publishesEvent_andReturnsDto() {
        // given
        var req = new CreateHotelRequest("Hilton", "Istanbul", 5);
        when(hotelRepository.existsByNameIgnoreCase("Hilton")).thenReturn(false);

        var id = UUID.randomUUID();
        var created = Instant.parse("2025-10-20T08:00:00Z");
        var updated = created;

        // JPA davranışını taklit et: save edilen *aynı instance*'a id/created/updated/version yaz
        when(hotelRepository.save(any(Hotel.class))).thenAnswer(inv -> {
            Hotel h = inv.getArgument(0);
            h.setId(id);
            h.setCreatedDate(created);
            h.setUpdatedDate(updated);
            h.setVersion(0);
            return h; // aynı referansı döndür
        });

        // Mapper'ı argümana göre stubla
        hotelMapperStatic = mockStatic(HotelMapper.class);
        hotelMapperStatic.when(() -> HotelMapper.toDto(any(Hotel.class)))
                .thenAnswer(inv -> {
                    Hotel h = inv.getArgument(0);
                    return new HotelResponse(
                            h.getId(),
                            h.getName(),
                            h.getAddress(),
                            h.getStarRating(),   // Integer
                            h.getCreatedDate(),
                            h.getUpdatedDate()
                    );
                });

        var sut = service();

        // when
        var resp = sut.createHotel(req);

        // then - response doğrulamaları
        assertNotNull(resp);
        assertEquals(id, resp.id());
        assertEquals("Hilton", resp.name());
        assertEquals("Istanbul", resp.address());
        assertEquals(Integer.valueOf(5), resp.starRating());
        assertEquals(created, resp.createdDate());
        assertEquals(updated, resp.updatedDate());

        // event publish doğrulaması
        ArgumentCaptor<EventType> typeCap = ArgumentCaptor.forClass(EventType.class);
        ArgumentCaptor<String> keyCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HotelCreated> payloadCap = ArgumentCaptor.forClass(HotelCreated.class);

        verify(publisher).publish(typeCap.capture(), keyCap.capture(), payloadCap.capture());
        assertEquals(EventType.HOTEL_CREATED, typeCap.getValue());
        assertEquals(id.toString(), keyCap.getValue()); // artık null değil
        assertEquals(id, payloadCap.getValue().hotelId());
        assertEquals("Hilton", payloadCap.getValue().name());
        assertEquals("Istanbul", payloadCap.getValue().address());

        verify(hotelRepository).existsByNameIgnoreCase("Hilton");
        verify(hotelRepository).save(any(Hotel.class));
    }


    @Test
    void createHotel_nameAlreadyExists_throwsBaseException() {
        // given
        var req = new CreateHotelRequest("Hilton", "Istanbul", 5);
        when(hotelRepository.existsByNameIgnoreCase("Hilton")).thenReturn(true);

        var sut = service();

        // when & then
        var ex = assertThrows(BaseException.class, () -> sut.createHotel(req));
        assertEquals(ExceptionErrorMessage.HOTEL_NAME_ALREADY_EXISTS, ex.getErrorMessage());

        verify(hotelRepository, never()).save(any());
        verify(publisher, never()).publish(any(), anyString(), any());
    }

    // -------- updateHotel --------

    @Test
    void updateHotel_success_publishesEvent_andReturnsDto() {
        // given
        var id = UUID.randomUUID();
        var req = new UpdateHotelRequest("NewName", "NewAddress", 4);

        var existing = new Hotel();
        existing.setId(id);
        existing.setName("OldName");
        existing.setAddress("OldAddress");
        existing.setStarRating(3);
        existing.setCreatedDate(Instant.parse("2025-10-10T10:00:00Z"));
        existing.setUpdatedDate(Instant.parse("2025-10-10T10:00:00Z"));
        existing.setVersion(0);

        when(hotelRepository.findById(id)).thenReturn(Optional.of(existing));
        when(hotelRepository.existsByNameIgnoreCase("NewName")).thenReturn(false);

        var updatedEntity = new Hotel();
        updatedEntity.setId(id);
        updatedEntity.setName("NewName");
        updatedEntity.setAddress("NewAddress");
        updatedEntity.setStarRating(4);
        updatedEntity.setCreatedDate(existing.getCreatedDate());
        updatedEntity.setUpdatedDate(Instant.parse("2025-10-20T09:00:00Z"));
        updatedEntity.setVersion(1);

        when(hotelRepository.save(any(Hotel.class))).thenReturn(updatedEntity);

        var expectedDto = new HotelResponse(
                id, "NewName", "NewAddress", Integer.valueOf(4),
                updatedEntity.getCreatedDate(), updatedEntity.getUpdatedDate()
        );
        hotelMapperStatic = mockStatic(HotelMapper.class);
        hotelMapperStatic.when(() -> HotelMapper.toDto(updatedEntity)).thenReturn(expectedDto);

        var sut = service();

        // when
        var resp = sut.updateHotel(id, req);

        // then
        assertEquals(expectedDto, resp);

        ArgumentCaptor<HotelUpdated> payloadCap = ArgumentCaptor.forClass(HotelUpdated.class);
        verify(publisher).publish(eq(EventType.HOTEL_UPDATED), eq(id.toString()), payloadCap.capture());
        assertEquals(id, payloadCap.getValue().hotelId());
        assertEquals("NewName", payloadCap.getValue().name());
        assertEquals("NewAddress", payloadCap.getValue().address());

        verify(hotelRepository).findById(id);
        verify(hotelRepository).existsByNameIgnoreCase("NewName");
        verify(hotelRepository).save(any(Hotel.class));
    }

    @Test
    void updateHotel_notFound_throwsBaseException() {
        var id = UUID.randomUUID();
        when(hotelRepository.findById(id)).thenReturn(Optional.empty());

        var sut = service();

        var ex = assertThrows(BaseException.class,
                () -> sut.updateHotel(id, new UpdateHotelRequest("x", "y", 3)));
        assertEquals(ExceptionErrorMessage.HOTEL_NOT_FOUND, ex.getErrorMessage());

        verify(hotelRepository, never()).save(any());
        verify(publisher, never()).publish(any(), anyString(), any());
    }

    @Test
    void updateHotel_newNameAlreadyExists_throwsBaseException() {
        var id = UUID.randomUUID();

        var existing = new Hotel();
        existing.setId(id);
        existing.setName("Old");
        existing.setAddress("Addr");
        existing.setStarRating(3);
        existing.setCreatedDate(Instant.parse("2025-10-10T10:00:00Z"));
        existing.setUpdatedDate(Instant.parse("2025-10-10T10:00:00Z"));
        existing.setVersion(0);

        when(hotelRepository.findById(id)).thenReturn(Optional.of(existing));
        when(hotelRepository.existsByNameIgnoreCase("Duplicate")).thenReturn(true);

        var sut = service();

        var ex = assertThrows(BaseException.class,
                () -> sut.updateHotel(id, new UpdateHotelRequest("Duplicate", "Addr", 3)));
        assertEquals(ExceptionErrorMessage.HOTEL_NAME_ALREADY_EXISTS, ex.getErrorMessage());

        verify(hotelRepository, never()).save(any());
        verify(publisher, never()).publish(any(), anyString(), any());
    }

    // -------- deleteHotel --------

    @Test
    void deleteHotel_success_deletesById() {
        var id = UUID.randomUUID();

        var existing = new Hotel();
        existing.setId(id);
        existing.setName("ToDelete");
        existing.setAddress("Addr");
        existing.setStarRating(3);
        existing.setCreatedDate(Instant.parse("2025-10-10T10:00:00Z"));
        existing.setUpdatedDate(Instant.parse("2025-10-10T10:00:00Z"));
        existing.setVersion(0);

        when(hotelRepository.findById(id)).thenReturn(Optional.of(existing));

        var sut = service();

        sut.deleteHotel(new DeleteHotelRequest(id));

        verify(hotelRepository).deleteById(id);
        verify(publisher, never()).publish(any(), anyString(), any());
    }

    @Test
    void deleteHotel_notFound_throwsBaseException() {
        var id = UUID.randomUUID();
        when(hotelRepository.findById(id)).thenReturn(Optional.empty());

        var sut = service();

        var ex = assertThrows(BaseException.class, () -> sut.deleteHotel(new DeleteHotelRequest(id)));
        assertEquals(ExceptionErrorMessage.HOTEL_NOT_FOUND, ex.getErrorMessage());

        verify(hotelRepository, never()).deleteById(any());
        verify(publisher, never()).publish(any(), anyString(), any());
    }
}
