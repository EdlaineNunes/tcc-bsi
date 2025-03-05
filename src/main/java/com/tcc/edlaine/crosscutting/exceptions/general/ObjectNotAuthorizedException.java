
package com.tcc.edlaine.crosscutting.exceptions.general;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class ObjectNotAuthorizedException extends RuntimeException{

    public ObjectNotAuthorizedException() {
        super("error authorizing");
    }

}