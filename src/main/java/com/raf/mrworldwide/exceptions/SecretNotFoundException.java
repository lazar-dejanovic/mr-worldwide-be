package com.raf.mrworldwide.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class SecretNotFoundException extends HttpResponseException {

    public SecretNotFoundException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public SecretNotFoundException(String message, Throwable cause) {
        super(message, cause, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

