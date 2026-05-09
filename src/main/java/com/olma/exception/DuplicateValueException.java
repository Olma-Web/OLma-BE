package com.olma.exception;

public class DuplicateValueException extends RuntimeException {
    public DuplicateValueException(String field) {
        super(field + " already in use");
    }
}
