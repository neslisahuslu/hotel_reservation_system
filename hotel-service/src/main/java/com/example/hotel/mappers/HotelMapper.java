package com.example.hotel.mappers;

import com.example.hotel.dto.response.HotelResponse;
import com.example.hotel.entity.Hotel;
import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Page;

@UtilityClass
public class HotelMapper {

    public static HotelResponse toDto(Hotel hotel) {
        return HotelResponse.of(
                hotel.getId(),
                hotel.getName(),
                hotel.getAddress(),
                hotel.getStarRating(),
                hotel.getCreatedDate(),
                hotel.getUpdatedDate());
    }

    public static Page<HotelResponse> toPagedDto(Page<Hotel> hotels) {
        return hotels.map(hotel ->
                new HotelResponse(
                        hotel.getId(),
                        hotel.getName(),
                        hotel.getAddress(),
                        hotel.getStarRating(),
                        hotel.getCreatedDate(),
                        hotel.getUpdatedDate()
                )
        );
    }
}