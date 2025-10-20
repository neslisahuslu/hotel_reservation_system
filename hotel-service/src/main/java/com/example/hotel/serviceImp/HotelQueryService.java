package com.example.hotel.serviceImp;

import com.example.common.exception.BaseException;
import com.example.common.exception.ExceptionErrorMessage;
import com.example.common.page.OrderDirection;
import com.example.hotel.dto.response.HotelResponse;
import com.example.hotel.entity.Hotel;
import com.example.hotel.mappers.HotelMapper;
import com.example.hotel.repository.HotelRepository;
import com.example.hotel.service.IHotelQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class HotelQueryService implements IHotelQueryService {

    private final HotelRepository hotelRepository;

    @Override
    @Transactional(readOnly = true)
    public HotelResponse getHotelById(UUID id) {
        log.info("Query get hotel by id={}", id);
        var hotel = hotelRepository.findById(id)
                .orElseThrow(() -> BaseException.of(ExceptionErrorMessage.HOTEL_NOT_FOUND));
        return HotelMapper.toDto(hotel);
    }

    @Override
    public Page<HotelResponse> getAllHotels(int page, int size, String orderBy, OrderDirection orderDirection) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Hotel> hotels = hotelRepository.findAll(pageable);
        return HotelMapper.toPagedDto(hotels);
    }


}
