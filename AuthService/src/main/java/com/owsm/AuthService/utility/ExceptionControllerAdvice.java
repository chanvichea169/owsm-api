package com.owsm.AuthService.utility;

import com.owsm.AuthService.exception.OwsmException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionControllerAdvice {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorInfor> exceptionHandler(Exception e) {
        ErrorInfor errorInfo = new ErrorInfor(e.getMessage());
        return ResponseEntity.status(500).body(errorInfo);
    }
    @ExceptionHandler(OwsmException.class)
    public ResponseEntity<ErrorInfor> hmsExceptionHandler(OwsmException e) {
        ErrorInfor errorInfo = new ErrorInfor(e.getMessage());
        return ResponseEntity.status(400).body(errorInfo);
    }
}
