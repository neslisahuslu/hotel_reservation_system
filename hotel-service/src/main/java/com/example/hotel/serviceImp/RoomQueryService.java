package com.example.hotel.serviceImp;

import com.example.common.exception.BaseException;
import com.example.common.exception.ExceptionErrorMessage;
import com.example.common.page.OrderDirection;
import com.example.hotel.dto.response.RoomResponse;
import com.example.hotel.entity.Room;
import com.example.hotel.mappers.RoomMapper;
import com.example.hotel.repository.RoomRepository;
import com.example.hotel.service.IRoomQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomQueryService implements IRoomQueryService {

    private final RoomRepository roomRepository;

    @Override
    public RoomResponse getRoomById(UUID id) {

        log.info("Query get hotel by id={}", id);
        var room = roomRepository.findById(id)
                .orElseThrow(() -> BaseException.of(ExceptionErrorMessage.HOTEL_NOT_FOUND));
        return RoomMapper.toDto(room);
    }

    @Override
    public Page<RoomResponse> getAllRooms(int page, int size, String orderBy, OrderDirection orderDirection) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Room> rooms = roomRepository.findAll(pageable);
        return RoomMapper.toPagedDto(rooms);
    }


}
