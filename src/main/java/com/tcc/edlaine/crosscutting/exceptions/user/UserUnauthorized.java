package com.tcc.edlaine.crosscutting.exceptions.user;

import com.tcc.edlaine.crosscutting.exceptions.general.ObjectNotAuthorizedException;

public class UserUnauthorized extends ObjectNotAuthorizedException {
    public UserUnauthorized() {
        super();
    }
}