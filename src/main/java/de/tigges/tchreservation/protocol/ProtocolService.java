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
import de.tigges.tchreservation.user.UserAwareService;
import de.tigges.tchreservation.user.jpa.UserRepository;

@RestController
@RequestMapping("/rest/protocol")
public class ProtocolService extends UserAwareService {

	private final ProtocolRepository protocolRepository;

	public ProtocolService(ProtocolRepository protocolRepository, UserRepository userService) {
		super(userService);
		this.protocolRepository = protocolRepository;
	}

	@GetMapping("/{time}")
	public Iterable<ProtocolEntity> getSince(@PathVariable Long time) {
		verifyIsAdmin();
		LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(time),
				TimeZone.getDefault().toZoneId());
		return protocolRepository.findByTimeGreaterThanOrderByIdDesc(localDateTime);
	}
}
