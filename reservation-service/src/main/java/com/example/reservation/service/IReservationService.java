package com.example.reservation.service;

import com.example.common.page.OrderDirection;
import com.example.reservation.dto.ReservationRequest;
import com.example.reservation.dto.ReservationResponse;

import org.springframework.data.domain.Page;

import java.util.UUID;

public interface IReservationService {

    ReservationResponse create(ReservationRequest request, String email);
    Page<ReservationResponse> listMyReservations(int page, int size, String OrderBy, OrderDirection orderDirection, UUID userId);

}
