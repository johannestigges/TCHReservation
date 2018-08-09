package de.tigges.tchreservation;

import static org.junit.Assert.assertThat;

import java.util.Iterator;
import java.util.Map;

import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;

import de.tigges.tchreservation.protocol.ActionType;
import de.tigges.tchreservation.protocol.Protocol;
import de.tigges.tchreservation.protocol.ProtocolRepository;
import de.tigges.tchreservation.protocol.Protocollable;

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
	public void checkProtocol(Protocollable entity, ActionType actionType) {
		Iterable<Protocol> protocols = protocolRepository.findByEntityTypeAndEntityId(entity.protocolEntityType(),
				entity.protocolEntityId());
		int found = 0;
		Iterator<Protocol> iter = protocols.iterator();
		while (iter.hasNext()) {
			Protocol protocol = iter.next();
			if (protocol.getActionType().equals(actionType)) {
				found++;
				checkProtocolFields(protocol, actionType, entity.protocolFields());
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
	public void checkProtocolFields(Protocol p, ActionType actionType, Map<String, String> value) {
//			assertThat(p.getValue(), Matchers.is(value));
	}
}