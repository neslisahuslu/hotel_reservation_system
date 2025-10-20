package com.example.hotel.service;

import com.example.common.page.OrderDirection;
import com.example.hotel.dto.response.HotelResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface IHotelQueryService {
    HotelResponse getHotelById(UUID id);
    Page<HotelResponse> getAllHotels(int page, int size,String orderBy, OrderDirection orderDirection);
}
