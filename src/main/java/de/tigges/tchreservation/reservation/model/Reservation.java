package de.tigges.tchreservation.reservation.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Reservation extends ReservationSystemConfigId {
	private long userId;
	private LocalDateTime start;
	private LocalDate weeklyRepeatUntil;
	private int[] courts;
	private int duration;
	private OccupationType type;

	public Reservation() {
	}
	
	public Reservation (long systemId, String name, long userId, int court, LocalDateTime start, int duration, OccupationType type) {
		this.systemId = systemId;
		this.name = name;
		this.userId = userId;
		this.courts = new int[1];
		this.courts[0] = court;
		this.start = start;
		this.duration = duration;
		this.type = type;
	}

	public static Reservation of(ReservationEntity entity) {
		Reservation reservation = new Reservation();
		reservation.systemId = entity.getSystemId();
		reservation.id = entity.id;
		reservation.name = entity.name;
		reservation.userId = entity.userId;
		reservation.start = entity.start;
		reservation.weeklyRepeatUntil = entity.weeklyRepeatUntil;
		reservation.duration = entity.duration;
		reservation.type = OccupationType.valueOf(entity.type);
		String[] tmpCourts = entity.courts.split(",");
		reservation.courts = new int[tmpCourts.length];
		for (int i = 0; i < tmpCourts.length; i++) {
			reservation.courts[i] = Integer.valueOf(tmpCourts[i]);
		}
		return reservation;
	}

	public static List<Reservation> of(Iterable<ReservationEntity> entities) {
		List<Reservation> list = new ArrayList<>();
		entities.forEach(e -> list.add(Reservation.of(e)));
		return list;
	}

	public long getUserId() {
		return userId;
	}

	public LocalDateTime getStart() {
		return start;
	}

	public LocalDate getWeeklyRepeatUntil() {
		return weeklyRepeatUntil;
	}

	public int[] getCourts() {
		return courts;
	}

	public int getDuration() {
		return duration;
	}

	public OccupationType getType() {
		return type;
	}

	public ReservationEntity toEntity() {
		ReservationEntity entity = new ReservationEntity();
		ReservationSystemConfigEntity system = new ReservationSystemConfigEntity();
		system.id = this.systemId;
		entity.system = system;
		entity.id = this.id;
		entity.name = this.name;
		entity.userId = this.userId;
		entity.courts = Arrays.stream(this.courts).mapToObj(String::valueOf).collect(Collectors.joining(","));
		entity.start = this.start;
		entity.weeklyRepeatUntil = this.weeklyRepeatUntil;
		entity.duration = duration;
		entity.type = this.type.name();
		return entity;
	}

	public String toString() {
		return "[" //
				+ "id:" + id //
				+ " name:" + name //
				+ " systemID:" + systemId //
				+ " start:" + start //
				+ " duration:" + duration //
				+ " repeat:" + weeklyRepeatUntil //
				+ " courts:" + Arrays.toString(courts) //
				+ " type:" + type //
				+ "]";
	}
}
