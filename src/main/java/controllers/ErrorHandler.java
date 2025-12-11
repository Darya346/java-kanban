package controllers;

import exception.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ErrorHandler {

    // Этот метод будет ловить ValidationException во всем приложении
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST) // Возвращаем 400 ошибку
    public Map<String, String> handleValidationException(final ValidationException e) {
        return Map.of("error", e.getMessage());
    }

    // Можно добавить обработку остальных исключений (код 500)
    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleRuntimeException(final RuntimeException e) {
        return Map.of("error", "Произошла внутренняя ошибка сервера");
    }
}