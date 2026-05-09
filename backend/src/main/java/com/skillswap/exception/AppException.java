package com.skillswap.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AppException extends RuntimeException {

    private final HttpStatus status;

    public AppException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    // Convenience factories
    public static AppException notFound(String resource) {
        return new AppException(resource + " not found", HttpStatus.NOT_FOUND);
    }

    public static AppException forbidden(String reason) {
        return new AppException(reason, HttpStatus.FORBIDDEN);
    }

    public static AppException badRequest(String reason) {
        return new AppException(reason, HttpStatus.BAD_REQUEST);
    }

    public static AppException conflict(String reason) {
        return new AppException(reason, HttpStatus.CONFLICT);
    }
}