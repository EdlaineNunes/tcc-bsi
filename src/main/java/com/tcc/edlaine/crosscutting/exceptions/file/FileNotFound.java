package com.tcc.edlaine.crosscutting.exceptions.file;

import com.tcc.edlaine.crosscutting.exceptions.general.ObjectNotFoundException;

public class FileNotFound extends ObjectNotFoundException {
    public FileNotFound(String message) {
        super(message);
    }
}
