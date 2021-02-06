package de.tigges.tchreservation.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * data describing an error concerning a field
 * 
 * @author johannes
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FieldError {
	private String entity;
	private String field;
	private String message;
}
