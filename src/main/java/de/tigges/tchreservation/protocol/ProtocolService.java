package de.tigges.tchreservation.protocol;

import de.tigges.tchreservation.protocol.jpa.ProtocolEntity;
import de.tigges.tchreservation.protocol.jpa.ProtocolRepository;
import de.tigges.tchreservation.user.LoggedinUserService;
import de.tigges.tchreservation.user.model.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

@RestController
@RequestMapping("/rest/protocol")
@RequiredArgsConstructor
public class ProtocolService {

    private final ProtocolRepository protocolRepository;
    private final LoggedinUserService loggedinUserService;

    @GetMapping("/{time}")
    public Iterable<ProtocolEntity> getSince(@PathVariable Long time) {
        loggedinUserService.verifyHasRole(UserRole.ADMIN);
        return protocolRepository.findByTimeGreaterThanOrderByIdDesc(toLocalDateTime(time));
    }

    private static LocalDateTime toLocalDateTime(long time) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(time), TimeZone.getDefault().toZoneId());
    }
}
