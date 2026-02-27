package com.example.newsService.exception;

public class OwsmException extends Exception {
    public static final long serialVersionUID = 1L;
    public OwsmException(String message) {
        super(message);
    }

    public OwsmException() {
        super("Forbidden");
    }
}
