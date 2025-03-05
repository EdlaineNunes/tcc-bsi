package com.tcc.edlaine.crosscutting.exceptions.user;

import com.tcc.edlaine.crosscutting.exceptions.general.ObjectNotFoundException;

public class UserNotFound extends ObjectNotFoundException {
    public UserNotFound(String message) {
        super(message);
    }
}
