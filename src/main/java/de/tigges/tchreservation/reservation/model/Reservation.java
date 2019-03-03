package de.tigges.tchreservation.reservation.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import de.tigges.tchreservation.protocol.EntityType;
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

	@Transient
	private List<Occupation> occupations;

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

	@Transient
	public List<Occupation> getOccupations() {
		if (occupations == null) {
			occupations = new ArrayList<>();
		}
		return occupations;
	}

	public void addOccupation(Occupation occupation) {
		occupation.setReservation(this);
		getOccupations().add(occupation);
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

	/**
	 * set the courts
	 * 
	 * @param courts
	 */
	@Transient
	public void setCourts(int... courts) {
		this.courts = toCourts(courts);
	}

	/**
	 * add courts to the list of courts
	 * 
	 * @param courts
	 */
	@Transient
	public void addCourts(int... courts) {
		this.courts = this.courts + ' ' + toCourts(courts);
	}

	/**
	 * get the courts as int[]
	 * 
	 * @return courts as int[]
	 */
	@Transient
	public int[] getCourtsAsArray() {
		return toCourts(this.courts);
	}

	/**
	 * convert courts from String to int[]
	 * 
	 * @param courtsString
	 * @return int[]
	 */
	@Transient
	public int[] toCourts(String courtsString) {
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

	/**
	 * convert courts from int... to String
	 * 
	 * @param courts
	 * @return courtsString
	 */
	@Transient
	public String toCourts(int... courts) {
		return Arrays.stream(courts).mapToObj(String::valueOf).collect(Collectors.joining(" "));
	}
}
