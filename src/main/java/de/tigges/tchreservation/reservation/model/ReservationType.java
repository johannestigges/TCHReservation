package de.tigges.tchreservation.reservation.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ReservationType {
	@JsonProperty("0")
	INDIVIDUAL, 
	
	@JsonProperty("1")
	TRAINER, 
	
	@JsonProperty("2")
	TOURNAMENT, 
	
	@JsonProperty("3")
	PREPAID, 
	
	@JsonProperty("4")
	UNAVAILABLE
}
