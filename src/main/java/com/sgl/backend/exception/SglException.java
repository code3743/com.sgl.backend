package com.sgl.backend.exception;

public class SglException extends RuntimeException {
    public SglException(String message) {
        super(message);
    }

    public SglException(String message, Throwable cause) {
        super(message, cause);
    }
}
