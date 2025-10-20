package com.example.hotel.serviceImp;

import com.example.common.event.EventType;
import com.example.common.event.KafkaEventPublisher;
import com.example.common.event.payload.HotelCreated;
import com.example.common.event.payload.HotelUpdated;
import com.example.common.exception.BaseException;
import com.example.common.exception.ExceptionErrorMessage;
import com.example.hotel.dto.request.hotel.CreateHotelRequest;
import com.example.hotel.dto.request.hotel.DeleteHotelRequest;
import com.example.hotel.dto.response.HotelResponse;
import com.example.hotel.dto.request.hotel.UpdateHotelRequest;
import com.example.hotel.entity.Hotel;
import com.example.hotel.mappers.HotelMapper;
import com.example.hotel.repository.HotelRepository;
import com.example.hotel.service.IHotelCommandService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class HotelCommandService implements IHotelCommandService {

    private final KafkaEventPublisher publisher;
    private final HotelRepository hotelRepository;

    @Override
    @Transactional
    public HotelResponse createHotel(CreateHotelRequest request) {

        if (hotelRepository.existsByNameIgnoreCase(request.name())) {
            log.warn("Hotel creation failed – name '{}' already exists", request.name());
            throw BaseException.of(ExceptionErrorMessage.HOTEL_NAME_ALREADY_EXISTS);
        }

        var hotel = Hotel.builder()
                .name(request.name())
                .address(request.address())
                .starRating(request.starRating())
                .build();

        var savedHotel = hotelRepository.save(hotel);

        publisher.publish(
                EventType.HOTEL_CREATED,
                savedHotel.getId().toString(),
                new HotelCreated(savedHotel.getId(), savedHotel.getName(), savedHotel.getAddress())
        );
        log.info("Hotel created successfully: id={}, name='{}'", savedHotel.getId(), savedHotel.getName());

        return HotelMapper.toDto(savedHotel);
    }

    @Override
    @Transactional
    public HotelResponse updateHotel(UUID hotelId, UpdateHotelRequest request) {

        var hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> BaseException.of(ExceptionErrorMessage.HOTEL_NOT_FOUND));


        if (!hotel.getName().equalsIgnoreCase(request.name())
                && hotelRepository.existsByNameIgnoreCase(request.name())) {
            log.warn("Hotel update failed – new name '{}' already exists", request.name());
            throw BaseException.of(ExceptionErrorMessage.HOTEL_NAME_ALREADY_EXISTS);
        }

        hotel.setName(request.name());
        hotel.setAddress(request.address());
        hotel.setStarRating(request.starRating());

        var updatedHotel = hotelRepository.save(hotel);

        publisher.publish(
                EventType.HOTEL_UPDATED,
                hotel.getId().toString(),
                new HotelUpdated(updatedHotel.getId(), updatedHotel.getName(), updatedHotel.getAddress())
        );

        log.info("Hotel updated successfully: id={}, newVersion={}", updatedHotel.getId(), updatedHotel.getVersion());

        return HotelMapper.toDto(updatedHotel);
    }

    @Override
    @Transactional
    public void deleteHotel(DeleteHotelRequest request) {

        var hotel = hotelRepository.findById(request.id())
                .orElseThrow(() -> BaseException.of(ExceptionErrorMessage.HOTEL_NOT_FOUND));

        hotelRepository.deleteById(hotel.getId());
        log.info("Hotel deleted successfully: id={}, name='{}'", hotel.getId(), hotel.getName());
    }
}

