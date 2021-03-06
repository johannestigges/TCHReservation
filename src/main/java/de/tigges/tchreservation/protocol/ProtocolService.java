package de.tigges.tchreservation.protocol;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.tigges.tchreservation.protocol.jpa.ProtocolEntity;
import de.tigges.tchreservation.protocol.jpa.ProtocolRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/protocol")
@RequiredArgsConstructor
public class ProtocolService {

	private final ProtocolRepository protocolRepository;

	@GetMapping("/get")
	public Iterable<ProtocolEntity> getAll() {
		return protocolRepository.findAll();
	}

	@GetMapping("/get/{time}")
	public Iterable<ProtocolEntity> getSince(@PathVariable Long time) {
		LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(time),
				TimeZone.getDefault().toZoneId());
		return protocolRepository.findByTimeGreaterThan(localDateTime);
	}
}
