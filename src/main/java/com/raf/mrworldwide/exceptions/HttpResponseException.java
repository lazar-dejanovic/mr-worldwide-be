package com.raf.mrworldwide.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class HttpResponseException extends RuntimeException {

	private HttpStatus httpStatus;

	public HttpResponseException(String message, HttpStatus httpStatus) {
		super(message);
		this.httpStatus = httpStatus;
	}

    public HttpResponseException(String message, Throwable cause, HttpStatus httpStatus) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

	public HttpResponseException(Throwable cause) {
		super(cause);
	}

}
