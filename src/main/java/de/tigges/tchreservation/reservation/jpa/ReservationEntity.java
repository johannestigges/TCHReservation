package de.tigges.tchreservation.reservation.jpa;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import de.tigges.tchreservation.protocol.EntityType;
import de.tigges.tchreservation.protocol.Protocollable;
import de.tigges.tchreservation.reservation.model.RepeatType;
import de.tigges.tchreservation.reservation.model.ReservationType;
import de.tigges.tchreservation.user.jpa.UserEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reservation")
@Data
@NoArgsConstructor
public class ReservationEntity implements Protocollable {

	@Id
	@GeneratedValue
	private long id;

	@Column(nullable = false)
	private long systemConfigId;

	@Column(nullable = false)
	private String text;

	@Column(nullable = false)
	private LocalDate date;

	@Column(nullable = false)
	private LocalTime start;

	@Column(nullable = false)
	private int duration;

	@Column(nullable = false)
	private String courts;

	@Column(nullable = false)
	private ReservationType type;

	@Column(nullable = true)
	private RepeatType repeatType;

	@Column(nullable = true)
	private LocalDate repeatUntil;

	@ManyToOne(optional = false)
	private UserEntity user;

	public ReservationEntity(long systemConfigId, UserEntity user, String text, String courts, LocalDate date,
			LocalTime start, int duration, ReservationType type) {
		setSystemConfigId(systemConfigId);
		setUser(user);
		setText(text);
		setCourts(courts);
		setDate(date);
		setStart(start);
		setDuration(duration);
		setType(type);
	}

	@Override
	public Map<String, String> protocolFields() {
		return protocolFields( //
				"system config", Long.toString(systemConfigId), //
				"text", text, //
				"date", date.toString(), //
				"start", start.toString(), //
				"duration", Integer.toString(duration), //
				"courts", courts, //
				"type", type.name(), //
				"weekly repeat until", repeatUntil == null ? "" : repeatUntil.toString() //
		);
	}

	@Override
	public EntityType protocolEntityType() {
		return EntityType.RESERVATION;
	}

	@Override
	public long protocolEntityId() {
		return id;
	}
}
