package com.example.reservation.controller;

import com.example.common.page.OrderDirection;
import com.example.reservation.dto.ReservationRequest;
import com.example.reservation.dto.ReservationResponse;
import com.example.reservation.service.IReservationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;


@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/reservations")
public class ReservationController {

    private final IReservationService reservationService;

    @GetMapping
    public ResponseEntity<Page<ReservationResponse>> listMyReservations(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "orderBy", required = false, defaultValue = "checkInDate") String orderBy,
            @RequestParam(name = "orderDirection", required = false, defaultValue = "ASC") OrderDirection orderDirection,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getClaim("userId"));
        Page<ReservationResponse> response = reservationService.listMyReservations(page, size, orderBy, orderDirection, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> create(
            @Valid @RequestBody ReservationRequest request,
            @RequestParam("email") @Email String email) {
        var savedReservation = reservationService.create(request,email);
        return ResponseEntity.ok(savedReservation);
    }

}
