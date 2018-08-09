package de.tigges.tchreservation.reservation.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import de.tigges.tchreservation.EntityType;
import de.tigges.tchreservation.protocol.Protocollable;
import de.tigges.tchreservation.user.model.User;

@Entity
public class Reservation implements Protocollable {

	@Id
	@GeneratedValue
	private long id;

	private long systemConfigId;
	private String text;
	private LocalDate date;
	private LocalTime start;
	private int duration;
	private String courts;
	private ReservationType type;
	private LocalDate weeklyRepeatUntil;

	@ManyToOne(optional = false)
	private User user;

	public Reservation() {
	}

	public Reservation(long systemConfigId, User user, String text, String courts, LocalDate date, LocalTime start,
			int duration, ReservationType type) {
		setSystemConfig(systemConfigId);
		setUser(user);
		setText(text);
		setCourts(courts);
		setDate(date);
		setStart(start);
		setDuration(duration);
		setType(type);
	}

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

	public String getCourts() {
		return courts;
	}

	public void setCourts(String courts) {
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
	public Map<String, String> protocolFields() {
		return protocolFields("text", text, //
				"date", date.toString(), //
				"start", start.toString(), //
				"duration", Integer.toString(duration), //
				"type", type.name(), //
				"system config", Long.toString(systemConfigId));
	}

	@Override
	public EntityType protocolEntityType() {
		return EntityType.RESERVATION;
	}

	@Override
	public long protocolEntityId() {
		return id;
	}

	public void courts(int... courts) {
		this.courts = toCourts(courts);
	}

	public void addCourts(int... courts) {
		this.courts = this.courts + ' ' + toCourts(courts);
	}

	public int[] courtsArray() {
		return toCourts(this.courts);
	}

	public static int[] toCourts(String courtsString) {
		if (courtsString == null) {
			return new int[0];
		}
		String[] c = courtsString.split(" ");
		int[] courts = new int[c.length];
		for (int i = 0; i < courts.length; i++) {
			courts[i] = Integer.parseInt(c[i]);
		}
		return courts;
	}

	public static String toCourts(int... courts) {
		return Arrays.toString(courts).replaceAll("\\[|\\]|,|\\s", "");

	}

}
