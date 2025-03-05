package com.tcc.edlaine.crosscutting.exceptions.file;

import com.tcc.edlaine.crosscutting.exceptions.general.UnprocessableEntityErrorException;

public class FileUnprocessableEntity extends UnprocessableEntityErrorException {
    public FileUnprocessableEntity(String message) {
        super(message);
    }

    public FileUnprocessableEntity() {
        super();
    }
}
