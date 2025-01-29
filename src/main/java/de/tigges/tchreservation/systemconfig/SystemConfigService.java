package de.tigges.tchreservation.systemconfig;

import de.tigges.tchreservation.exception.NotFoundException;
import de.tigges.tchreservation.protocol.ActionType;
import de.tigges.tchreservation.protocol.EntityType;
import de.tigges.tchreservation.protocol.jpa.ProtocolEntity;
import de.tigges.tchreservation.protocol.jpa.ProtocolRepository;
import de.tigges.tchreservation.reservation.model.ReservationSystemConfig;
import de.tigges.tchreservation.reservation.model.SystemConfigReservationType;
import de.tigges.tchreservation.systemconfig.jpa.ReservationTypeMapper;
import de.tigges.tchreservation.systemconfig.jpa.ReservationTypeRepository;
import de.tigges.tchreservation.systemconfig.jpa.SystemConfigEntity;
import de.tigges.tchreservation.systemconfig.jpa.SystemConfigRepository;
import de.tigges.tchreservation.user.LoggedinUserService;
import de.tigges.tchreservation.user.jpa.UserEntity;
import de.tigges.tchreservation.user.model.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static de.tigges.tchreservation.JpaUtil.stream;

@RestController
@RequestMapping("/rest/systemconfig")
@RequiredArgsConstructor
public class SystemConfigService {

    private final SystemConfigRepository systemConfigRepository;
    private final ReservationTypeRepository reservationTypeRepository;
    private final SystemConfigValidator systemConfigValidator;
    private final ProtocolRepository protocolRepository;
    private final LoggedinUserService loggedinUserService;

    @GetMapping("/getone/{id}")
    Optional<ReservationSystemConfig> getOne(@PathVariable Long id) {
        return systemConfigRepository.findById(id)
                .map(SystemConfigMapper::map)
                .map(this::setTypes);
    }

    @GetMapping("/getall")
    public List<ReservationSystemConfig> getAll() {
        return stream(systemConfigRepository.findAll())
                .map(SystemConfigMapper::map)
                .map(this::setTypes)
                .toList();
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public @ResponseBody ReservationSystemConfig add(@RequestBody ReservationSystemConfig config) {
        var loggedInUser = loggedinUserService.verifyHasRole(UserRole.ADMIN);
        systemConfigRepository.findById(config.id()).ifPresent(e -> {
            throw systemConfigValidator.validator.foundException(
                    EntityType.SYSTEM_CONFIGURATION, config.id());
        });
        systemConfigValidator.validate(config, loggedInUser);
        var entity = systemConfigRepository.save(SystemConfigMapper.map(config));
        protocolRepository.save(new ProtocolEntity(entity, ActionType.CREATE, loggedInUser));
        insertTypes(loggedInUser, config.types(), entity);
        return SystemConfigMapper.map(entity);
    }

    @PutMapping("")
    @Transactional
    public @ResponseBody ReservationSystemConfig update(@RequestBody ReservationSystemConfig config) {
        var loggedInUser = loggedinUserService.verifyHasRole(UserRole.ADMIN);
        systemConfigRepository.findById(config.id()).orElseThrow(notFoundException(config.id()));
        systemConfigValidator.validate(config, loggedInUser);
        var entity = SystemConfigMapper.map(config);
        var savedEntity = systemConfigRepository.save(SystemConfigMapper.map(config));
        protocolRepository.save(new ProtocolEntity(savedEntity, entity, loggedInUser));
        reservationTypeRepository.deleteBySystemConfigId(config.id());
        insertTypes(loggedInUser, config.types(), savedEntity);
        return SystemConfigMapper.map(savedEntity);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public @ResponseBody ReservationSystemConfig delete(@PathVariable long id) {
        var loggedInUser = loggedinUserService.verifyHasRole(UserRole.ADMIN);
        var entity = systemConfigRepository.findById(id).orElseThrow(notFoundException(id));
        reservationTypeRepository.deleteBySystemConfigId(id);
        protocolRepository.save(new ProtocolEntity(entity, ActionType.DELETE, loggedInUser));
        systemConfigRepository.delete(entity);
        return SystemConfigMapper.map(entity);
    }

    private ReservationSystemConfig setTypes(ReservationSystemConfig systemConfig) {
        return systemConfig.withTypes(getTypes(systemConfig.id()));
    }

    private List<SystemConfigReservationType> getTypes(long systemConfigId) {
        return stream(reservationTypeRepository.findBySystemConfigId(systemConfigId))
                .map(ReservationTypeMapper::mapEntity)
                .toList();
    }

    private void insertTypes(UserEntity loggedInUser,
                             Collection<SystemConfigReservationType> types,
                             SystemConfigEntity systemConfig) {
        if (types == null) {
            return;
        }
        types.forEach(type -> insertType(loggedInUser, type, systemConfig));
    }

    private void insertType(UserEntity loggedInUser,
                            SystemConfigReservationType type,
                            SystemConfigEntity systemConfig) {
        var entity = ReservationTypeMapper.mapType(type);
        entity.setSystemConfig(systemConfig);
        reservationTypeRepository.save(entity);
        protocolRepository.save(new ProtocolEntity(entity, ActionType.CREATE, loggedInUser));
    }

    private Supplier<NotFoundException> notFoundException(long id) {
        return () -> systemConfigValidator.validator.notFoundException(EntityType.SYSTEM_CONFIGURATION, id);
    }
}
