package de.tigges.tchreservation.protocol;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.json.JSONObject;

import de.tigges.tchreservation.user.model.User;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Protocol {

	public Protocol(Protocollable entity, ActionType actionType, User user) {
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
	public Protocol(Protocollable entity, Protocollable oldEntity, User user) {
		this(entity, ActionType.MODIFY, user);
		this.setOldValue(new JSONObject(oldEntity.protocolFields()).toString());
	}

	@Id
	@GeneratedValue
	private long id;
	private LocalDateTime time;
	private EntityType entityType;
	private long entityId;
	private ActionType actionType;
	@Column(length = 2000)
	private String value;
	@Column(length = 2000, nullable = true)
	private String oldValue;
	@ManyToOne(optional = false)
	private User user;
}
