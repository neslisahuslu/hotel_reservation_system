package com.example.hotel.serviceImp;

import com.example.common.exception.BaseException;
import com.example.common.exception.ExceptionErrorMessage;
import com.example.common.page.OrderDirection;
import com.example.hotel.dto.response.HotelResponse;
import com.example.hotel.entity.Hotel;
import com.example.hotel.mappers.HotelMapper;
import com.example.hotel.repository.HotelRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelQueryServiceTest {

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

    private HotelQueryService service() {
        return new HotelQueryService(hotelRepository);
    }

    // ---------- getHotelById ----------

    @Test
    void getHotelById_success_returnsDto() {
        var id = UUID.randomUUID();

        var hotel = new Hotel();
        hotel.setId(id);
        hotel.setName("Hilton");
        hotel.setAddress("Istanbul");
        hotel.setStarRating(5);
        hotel.setCreatedDate(Instant.parse("2025-10-10T10:00:00Z"));
        hotel.setUpdatedDate(Instant.parse("2025-10-10T10:00:00Z"));
        hotel.setVersion(1);

        when(hotelRepository.findById(id)).thenReturn(Optional.of(hotel));

        var expected = new HotelResponse(
                id, "Hilton", "Istanbul", Integer.valueOf(5),
                hotel.getCreatedDate(), hotel.getUpdatedDate()
        );

        hotelMapperStatic = mockStatic(HotelMapper.class);
        hotelMapperStatic.when(() -> HotelMapper.toDto(hotel)).thenReturn(expected);

        var resp = service().getHotelById(id);

        assertEquals(expected, resp);
        verify(hotelRepository).findById(id);
    }

    @Test
    void getHotelById_notFound_throwsBaseException() {
        var id = UUID.randomUUID();
        when(hotelRepository.findById(id)).thenReturn(Optional.empty());

        var ex = assertThrows(BaseException.class, () -> service().getHotelById(id));
        assertEquals(ExceptionErrorMessage.HOTEL_NOT_FOUND, ex.getErrorMessage());

        verify(hotelRepository).findById(id);
    }

    // ---------- getAllHotels ----------

    @Test
    void getAllHotels_returnsMappedPage() {
        int page = 0, size = 2;
        Pageable pageable = PageRequest.of(page, size);

        // repo’dan dönecek entity sayfası
        var h1 = new Hotel();
        h1.setId(UUID.randomUUID());
        h1.setName("A");
        h1.setAddress("AddrA");
        h1.setStarRating(4);
        h1.setCreatedDate(Instant.parse("2025-10-01T09:00:00Z"));
        h1.setUpdatedDate(Instant.parse("2025-10-02T09:00:00Z"));
        h1.setVersion(1);

        var h2 = new Hotel();
        h2.setId(UUID.randomUUID());
        h2.setName("B");
        h2.setAddress("AddrB");
        h2.setStarRating(5);
        h2.setCreatedDate(Instant.parse("2025-10-03T09:00:00Z"));
        h2.setUpdatedDate(Instant.parse("2025-10-04T09:00:00Z"));
        h2.setVersion(1);

        Page<Hotel> hotelPage = new PageImpl<>(List.of(h1, h2), pageable, 2);
        when(hotelRepository.findAll(pageable)).thenReturn(hotelPage);

        // mapper’dan dönecek DTO sayfası (doğrudan beklenen sonucu stub’la)
        var r1 = new HotelResponse(h1.getId(), "A", "AddrA", Integer.valueOf(4), h1.getCreatedDate(), h1.getUpdatedDate());
        var r2 = new HotelResponse(h2.getId(), "B", "AddrB", Integer.valueOf(5), h2.getCreatedDate(), h2.getUpdatedDate());
        Page<HotelResponse> expected = new PageImpl<>(List.of(r1, r2), pageable, 2);

        hotelMapperStatic = mockStatic(HotelMapper.class);
        hotelMapperStatic.when(() -> HotelMapper.toPagedDto(hotelPage)).thenReturn(expected);

        var resp = service().getAllHotels(page, size, "ignored", OrderDirection.ASC);

        assertEquals(2, resp.getTotalElements());
        assertEquals(2, resp.getContent().size());
        assertEquals(r1, resp.getContent().get(0));
        assertEquals(r2, resp.getContent().get(1));

        verify(hotelRepository).findAll(pageable);
    }
}
