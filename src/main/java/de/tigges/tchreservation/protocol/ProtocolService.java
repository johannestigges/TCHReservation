package de.tigges.tchreservation.protocol;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/protocol")
public class ProtocolService {

	private ProtocolRepository protocolRepository;

	public ProtocolService(ProtocolRepository protocolRepository) {
		this.protocolRepository = protocolRepository;
	}

	@GetMapping("/get")
	public Iterable<Protocol> getAll() {
		return protocolRepository.findAll();
	}

	@GetMapping("/get/{time}")
	public Iterable<Protocol> getSince(@PathVariable Long time) {
		LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(time),
				TimeZone.getDefault().toZoneId());
		return protocolRepository.findByTimeGreaterThan(localDateTime);
	}
}
