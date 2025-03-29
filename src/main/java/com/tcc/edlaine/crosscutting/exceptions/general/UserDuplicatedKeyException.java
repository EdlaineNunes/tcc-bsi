
package com.tcc.edlaine.crosscutting.exceptions.general;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(HttpStatus.CONFLICT)
public class UserDuplicatedKeyException extends RuntimeException{

    public UserDuplicatedKeyException(String message) {
        super(message);
    }

}