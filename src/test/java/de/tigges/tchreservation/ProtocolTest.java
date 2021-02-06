package de.tigges.tchreservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Iterator;

import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;

import de.tigges.tchreservation.protocol.ActionType;
import de.tigges.tchreservation.protocol.Protocollable;
import de.tigges.tchreservation.protocol.jpa.ProtocolEntity;
import de.tigges.tchreservation.protocol.jpa.ProtocolRepository;

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
	 * @throws JSONException
	 */
	public void checkProtocol(Protocollable entity, ActionType actionType) throws JSONException {
		Iterable<ProtocolEntity> protocols = protocolRepository.findByEntityTypeAndEntityId(entity.protocolEntityType(),
				entity.protocolEntityId());
		int found = 0;
		Iterator<ProtocolEntity> iter = protocols.iterator();
		while (iter.hasNext()) {
			ProtocolEntity protocol = iter.next();
			if (protocol.getActionType().equals(actionType)) {
				found++;
				checkProtocol(protocol, entity);
			}
		}
		assertThat(found).isEqualTo(1);
	}

	/**
	 * check that a protocol value has an expected value
	 * 
	 * @param p
	 * @param actionType
	 * @param value
	 * @throws JSONException
	 */
	public void checkProtocol(ProtocolEntity p, Protocollable entity) throws JSONException {
		assertThat(p.getEntityType()).isEqualTo(entity.protocolEntityType());
		assertThat(p.getEntityId()).isEqualTo(entity.protocolEntityId());
		JSONAssert.assertEquals(p.getValue(), new org.json.JSONObject(entity.protocolFields()), true);
	}
}