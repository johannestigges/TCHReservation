package de.tigges.tchreservation.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FieldError {
	private String entity;
	private String field;
	private String message;
}
