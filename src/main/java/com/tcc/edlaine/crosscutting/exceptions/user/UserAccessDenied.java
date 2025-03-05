package com.tcc.edlaine.crosscutting.exceptions.user;

import com.tcc.edlaine.crosscutting.exceptions.general.ObjectForbiddenException;

public class UserAccessDenied extends ObjectForbiddenException {

    public UserAccessDenied() {
        super();
    }

    public UserAccessDenied(String message) {
        super(message);
    }
}