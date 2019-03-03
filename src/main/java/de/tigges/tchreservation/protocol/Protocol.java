package de.tigges.tchreservation.protocol;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.json.JSONObject;

import de.tigges.tchreservation.user.model.User;

@Entity
public class Protocol {

	public Protocol() {
	}

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

	@Transient
	private List<ProtocolField> fields;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public LocalDateTime getTime() {
		return time;
	}

	public void setTime(LocalDateTime time) {
		this.time = time;
	}

	public EntityType getEntityType() {
		return entityType;
	}

	public void setEntityType(EntityType entityType) {
		this.entityType = entityType;
	}

	public long getEntityId() {
		return entityId;
	}

	public void setEntityId(long entityId) {
		this.entityId = entityId;
	}

	public ActionType getActionType() {
		return actionType;
	}

	public void setActionType(ActionType actionType) {
		this.actionType = actionType;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getOldValue() {
		return oldValue;
	}

	public void setOldValue(String oldValue) {
		this.oldValue = oldValue;
	}

	public List<ProtocolField> getFields() {
		return fields;
	}

	public static class ProtocolField {
		public String name;
		public String value;
		public String oldValue;
	}

	public static Iterable<Protocol> setFields(Iterable<Protocol> protocols) {
		if (protocols != null) {
			protocols.forEach(p -> p.setFields());
		}
		return protocols;
	}
	
	private void setFields() {
	}
}
