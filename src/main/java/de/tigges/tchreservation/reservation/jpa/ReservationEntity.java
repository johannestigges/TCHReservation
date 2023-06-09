package de.tigges.tchreservation.reservation.jpa;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

import jakarta.persistence.*;

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
	@GeneratedValue(strategy = GenerationType.IDENTITY)
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

	@Column()
	private RepeatType repeatType;

	@Column()
	private LocalDate repeatUntil;

	@ManyToOne(optional = false)
	private UserEntity user;

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
