package com.example.reservation.serviceImp;

import com.example.common.event.EventType;
import com.example.common.event.KafkaEventPublisher;
import com.example.common.event.payload.ReservationCreated;
import com.example.common.page.OrderDirection;
import com.example.reservation.dto.ReservationRequest;
import com.example.reservation.dto.ReservationResponse;
import com.example.reservation.entity.HotelCache;
import com.example.reservation.entity.Reservation;
import com.example.reservation.entity.RoomCache;
import com.example.reservation.mapper.ReservationMapper;
import com.example.reservation.repository.ReservationRepository;
import com.example.reservation.service.IReservationService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationService implements IReservationService {

    private final ReservationRepository repository;
    private final KafkaEventPublisher publisher;
    private final EntityManager entityManager;


    @Override
    @Transactional
    public ReservationResponse create(ReservationRequest request, String email) {

        var hotelRef = entityManager.getReference(HotelCache.class, request.hotelId());
        var roomRef = entityManager.getReference(RoomCache.class, request.roomId());

        var reservation = Reservation.builder()
                .hotel(hotelRef)
                .room(roomRef)
                .userId(request.userId())
                .guestName(request.guestName())
                .checkInDate(request.checkInDate())
                .checkOutDate(request.checkOutDate())
                .build();

        var savedReservation = repository.save(reservation);
        publisher.publish(
                EventType.RESERVATION_CREATED,
                reservation.getId().toString(),
                new ReservationCreated(
                        savedReservation.getId(),
                        savedReservation.getHotel().getId(),
                        savedReservation.getRoom().getId(),
                        savedReservation.getGuestName(),
                        email,
                        savedReservation.getCheckInDate(),
                        savedReservation.getCheckOutDate())
        );

        return ReservationMapper.toDto(savedReservation);

    }



    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponse> listMyReservations(int page, int size, String orderBy, OrderDirection orderDirection, UUID userId) {

        Sort.Direction dir = (orderDirection == OrderDirection.DESC)
                ? Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, orderBy));

        var pageEntities = repository.findByUserId(userId, pageable);

        return ReservationMapper.toPagedDto(pageEntities);
    }

}
