package de.tigges.tchreservation.exception;

public record ErrorMessage(
        ErrorCode code,
        String message,
        String field) {
}
