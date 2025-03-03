package com.tcc.edlaine.crosscutting.utils;

import com.tcc.edlaine.domain.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.HttpStatusCodeException;
@Slf4j
public class ErrorUtils {
    public static String extractErrorMessage(HttpStatusCodeException exception){
        try {
            ErrorResponse errorResponse = exception.getResponseBodyAs(ErrorResponse.class);
            return errorResponse != null ?
                    errorResponse.getMessage() :
                    exception.getMessage();
        } catch (Exception ex) {
            log.error("error extracting message from exception body", ex);
            return exception.getMessage();
        }
    }
}
