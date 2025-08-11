package de.tigges.tchreservation.util.exception;

public record ErrorMessage(
        ErrorCode code,
        String message,
        String field) {
}
