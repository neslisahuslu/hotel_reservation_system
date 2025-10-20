package com.example.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorDto> handleBaseException(BaseException exception) {
        final ExceptionErrorMessage errorMessage = exception.getErrorMessage();
        final HttpStatus status = ExceptionErrorMessage.getHttpStatus(errorMessage);
        final ErrorDto body = createErrorDto(errorMessage);
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(value = {
            MethodArgumentTypeMismatchException.class,
    })

    public ResponseEntity<ErrorDto> handleExceptions(MethodArgumentTypeMismatchException exception) {
        final ExceptionErrorMessage errorMessage = ExceptionErrorMessage.BAD_REQUEST;
        final HttpStatus status = ExceptionErrorMessage.getHttpStatus(errorMessage);

        final String paramName = exception.getName();

        String requiredTypeName = "unknown";
        if (exception.getRequiredType() != null) {
            requiredTypeName = exception.getRequiredType().getSimpleName();
        }

        String providedTypeName = "unknown";
        if (exception.getValue() != null) {
            providedTypeName = exception.getValue().getClass().getSimpleName();
        }

        final String message = String.format(
                "Invalid value for parameter '%s'. Expected type: '%s', but got: '%s'.",
                paramName, requiredTypeName, providedTypeName
        );

        final ErrorDto body = createErrorDto(errorMessage, message);
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> handleValidationExceptions(MethodArgumentNotValidException exception) {
        final Map<String, String> errors = new HashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach((error) -> {
                    final String fieldName = error.getField();
                    final String errorMessage = error.getDefaultMessage();
                    errors.put(fieldName, errorMessage);
                });

        final ExceptionErrorMessage errorMessage = ExceptionErrorMessage.BAD_REQUEST;
        final HttpStatus status = ExceptionErrorMessage.getHttpStatus(errorMessage);
        final ErrorDto body = new ErrorDto(errorMessage, errors.toString());
        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleGenericException(Exception exception) {
        final ExceptionErrorMessage errorMessage = ExceptionErrorMessage.INTERNAL_SERVER_ERROR;
        final HttpStatus status = ExceptionErrorMessage.getHttpStatus(errorMessage);
        final ErrorDto body = createErrorDto(errorMessage);
        return ResponseEntity.status(status).body(body);
    }

    private ErrorDto createErrorDto(ExceptionErrorMessage errorMessage) {
        final String message = ExceptionErrorMessage.getMessage(errorMessage);
        return new ErrorDto(errorMessage, message);
    }

    private ErrorDto createErrorDto(ExceptionErrorMessage errorMessage, String message) {
        return new ErrorDto(errorMessage, message);
    }

}
