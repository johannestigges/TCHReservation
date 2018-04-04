package de.tigges.tchreservation.reservation.model;

import java.time.LocalDate;
import java.time.LocalTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import de.tigges.tchreservation.EntityType;
import de.tigges.tchreservation.protocol.ProtocolEntity;
import de.tigges.tchreservation.user.model.User;

@Entity
public class Reservation implements ProtocolEntity {
	
	public Reservation() {}
	
	public Reservation (long configId, User user, String text, int court, LocalDate date, LocalTime start, int duration, ReservationType type) {
		setSystemConfig(configId);
		setUser(user);
		setText(text);
		setCourts(new int[1]);
		getCourts()[0] = court;
		setDate(date);
		setStart(start);
		setDuration(duration);
		setType(type);
	}
	
	@Id
	@GeneratedValue
	private long id;
	
	private String text;
	private LocalDate date;
	private LocalTime start;
	private LocalDate weeklyRepeatUntil;
	private int[] courts;
	private int duration;
	private ReservationType type;

	private long systemConfigId;
	
	@ManyToOne(optional=false)
	private User user;

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

	public long getSystemConfigId() {
		return systemConfigId;
	}

	public void setSystemConfig(long systemConfigId) {
		this.systemConfigId = systemConfigId;
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

	public LocalDate getWeeklyRepeatUntil() {
		return weeklyRepeatUntil;
	}

	public void setWeeklyRepeatUntil(LocalDate weeklyRepeatUntil) {
		this.weeklyRepeatUntil = weeklyRepeatUntil;
	}

	public int[] getCourts() {
		return courts;
	}

	public void setCourts(int[] courts) {
		this.courts = courts;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public ReservationType getType() {
		return type;
	}

	public void setType(ReservationType type) {
		this.type = type;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Override
	public String toProtocol() {
		return toProtocol("text",text,
				"date", date.toString(),
				"start", start.toString(),
				"duration", Integer.toString(duration),
				"type", type.name(),
				"system config", Long.toString(systemConfigId)
				);
	}

	@Override
	public EntityType getEntityType() {
		return EntityType.RESERVATION;
	}

	@Override
	public long getEntityId() {
		return id;
	}
	
}
