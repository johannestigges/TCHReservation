package de.tigges.tchreservation;

import static org.junit.Assert.assertThat;

import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;

import de.tigges.tchreservation.protocol.ActionType;
import de.tigges.tchreservation.protocol.Protocol;
import de.tigges.tchreservation.protocol.ProtocolEntity;
import de.tigges.tchreservation.protocol.ProtocolRepository;

public class ProtocolTest {

	@Autowired
	ProtocolRepository protocolRepository;

	public void checkProtocol(ProtocolEntity entity, ActionType actionType) {
		protocolRepository.findByEntityTypeAndEntityId(entity.getEntityType(), entity.getEntityId())
				.forEach(p -> checkProtocol(p, actionType, entity.toProtocol()));
	}

	public void checkProtocol(Protocol p, ActionType actionType, String value) {
		assertThat(p.getActionType(), Matchers.is(actionType));
		assertThat(p.getValue(), Matchers.is(value));
	}
}