package com.olma.exception;

public class AiEstimateException extends RuntimeException {
    public AiEstimateException(String message) {
        super(message);
    }

    public AiEstimateException(String message, Throwable cause) {
        super(message, cause);
    }
}
