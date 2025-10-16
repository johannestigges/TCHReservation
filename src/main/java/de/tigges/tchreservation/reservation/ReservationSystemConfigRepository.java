package de.tigges.tchreservation.reservation;

import de.tigges.tchreservation.protocol.EntityType;
import de.tigges.tchreservation.reservation.model.ReservationSystemConfig;
import de.tigges.tchreservation.systemconfig.SystemConfigMapper;
import de.tigges.tchreservation.systemconfig.jpa.ReservationTypeRepository;
import de.tigges.tchreservation.systemconfig.jpa.SystemConfigEntity;
import de.tigges.tchreservation.systemconfig.jpa.SystemConfigRepository;
import de.tigges.tchreservation.util.StreamUtil;
import de.tigges.tchreservation.util.exception.NotFoundException;
import de.tigges.tchreservation.util.message.MessageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ReservationSystemConfigRepository {

    private final SystemConfigRepository repository;
    private final ReservationTypeRepository reservationTypeRepository;
    private final MessageUtil messageUtil;

    public ReservationSystemConfig get(long id) {
        return repository
                .findById(id)
                .map(this::addTypes)
                .map(SystemConfigMapper::map)
                .orElseThrow(() -> new NotFoundException(messageUtil, EntityType.SYSTEM_CONFIGURATION, id));
    }

    private SystemConfigEntity addTypes(SystemConfigEntity systemConfig) {
        Optional.of(systemConfig)
                .map(SystemConfigEntity::getId)
                .map(reservationTypeRepository::findBySystemConfigId)
                .map(StreamUtil::stream)
                .map(s -> s.collect(Collectors.toSet()))
                .ifPresent(systemConfig::setTypes);
        return systemConfig;
    }
}
