package de.tigges.tchreservation.protocol;

import java.util.HashMap;
import java.util.Map;

import de.tigges.tchreservation.EntityType;

/**
 * interface for protocollable entities
 */
public interface Protocollable {

	/**
	 * @return protocollable fields
	 */
	Map<String, String> protocolFields();

	/**
	 * @return {@link EntityType} of protocollable entity
	 */
	EntityType protocolEntityType();

	/**
	 * @return id of protocollable entity
	 */
	long protocolEntityId();

	/**
	 * helper method to define all protocol fields
	 * <p>
	 * 
	 * @param keyValues
	 * @return
	 */
	default Map<String, String> protocolFields(String... keyValues) {

		if (keyValues.length % 2 != 0) {
			throw new IllegalArgumentException("key values not even.");
		}
		Map<String, String> fields = new HashMap<>();
		int i = 0;
		while (i < keyValues.length) {
			fields.put(keyValues[i++], keyValues[i++]);
		}
		return fields;
	}
}
