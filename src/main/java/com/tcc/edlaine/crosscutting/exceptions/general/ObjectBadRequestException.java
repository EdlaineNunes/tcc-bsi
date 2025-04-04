
package com.tcc.edlaine.crosscutting.exceptions.general;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ObjectBadRequestException extends RuntimeException{

    public ObjectBadRequestException() {
        super();
    }

    public ObjectBadRequestException(String message) {
        super(message);
    }

}