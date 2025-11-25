package com.raf.mrworldwide.exceptions;

import org.springframework.http.HttpStatus;

public class CustomException extends HttpResponseException {

    public CustomException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }

    public CustomException(String message, Throwable cause, HttpStatus httpStatus) {
        super(message, cause, httpStatus);
    }
}
