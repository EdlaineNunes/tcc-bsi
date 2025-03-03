package com.tcc.edlaine.crosscutting.utils;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public abstract class DateUtils {

    private final static DateTimeFormatter DEFAULT_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");


    public static String convertLocalDateTimeToString(LocalDateTime localDateTime) {
        if(localDateTime == null) {
            return null;
        }

        try {
            return DEFAULT_TIME_FORMATTER.format(localDateTime);
        } catch (Exception e) {
            log.error("error parsing localDateTime", e);
            return localDateTime.toString();
        }
    }

    public static LocalDateTime getLocalDateTimeNow() {
        return ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"))
                .toLocalDateTime();
    }

    public static String getLocalDateTimeNowAsString() {
        return convertLocalDateTimeToString(getLocalDateTimeNow());
    }
}

