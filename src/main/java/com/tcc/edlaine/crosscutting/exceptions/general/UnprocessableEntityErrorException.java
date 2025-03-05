package com.tcc.edlaine.crosscutting.exceptions.general;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class UnprocessableEntityErrorException extends RuntimeException {

    public UnprocessableEntityErrorException() {
        super("error trying to processable");
    }

    public UnprocessableEntityErrorException(String message) {
        super(message);
    }

    public UnprocessableEntityErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}