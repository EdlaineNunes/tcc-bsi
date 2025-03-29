package com.tcc.edlaine.crosscutting.exceptions.general;

import com.tcc.edlaine.crosscutting.exceptions.file.FileNotFound;
import com.tcc.edlaine.crosscutting.exceptions.file.FileUnprocessableEntity;
import com.tcc.edlaine.crosscutting.exceptions.user.UserAccessDenied;
import com.tcc.edlaine.crosscutting.exceptions.user.UserNotFound;
import com.tcc.edlaine.crosscutting.exceptions.user.UserUnauthorized;
import com.tcc.edlaine.domain.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FileNotFound.class)
    public ResponseEntity<ErrorResponse> handleCustomException(FileNotFound ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(FileUnprocessableEntity.class)
    public ResponseEntity<ErrorResponse> handleCustomException(FileUnprocessableEntity ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(UserNotFound.class)
    public ResponseEntity<ErrorResponse> handleCustomException(UserNotFound ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserAccessDenied.class)
    public ResponseEntity<ErrorResponse> handleCustomException(UserAccessDenied ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(UserUnauthorized.class)
    public ResponseEntity<ErrorResponse> handleCustomException(UserUnauthorized ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UserDuplicatedKeyException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(UserDuplicatedKeyException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.CONFLICT);
    }


}
