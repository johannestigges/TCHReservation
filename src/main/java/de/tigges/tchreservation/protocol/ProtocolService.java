package de.tigges.tchreservation.protocol;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.tigges.tchreservation.EntityType;

@RestController
@RequestMapping("/protocol")
public class ProtocolService {

	private ProtocolRepository protocolRepository;

	public ProtocolService(ProtocolRepository protocolRepository) {
		this.protocolRepository = protocolRepository;
	}

	@GetMapping("/get")
	public Iterable<Protocol> getAll() {
		return Protocol.setFields(protocolRepository.findAll());
	}

	@GetMapping("/migrate")
	public void migrate() {
		System.out.println("start migrate protocol");
		getAll().forEach(p -> migrate(p));
		System.out.println("finished migrate protocol");
	}

	private void migrate(Protocol p) {
		migrate(p, EntityType.RESERVATION, "text", "date", "start", "duration", "type", "system config");
		migrate(p, EntityType.OCCUPATION, "court", "lastCourt", "date", "start", "duration", "text", "type");
		migrate(p, EntityType.USER, "name", "email", "password", "role", "status");
	}

	private void migrate(Protocol p, EntityType type, String... fields) {
		Pattern pattern = createPattern(fields);

		// migrate value
		Matcher valueMatcher = pattern.matcher(p.getValue());
		if (valueMatcher.matches()) {
			String newValue = createNewValue(valueMatcher, fields);
			System.out.println("migrate value " + type + " " + p.getValue() + " to " + newValue.toString());
			p.setValue(newValue);
			protocolRepository.save(p);
		}
		// migrate old value
		if (p.getOldValue() != null) {
			Matcher oldValueMatcher = pattern.matcher(p.getOldValue());
			if (oldValueMatcher.matches()) {
				String newValue = createNewValue(oldValueMatcher, fields);
				System.out.println("migrate old value " + type + " " + p.getOldValue() + " to " + newValue.toString());
				p.setOldValue(newValue);
				protocolRepository.save(p);
			}
		}
	}

	private Pattern createPattern(String... fields) {
		StringBuilder regex = new StringBuilder();
		for (String field : fields) {
			regex.append(field).append("=(.*)");
		}
		return Pattern.compile(regex.toString());
	}

	private String createNewValue(Matcher matcher, String... fields) {
		StringBuilder newValue = new StringBuilder();
		newValue.append("{");
		for (int i = 0; i < fields.length; i++) {
			newValue.append("\"").append(fields[i]).append("\":\"").append(matcher.group(i + 1)).append("\"");
			if (i < fields.length - 1) {
				newValue.append(",");
			}
		}
		newValue.append("}");
		return newValue.toString();
	}
}
