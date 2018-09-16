package de.tigges.tchreservation;

import static org.junit.Assert.assertThat;

import java.util.Iterator;
import java.util.Map;

import org.hamcrest.Matchers;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;

import de.tigges.tchreservation.protocol.ActionType;
import de.tigges.tchreservation.protocol.Protocol;
import de.tigges.tchreservation.protocol.ProtocolRepository;
import de.tigges.tchreservation.protocol.Protocollable;
import net.minidev.json.JSONObject;

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
				checkProtocol(protocol, entity);
			}
		}
		assertThat(found, Matchers.is(1));
	}

	/**
	 * check that a protocol value has an expected value
	 * 
	 * @param p
	 * @param actionType
	 * @param value
	 */
	public void checkProtocol(Protocol p, Protocollable entity) {
		assertThat(p.getEntityType(), Matchers.is(entity.protocolEntityType()));
		assertThat(p.getEntityId(), Matchers.is(entity.protocolEntityId()));
		JSONAssert.assertEquals(p.getValue(), new org.json.JSONObject(entity.protocolFields()), true);
	}
}