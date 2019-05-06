package de.tigges.tchreservation.reservation.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.tigges.tchreservation.protocol.EntityType;
import de.tigges.tchreservation.protocol.Protocollable;
import lombok.Data;

@Entity
@Data
public class Occupation implements Protocollable {

	@Id
	@GeneratedValue
	private long id;

	private String text;
	private LocalDate date;
	private LocalTime start;
	private int duration;

	private int court;
	private int lastCourt;

	private ReservationType type;

	private long systemConfigId;

	@ManyToOne(optional = false)
	@JsonIgnore // avoid infinite serialization loop
	private Reservation reservation;

	@Override
	public Map<String, String> protocolFields() {
		return protocolFields(//
				"systemConfigId", Long.toString(systemConfigId), //
				"court", Integer.toString(court), //
				"lastCourt", Integer.toString(lastCourt), //
				"date", date.toString(), //
				"start", start.toString(), //
				"duration", Integer.toString(duration), //
				"text", text, //
				"type", type.name());
	}

	@Override
	public EntityType protocolEntityType() {
		return EntityType.OCCUPATION;
	}

	@Override
	public long protocolEntityId() {
		return id;
	}
}
