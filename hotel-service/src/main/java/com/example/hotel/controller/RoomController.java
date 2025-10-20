package com.example.hotel.controller;

import com.example.common.page.OrderDirection;
import com.example.hotel.dto.request.room.CreateRoomRequest;
import com.example.hotel.dto.request.room.DeleteRoomRequest;
import com.example.hotel.dto.request.room.UpdateRoomRequest;
import com.example.hotel.dto.response.RoomResponse;
import com.example.hotel.service.IRoomCommandService;
import com.example.hotel.service.IRoomQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/rooms")
public class RoomController {

    private final IRoomCommandService commandService;
    private final IRoomQueryService queryService;

    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(@RequestBody @Valid CreateRoomRequest request) {
        var response = commandService.createRoom(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{roomId}")
    public ResponseEntity<RoomResponse> updateRoom(
            @PathVariable ("roomId")UUID roomId,
            @RequestBody @Valid UpdateRoomRequest request
    ) {
        var response = commandService.updateRoom(roomId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<RoomResponse> deleteRoom(@RequestBody @Valid DeleteRoomRequest request) {
        commandService.deleteRoom(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<RoomResponse> getRoomById(@PathVariable ("roomId") UUID roomId) {
        var response = queryService.getRoomById(roomId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<RoomResponse>> getAllRooms(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "orderBy", required = false, defaultValue = "name") String orderBy,
            @RequestParam(name = "orderDirection", required = false, defaultValue = "ASC") OrderDirection orderDirection
    ) {

        var response = queryService.getAllRooms(page, size, orderBy, orderDirection);
        return ResponseEntity.ok(response);
    }
}