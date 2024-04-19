package com.jurisitsm.test.exception;

import org.springframework.http.HttpStatus;

public class ClientManagerException extends Exception {

    private final HttpStatus httpStatus;

    public ClientManagerException(String message, HttpStatus httpStatus){
        super(message);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
