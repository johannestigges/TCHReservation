package de.tigges.tchreservation.protocol;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import de.tigges.tchreservation.user.model.User;

@Entity
public class Protocol {

	public Protocol() {}
	public Protocol(EntityTxpe entityType, long entityId, ActionType actionType, String value, User user) {
		setTime(LocalDateTime.now());
		setEntityType(entityType);
		setEntityId(entityId);
		setActionType(actionType);
		setValue(value);
		setUser(user);
	}
	
	@Id
	@GeneratedValue
	private long id;
	private LocalDateTime time;
	private EntityTxpe entityType;
	private long entityId;
	private ActionType actionType;
	@Column(length = 2000)
	private String value;
	@Column(length = 2000, nullable = true)
	private String oldValue;
	@ManyToOne(optional = false)
	private User user;

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

	public EntityTxpe getEntityType() {
		return entityType;
	}

	public void setEntityType(EntityTxpe entityType) {
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
}
