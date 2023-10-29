package de.tigges.tchreservation.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorMessage {
    private String message;
    private String details;
    private String field;


    @Override
    public String toString() {
        return String.format("%s (%s) [%s]", message, details == null ? "" : details, field == null ? "" : field);
    }
}
