package de.tigges.tchreservation.reservation;

import de.tigges.tchreservation.exception.NotFoundException;
import de.tigges.tchreservation.protocol.EntityType;
import de.tigges.tchreservation.reservation.model.ReservationSystemConfig;
import de.tigges.tchreservation.systemconfig.SystemConfigMapper;
import de.tigges.tchreservation.systemconfig.jpa.ReservationTypeRepository;
import de.tigges.tchreservation.systemconfig.jpa.SystemConfigEntity;
import de.tigges.tchreservation.systemconfig.jpa.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@RequiredArgsConstructor
public class ReservationSystemConfigRepository {

	private final SystemConfigRepository repository;
	private final ReservationTypeRepository reservationTypeRepository;

	public ReservationSystemConfig get(long id) {
		return repository
				.findById(id)
				.map(this::addTypes)
				.map(SystemConfigMapper::map)
				.orElseThrow(() -> new NotFoundException(EntityType.SYSTEM_CONFIGURATION, id));
	}

	private SystemConfigEntity addTypes(SystemConfigEntity systemConfig) {
		var typesIterator = reservationTypeRepository.findBySystemConfigId(systemConfig.getId());
		var types = StreamSupport.stream(typesIterator.spliterator(), false)
				.collect(Collectors.toSet());
		systemConfig.setTypes(types);
		return systemConfig;
	}
}
