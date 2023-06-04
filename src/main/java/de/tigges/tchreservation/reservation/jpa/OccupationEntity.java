package de.tigges.tchreservation.reservation.jpa;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

import jakarta.persistence.*;

import de.tigges.tchreservation.protocol.EntityType;
import de.tigges.tchreservation.protocol.Protocollable;
import de.tigges.tchreservation.reservation.model.ReservationType;
import lombok.Data;

@Entity
@Table(name = "occupation")
@Data
public class OccupationEntity implements Protocollable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(nullable = false)
	private String text;

	@Column(nullable = false)
	private LocalDate date;

	@Column(nullable = false)
	private LocalTime start;

	@Column(nullable = false)
	private int duration;

	@Column(nullable = false)
	private int court;

	@Column()
	private int lastCourt;

	@Column(nullable = false)
	private ReservationType type;

	@Column(nullable = false)
	private long systemConfigId;

	@ManyToOne(optional = false)
	private ReservationEntity reservation;

	@Override
	public Map<String, String> protocolFields() {
		return protocolFields( //
				"systemConfigId", Long.toString(systemConfigId), //
				"court", Integer.toString(court), //
				"lastCourt", Integer.toString(lastCourt), //
				"date", date.toString(), //
				"start", start.toString(), //
				"duration", Integer.toString(duration), //
				"text", text, //
				"type", type.name()//
		);
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
