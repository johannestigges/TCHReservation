package de.tigges.tchreservation;

import static org.junit.Assert.assertThat;

import java.util.Iterator;

import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;

import de.tigges.tchreservation.protocol.ActionType;
import de.tigges.tchreservation.protocol.Protocol;
import de.tigges.tchreservation.protocol.ProtocolEntity;
import de.tigges.tchreservation.protocol.ProtocolRepository;

/**
 * base class for unit tests dealing with protocol data
 */
public class ProtocolTest extends UserTest {

	@Autowired
	protected ProtocolRepository protocolRepository;

	/**
	 * check that entity has correct protocol
	 * 
	 * @param entity
	 * @param actionType
	 */
	public void checkProtocol(ProtocolEntity entity, ActionType actionType) {
		Iterable<Protocol> protocols = protocolRepository.findByEntityTypeAndEntityId(entity.protocolEntityType(),
				entity.protocolEntityId());
		int found = 0;
		Iterator<Protocol> iter = protocols.iterator();
		while (iter.hasNext()) {
			Protocol protocol = iter.next();
			if (protocol.getActionType().equals(actionType)) {
				found++;
				checkProtocol(protocol, actionType, entity.toProtocol());
			}
		}
		assertThat(found, Matchers.is(1));
	}

	/**
	 * check that a protocol value has an expected value 
	 * @param p
	 * @param actionType
	 * @param value
	 */
	public void checkProtocol(Protocol p, ActionType actionType, String value) {
		if (p.getActionType().equals(actionType)) {
			assertThat(p.getValue(), Matchers.is(value));
		}
	}
}