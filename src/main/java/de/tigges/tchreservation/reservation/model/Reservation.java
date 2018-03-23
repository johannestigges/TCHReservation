package de.tigges.tchreservation.reservation.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import de.tigges.tchreservation.user.model.User;

@Entity
public class Reservation  {
	
	public Reservation() {}
	
	public Reservation (ReservationSystemConfig config, User user, String name, int court, LocalDateTime start, int duration, ReservationType type) {
		setSystemConfig(config);
		setUser(user);
		setName(name);
		setCourts(new int[1]);
		getCourts()[0] = court;
		setStart(start);
		setDuration(duration);
		setType(type);
	}
	
	@Id
	@GeneratedValue
	private long id;
	
	private String name;
	private LocalDateTime start;
	private LocalDate weeklyRepeatUntil;
	private int[] courts;
	private int duration;
	private ReservationType type;

	@ManyToOne(optional=false)
	private ReservationSystemConfig systemConfig;
	
	@ManyToOne(optional=false)
	private User user;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ReservationSystemConfig getSystemConfig() {
		return systemConfig;
	}

	public void setSystemConfig(ReservationSystemConfig systemConfig) {
		this.systemConfig = systemConfig;
	}

	public LocalDateTime getStart() {
		return start;
	}

	public void setStart(LocalDateTime start) {
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
	
}
