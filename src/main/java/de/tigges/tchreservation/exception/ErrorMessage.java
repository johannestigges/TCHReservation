package de.tigges.tchreservation.exception;

public record ErrorMessage(
        String message,
        String details,
        String field) {
}
