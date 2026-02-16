package com.owsm.AuthService.exception;

public class HmsException extends Exception {
    public static final long serialVersionUID = 1L;
    public HmsException(String message) {
        super(message);
    }

    public HmsException() {
        super("Forbidden");
    }
}
