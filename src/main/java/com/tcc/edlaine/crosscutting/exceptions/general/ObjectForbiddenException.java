
package com.tcc.edlaine.crosscutting.exceptions.general;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(HttpStatus.FORBIDDEN)
public class ObjectForbiddenException extends RuntimeException{

    public ObjectForbiddenException() {
        super("Access Denied");
    }

    public ObjectForbiddenException(String message) {
        super("Access Denied -> " + message);
    }

}