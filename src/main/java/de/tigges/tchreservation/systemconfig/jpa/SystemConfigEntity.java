package de.tigges.tchreservation.systemconfig.jpa;

import java.util.Map;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import de.tigges.tchreservation.protocol.EntityType;
import de.tigges.tchreservation.protocol.Protocollable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "systemconfig")
@Data
@NoArgsConstructor
public class SystemConfigEntity implements Protocollable {

	@Id
	private long id;

	@Column(nullable = false)
	private String name;

	@Column()
	private String title;

	@Column(nullable = false)
	private String courts;

	@Column(nullable = false)
	private int durationUnitInMinutes;

	@Column(nullable = false)
	private int maxDaysReservationInFuture;

	@Column(nullable = false)
	private int maxDuration;

	@Column(nullable = false)
	private int openingHour;

	@Column(nullable = false)
	private int closingHour;

	@Override
	public Map<String, String> protocolFields() {
		return protocolFields( //
				"id", Long.toString(id), //
				"name", name, //
				"title", title, //
				"courts", courts, //
				"durationUnitInMinutes", Integer.toString(durationUnitInMinutes), //
				"maxDaysReservationInFuture", Integer.toString(maxDaysReservationInFuture), //
				"maxDuration", Integer.toString(maxDuration), //
				"openingHour", Integer.toString(openingHour), //
				"closingHour", Integer.toString(closingHour));
	}

	@Override
	public EntityType protocolEntityType() {
		return EntityType.SYSTEM_CONFIGURATION;
	}

	@Override
	public long protocolEntityId() {
		return id;
	}
}
