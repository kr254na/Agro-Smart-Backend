package com.agrosmart.common.exception;

import org.springframework.http.HttpStatus;

public class ImageUploadException extends RuntimeException {
    private final HttpStatus status;

    public ImageUploadException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
