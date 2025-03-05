package com.tcc.edlaine.crosscutting.exceptions.user;

import com.tcc.edlaine.crosscutting.exceptions.general.ObjectBadRequestException;

public class UserBadRequest extends ObjectBadRequestException {

    public UserBadRequest() {
        super();
    }

    public UserBadRequest(String message) {
        super(message);
    }
}