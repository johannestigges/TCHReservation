package de.tigges.tchreservation.protocol;

import de.tigges.tchreservation.EntityType;

public interface ProtocolEntity {
	String toProtocol();

	EntityType protocolEntityType();

	long protocolEntityId();

	default String toProtocol(String... keyValues) {
		StringBuilder sb = new StringBuilder();
		if (keyValues.length % 2 != 0) {
			throw new IllegalArgumentException("key values not even.");
		}
		int i = 0;
		while (i < keyValues.length) {
			sb.append(keyValues[i++]).append("=").append(keyValues[i++]);
		}
		return sb.toString();
	}
}
