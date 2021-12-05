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
import de.tigges.tchreservation.user.UserUtils;
import de.tigges.tchreservation.user.model.UserRole;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rest/protocol")
@RequiredArgsConstructor
public class ProtocolService {

	private final ProtocolRepository protocolRepository;
	private final UserUtils userUtils;

	@GetMapping("/{time}")
	public Iterable<ProtocolEntity> getSince(@PathVariable Long time) {
		userUtils.verifyHasRole(UserRole.ADMIN);
		LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(time),
				TimeZone.getDefault().toZoneId());
		return protocolRepository.findByTimeGreaterThanOrderByIdDesc(localDateTime);
	}
}
