package de.tigges.tchreservation.systemconfig.jpa;

import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import de.tigges.tchreservation.protocol.EntityType;
import de.tigges.tchreservation.protocol.Protocollable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "config")
@Data
@NoArgsConstructor
public class SystemConfigEntity implements Protocollable {

	@Id
	private long id;

	@Column(nullable = false)
	private String name;

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
				"courts", courts, //
				"durationUnitInminutes", Integer.toString(durationUnitInMinutes), //
				"maxDaysReservationInfuture", Integer.toString(maxDaysReservationInFuture), //
				"maxDuration", Integer.toString(maxDuration), //
				"opening hour", Integer.toString(openingHour), //
				"closing hour", Integer.toString(closingHour) //
		);
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
