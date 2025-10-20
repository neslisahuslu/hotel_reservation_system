package com.example.common.exception;


import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.http.HttpStatus;

import java.util.Arrays;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ExceptionErrorMessage {

    HOTEL_NAME_ALREADY_EXISTS("Hotel name already exists", HttpStatus.BAD_REQUEST),
    HOTEL_NOT_FOUND("Hotel not found.", HttpStatus.BAD_REQUEST),
    ROOM_NUMBER_ALREADY_EXISTS("Room number already exists.", HttpStatus.BAD_REQUEST),
    ROOM_NOT_FOUND("Room not found.", HttpStatus.BAD_REQUEST),
    ROOM_NAME_ALREADY_EXISTS("Room name already exists.", HttpStatus.BAD_REQUEST),

    //GENERIC
    INTERNAL_SERVER_ERROR("Something wrong happened",HttpStatus.INTERNAL_SERVER_ERROR),
    BAD_REQUEST("Bad request",HttpStatus.BAD_REQUEST);

    ExceptionErrorMessage(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }

    private final String message;
    private final HttpStatus httpStatus;

    public static HttpStatus getHttpStatus(ExceptionErrorMessage errorMessage) {
        return Arrays.stream(ExceptionErrorMessage.values())
                .filter(element -> element.equals(errorMessage))
                .findFirst()
                .map(element -> element.httpStatus)
                .orElse(null);

    }

    public static String getMessage(ExceptionErrorMessage errorMessage) {
        return Arrays.stream(ExceptionErrorMessage.values())
                .filter(element -> element.equals(errorMessage))
                .findFirst()
                .map(element -> element.message)
                .orElse(null);
    }

    public static ExceptionErrorMessage getFromHttpStatus(HttpStatus code) {
        return Arrays.stream(ExceptionErrorMessage.values())
                .filter(element -> element.httpStatus.equals(code))
                .findFirst()
                .orElse(INTERNAL_SERVER_ERROR);
    }
}

