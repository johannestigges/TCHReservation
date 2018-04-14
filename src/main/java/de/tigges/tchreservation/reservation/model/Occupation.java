package de.tigges.tchreservation.reservation.model;

import java.time.LocalDate;
import java.time.LocalTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import de.tigges.tchreservation.EntityType;
import de.tigges.tchreservation.protocol.ProtocolEntity;

@Entity
public class Occupation implements ProtocolEntity {

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
	private Reservation reservation;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public LocalTime getStart() {
		return start;
	}

	public void setStart(LocalTime start) {
		this.start = start;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public int getCourt() {
		return court;
	}

	public void setCourt(int court) {
		this.court = court;
	}

	public int getLastCourt() {
		return lastCourt;
	}

	public void setLastCourt(int lastCourt) {
		this.lastCourt = lastCourt;
	}

	public ReservationType getType() {
		return type;

	}

	public void setType(ReservationType type) {
		this.type = type;
	}

	public long getSystemConfigId() {
		return systemConfigId;
	}

	public void setSystemConfigId(long systemConfigId) {
		this.systemConfigId = systemConfigId;
	}

	public Reservation getReservation() {
		return reservation;
	}

	public void setReservation(Reservation reservation) {
		this.reservation = reservation;
	}

	@Override
	public String toProtocol() {
		return toProtocol(//
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
