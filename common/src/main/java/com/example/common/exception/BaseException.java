package com.example.common.exception;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {
    private final ExceptionErrorMessage errorMessage;

    public BaseException(ExceptionErrorMessage errorMessage) {
        super();
        this.errorMessage = errorMessage;
    }

    public static BaseException of(ExceptionErrorMessage errorMessage) {
        return new BaseException(errorMessage);
    }

}