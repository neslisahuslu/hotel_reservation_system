package com.example.hotel.service;

import com.example.hotel.dto.request.hotel.CreateHotelRequest;
import com.example.hotel.dto.request.hotel.DeleteHotelRequest;
import com.example.hotel.dto.response.HotelResponse;
import com.example.hotel.dto.request.hotel.UpdateHotelRequest;

import java.util.UUID;

public interface IHotelCommandService {

    HotelResponse createHotel( CreateHotelRequest request);
    HotelResponse updateHotel(UUID hotelId, UpdateHotelRequest request);
    void deleteHotel( DeleteHotelRequest request);
}
