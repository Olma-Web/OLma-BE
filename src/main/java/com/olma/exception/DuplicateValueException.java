package com.olma.exception;

public class DuplicateValueException extends RuntimeException {
    public DuplicateValueException(String field, String value) {
        super(field + " already in use: " + value);
    }
}
