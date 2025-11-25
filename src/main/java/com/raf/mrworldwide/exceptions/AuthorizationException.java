package com.raf.mrworldwide.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class AuthorizationException extends HttpResponseException {

    public AuthorizationException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }

}
