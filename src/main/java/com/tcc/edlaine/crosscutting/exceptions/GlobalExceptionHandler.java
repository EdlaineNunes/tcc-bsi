package com.tcc.edlaine.crosscutting.exceptions;

import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class GlobalExceptionHandler {

//    @ExceptionHandler(CustomExceptionEpayException.class)
//    public ResponseEntity<ErrorResponse> handleCustomException(CustomExceptionEpayException ex) {
//        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), ex.getStatus());
//    }
//
//    @ExceptionHandler(AuthorizationVoucherException.class)
//    public ResponseEntity<ErrorResponse> handleAuthorizationException(AuthorizationVoucherException ex) {
//        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.UNAUTHORIZED);
//    }

}
