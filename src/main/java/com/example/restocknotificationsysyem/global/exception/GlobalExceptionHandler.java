package com.example.restocknotificationsysyem.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler extends RuntimeException{
    @ExceptionHandler({RestockException.class})
    public ResponseEntity<ErrorHandler> globalExceptionHandler(Exception ex) {
        ErrorHandler errorHandler = new ErrorHandler(ex.getMessage(), HttpStatus.NOT_FOUND.value());
        return new ResponseEntity<>(errorHandler, HttpStatus.NOT_FOUND);
    }

}
