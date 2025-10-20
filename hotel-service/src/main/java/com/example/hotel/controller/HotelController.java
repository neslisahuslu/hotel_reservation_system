package com.example.hotel.controller;

import com.example.common.page.OrderDirection;
import com.example.hotel.dto.request.hotel.CreateHotelRequest;
import com.example.hotel.dto.request.hotel.DeleteHotelRequest;
import com.example.hotel.dto.response.HotelResponse;
import com.example.hotel.dto.request.hotel.UpdateHotelRequest;
import com.example.hotel.service.IHotelCommandService;
import com.example.hotel.service.IHotelQueryService;
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
@RequestMapping("/api/hotels")
public class HotelController {

    private final IHotelCommandService commandService;
    private final IHotelQueryService queryService;

    @PostMapping
    public ResponseEntity<HotelResponse> createHotel(@RequestBody @Valid CreateHotelRequest request) {
        var response = commandService.createHotel(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{hotelId}")
    public ResponseEntity<HotelResponse> updateHotel(
            @PathVariable ("hotelId") UUID hotelId,
            @RequestBody @Valid UpdateHotelRequest request
    ) {
        var response = commandService.updateHotel(hotelId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<HotelResponse> deleteHotel(@RequestBody @Valid DeleteHotelRequest request) {
        commandService.deleteHotel(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{hotelId}")
    public ResponseEntity<HotelResponse> getHotelById(@PathVariable("hotelId") UUID hotelId) {
        var response = queryService.getHotelById(hotelId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<HotelResponse>> getAllHotels(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "orderBy", required = false, defaultValue = "name") String orderBy,
            @RequestParam(name = "orderDirection", required = false, defaultValue = "ASC") OrderDirection orderDirection
    ) {
        var response = queryService.getAllHotels(page, size, orderBy, orderDirection);
        return ResponseEntity.ok(response);
    }
}