package de.tigges.tchreservation.protocol.jpa;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.json.JSONObject;

import de.tigges.tchreservation.protocol.ActionType;
import de.tigges.tchreservation.protocol.EntityType;
import de.tigges.tchreservation.protocol.Protocollable;
import de.tigges.tchreservation.user.jpa.UserEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "protocol")
@Data
@NoArgsConstructor
public class ProtocolEntity {

	public ProtocolEntity(Protocollable entity, ActionType actionType, UserEntity user) {
		setTime(LocalDateTime.now());
		setEntityType(entity.protocolEntityType());
		setEntityId(entity.protocolEntityId());
		setValue(new JSONObject(entity.protocolFields()).toString());
		setActionType(actionType);
		setUser(user);
	}

	/**
	 * Constructor for {@link ActionType#MODIFY}
	 * 
	 * @param entity
	 * @param oldEntity
	 * @param user
	 */
	public ProtocolEntity(Protocollable entity, Protocollable oldEntity, UserEntity user) {
		this(entity, ActionType.MODIFY, user);
		this.setOldValue(new JSONObject(oldEntity.protocolFields()).toString());
	}

	@Id
	@GeneratedValue
	private long id;

	@Column(nullable = false)
	private LocalDateTime time;

	@Column(nullable = false)
	private EntityType entityType;

	@Column(nullable = false)
	private long entityId;

	@Column(nullable = false)
	private ActionType actionType;

	@Column(length = 20000, nullable = false)
	private String value;

	@Column(length = 20000, nullable = true)
	private String oldValue;

	@ManyToOne(optional = false)
	private UserEntity user;
}
