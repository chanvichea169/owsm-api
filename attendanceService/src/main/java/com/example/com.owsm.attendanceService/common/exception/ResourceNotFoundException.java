package com.example.attendanceService.common.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String entity, Object key) {
        super(entity + " not found for id " + key);
    }
}
