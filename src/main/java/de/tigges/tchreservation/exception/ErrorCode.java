package de.tigges.tchreservation.exception;

public enum ErrorCode {
    JSON_SERIALIZATION,
    USER_NOT_ACTIVE,
    USER_NOT_AUTHORIZED, OCCUPIED, INVALID_RESERVATION_TYPE, NO_RESERVATION_TYSTEM, USER_EXISTS, PASSWORD_EMPTY, USER_NAME_EMPTY, NO_SYSTEM_CONFIG_ID, WRONG_USER, STRING_TOO_SHORT, STRING_TOO_LONG, NULL_NOT_ALLOWED, NUMBER_TOO_SMALL, NUMBER_TOO_BIG, NO_RESERVATION_TYPES, OPENING_HOUR_AFTER_CLOSING_HOUR, TOO_MANY_COURTS, COURT_TOO_SMALL, COURT_TOO_BIG, REPEATUNTIL_EMPTY, REPEATUNTIL_BEFORE_START, DURATION_TOO_SMALL, DURATION_TOO_LONG, USER_CANNOT_ADD_TYPE, DAYOFWEEK_NOT_ALLOWED, START_HOUR_BEFORE_OPENING, START_HOUR_AFTER_CLOSING, START_TIME_MINUTES, DATE_IN_THE_PAST, START_TIME_PLUS_DURATION, DATE_TOO_FAR_IN_FUTURE, USER_NOT_LOGGED_IN, NOT_EXISTS, EXISTS,
}
