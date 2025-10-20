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
import com.example.hotel.entity.Room;
import com.example.hotel.mappers.RoomMapper;
import com.example.hotel.repository.HotelRepository;
import com.example.hotel.repository.RoomRepository;
import com.example.hotel.service.IRoomCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


    @Service
    @RequiredArgsConstructor
    @Slf4j
    public class RoomCommandService implements IRoomCommandService {

        private final RoomRepository roomRepository;
        private final HotelRepository hotelRepository;
        private final KafkaEventPublisher publisher;

        @Override
        @Transactional
        public RoomResponse createRoom(CreateRoomRequest request) {

           var hotel = hotelRepository.findById(request.hotelId())
                    .orElseThrow(() -> BaseException.of(ExceptionErrorMessage.HOTEL_NOT_FOUND));


            if (roomRepository.existsByHotelIdAndRoomNumberIgnoreCase(request.hotelId(), request.roomNumber())) {

                log.warn("Room creation blocked: roomNumber='{}' already exists for hotelId={}",
                        request.roomNumber(), request.hotelId());
                throw BaseException.of(ExceptionErrorMessage.ROOM_NUMBER_ALREADY_EXISTS);
            }

            var room = Room.builder()
                    .hotelId(hotel.getId())
                    .hotel(hotel)
                    .roomNumber(request.roomNumber())
                    .capacity(request.capactiy())
                    .pricePerNight(request.pricePerNight())
                    .build();

            var savedRoom = roomRepository.save(room);

            publisher.publish(
                    EventType.ROOM_CREATED,
                    room.getId().toString(),
                    new RoomCreated(
                            savedRoom.getId(),
                            savedRoom.getHotelId(),
                            savedRoom.getRoomNumber(),
                            savedRoom.getCapacity(),
                            savedRoom.getPricePerNight())
            );

            log.info("Room created successfully: id={}, roomNumber='{}'", savedRoom.getId(), savedRoom.getRoomNumber());

            return RoomMapper.toDto(savedRoom);
        }

        @Override
        @Transactional
        public RoomResponse updateRoom(UUID roomId, UpdateRoomRequest request) {

            var hotel = hotelRepository.findById(request.hotelId())
                    .orElseThrow(() -> BaseException.of(ExceptionErrorMessage.HOTEL_NOT_FOUND));


            var room = roomRepository.findById(roomId)
                    .orElseThrow(() -> BaseException.of(ExceptionErrorMessage.ROOM_NOT_FOUND));


            boolean numberChanging = !room.getRoomNumber().equalsIgnoreCase(request.roomNumber())
                    || !room.getHotelId().equals(request.hotelId());

            if (numberChanging &&
                    roomRepository.existsByHotelIdAndRoomNumberIgnoreCase(request.hotelId(), request.roomNumber())) {
                log.warn("Room update blocked: roomNumber='{}' already exists for hotelId={}",
                        request.roomNumber(), request.hotelId());

                throw BaseException.of(ExceptionErrorMessage.ROOM_NUMBER_ALREADY_EXISTS);
            }
            room.setHotel(hotel);
            room.setRoomNumber(request.roomNumber());
            room.setCapacity(request.capacity());
            room.setPricePerNight(request.pricePerNight());

            var updatedRoom = roomRepository.save(room);

          publisher.publish(
                    EventType.ROOM_UPDATED,
                    room.getId().toString(),
                    new RoomUpdated(
                            updatedRoom.getId(),
                            updatedRoom.getHotelId(),
                            updatedRoom.getRoomNumber(),
                            updatedRoom.getCapacity(),
                            updatedRoom.getPricePerNight()));


            log.info("Room updated successfully: id={}, roomNumber='{}'", updatedRoom.getId(), updatedRoom.getRoomNumber());

            return RoomMapper.toDto(updatedRoom);
        }

        @Override
        @Transactional
        public void deleteRoom(DeleteRoomRequest request) {
            var room = roomRepository.findById(request.id())
                    .orElseThrow();

            roomRepository.deleteById(room.getId());
        }
    }
