package de.tigges.tchreservation.reservation;

import de.tigges.tchreservation.protocol.ActionType;
import de.tigges.tchreservation.protocol.EntityType;
import de.tigges.tchreservation.protocol.jpa.ProtocolEntity;
import de.tigges.tchreservation.protocol.jpa.ProtocolRepository;
import de.tigges.tchreservation.reservation.jpa.OccupationEntity;
import de.tigges.tchreservation.reservation.jpa.OccupationRepository;
import de.tigges.tchreservation.reservation.jpa.ReservationEntity;
import de.tigges.tchreservation.reservation.jpa.ReservationRepository;
import de.tigges.tchreservation.reservation.model.*;
import de.tigges.tchreservation.user.LoggedInUserService;
import de.tigges.tchreservation.user.UserMapper;
import de.tigges.tchreservation.user.UserUtils;
import de.tigges.tchreservation.user.jpa.UserEntity;
import de.tigges.tchreservation.user.model.UserRole;
import de.tigges.tchreservation.util.exception.AuthorizationException;
import de.tigges.tchreservation.util.exception.ErrorCode;
import de.tigges.tchreservation.util.exception.NotFoundException;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

import static de.tigges.tchreservation.reservation.model.RepeatType.weekly;

@RestController
@RequestMapping("/rest/reservation")
@RequiredArgsConstructor
@Log4j2
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final OccupationRepository occupationRepository;
    private final ReservationSystemConfigRepository systemConfigRepository;
    private final ProtocolRepository protocolRepository;
    private final ReservationValidator reservationValidator;
    private final OccupationValidator occupationValidator;
    private final LoggedInUserService loggedinUserService;

    private static int plusDays(@Nullable RepeatType repeatType) {
        if (weekly.equals(repeatType)) {
            return 7;
        }
        return 1;
    }

    private static LocalDate repeatUntil(Reservation reservation) {
        var repeatUntil = reservation.getRepeatUntil();
        return (repeatUntil != null) ? repeatUntil : reservation.getDate();
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody Reservation addReservation(@RequestBody Reservation reservation) {
        var loggedInUser = loggedinUserService.getLoggedInUser();
        var systemConfig = getSystemConfig(reservation.getSystemConfigId());

        reservationValidator.validateReservation(reservation, loggedInUser, systemConfig);
        reservation.setUser(UserMapper.map(loggedInUser));

        if (reservation.getOccupations().isEmpty()) {
            createOccupations(reservation);
        }

        reservationValidator.validateOccupations(reservation, loggedInUser, systemConfig);

        var savedReservation = reservationRepository.save(ReservationMapper.map(reservation));
        protocolRepository.save(new ProtocolEntity(savedReservation, ActionType.CREATE, loggedInUser));

        reservation.getOccupations().forEach(o -> {
            OccupationEntity occupationEntity = OccupationMapper.map(o);
            occupationEntity.setReservation(savedReservation);
            OccupationEntity saveOccupation = saveOccupation(occupationEntity, loggedInUser);
            o.setId(saveOccupation.getId());
        });
        var r = ReservationMapper.map(savedReservation);
        r.getOccupations().addAll(reservation.getOccupations());
        return r;
    }

    @PostMapping("/check")
    public @ResponseBody Reservation checkOccupations(@RequestBody Reservation reservation) {
        if (reservation.getOccupations().isEmpty()) {
            createOccupations(reservation);
        }
        var loggedInUser = loggedinUserService.getLoggedInUser();
        var systemConfig = getSystemConfig(reservation.getSystemConfigId());
        reservationValidator.validateOccupations(reservation, loggedInUser, systemConfig);
        return reservation;
    }

    @PutMapping("/occupation")
    @Transactional
    public @ResponseBody Occupation updateOccupation(@RequestBody Occupation occupation) {

        var dbOccupation = occupationRepository.findById(occupation.getId())
                .orElseThrow(notFoundException(EntityType.OCCUPATION, occupation.getId()));

        var loggedInUser = loggedinUserService.getLoggedInUser();
        var systemConfig = getSystemConfig(occupation.getSystemConfigId());

        occupationValidator.validateOccupation(occupation, loggedInUser, systemConfig);

        var occupationEntity = OccupationMapper.map(occupation);

        occupationEntity.setReservation(dbOccupation.getReservation());
        OccupationEntity savedOccupation = occupationRepository.save(occupationEntity);
        protocolRepository.save(new ProtocolEntity(savedOccupation, dbOccupation, loggedInUser));

        return OccupationMapper.map(savedOccupation);
    }

    @PutMapping("")
    @Transactional
    public @ResponseBody Reservation updateReservation(@RequestBody Reservation reservation) {

        var dbReservation = reservationRepository.findById(reservation.getId())
                .orElseThrow(notFoundException(EntityType.RESERVATION, reservation.getId()));

        var loggedInUser = loggedinUserService.getLoggedInUser();
        var systemConfig = getSystemConfig(reservation.getSystemConfigId());
        reservationValidator.validateReservation(reservation, loggedInUser, systemConfig);

        var savedReservation = reservationRepository.save(ReservationMapper.map(reservation));
        protocolRepository.save(new ProtocolEntity(savedReservation, dbReservation, loggedInUser));
        var response = ReservationMapper.map(savedReservation);

        var dbOccupations = occupationRepository.findByReservationId(reservation.getId());

        reservation.getOccupations().forEach(occupation -> {
            OccupationEntity dbOccupation = StreamSupport.stream(dbOccupations.spliterator(), false)
                    .filter(o -> occupation.getId() == o.getId()).findAny()
                    .orElseThrow(notFoundException(EntityType.OCCUPATION, occupation.getId()));
            occupation.setReservation(response);
            var savedOccupation = occupationRepository.save(OccupationMapper.map(occupation));
            protocolRepository.save(new ProtocolEntity(savedOccupation, dbOccupation, loggedInUser));
            response.getOccupations().add(OccupationMapper.map(savedOccupation));
        });

        return response;
    }

    @DeleteMapping("/occupation/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteOccupation(@PathVariable long id) {
        var occupation = occupationRepository.findById(id)
                .orElseThrow(notFoundException(EntityType.OCCUPATION, id));

        var loggedInUser = verifyCanDelete(occupation.getReservation().getUser().getId());

        protocolRepository.save(new ProtocolEntity(occupation, ActionType.DELETE, loggedInUser));
        occupationRepository.delete(occupation);
    }

    @DeleteMapping("/occupations/{ids}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteOccupations(@PathVariable String ids) {
        for (String id : ids.split(",")) {
            deleteOccupation(Long.parseLong(id));
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteReservation(@PathVariable long id) {
        var reservation = reservationRepository.findById(id)
                .orElseThrow(notFoundException(EntityType.RESERVATION, id));

        var loggedInUser = verifyCanDelete(reservation.getUser().getId());

        occupationRepository.findByReservationId(id).forEach(o -> this.deleteOccupation(o, loggedInUser));

        protocolRepository.save(new ProtocolEntity(reservation, ActionType.DELETE, loggedInUser));
        reservationRepository.delete(reservation);
    }

    private Supplier<NotFoundException> notFoundException(EntityType entityType, long id) {
        return () -> new NotFoundException(reservationValidator.validator.messageUtil, entityType, id);
    }

    @GetMapping("/id/{id}")
    public Optional<Reservation> getReservation(@PathVariable long id) {
        return reservationRepository.findById(id).map(this::map);
    }

    private Reservation map(ReservationEntity reservationEntity) {
        var reservation = ReservationMapper.map(reservationEntity);
        occupationRepository.findByReservationId(reservationEntity.getId())
                .forEach(o -> reservation.getOccupations().add(OccupationMapper.map(o)));
        return reservation;
    }

    @GetMapping("/occupation/{id}")
    public Optional<Occupation> getOccupation(@PathVariable long id) {
        return occupationRepository.findById(id).map(OccupationMapper::map);
    }

    @GetMapping("/getOccupations/{systemConfigId}/{date}")
    public Iterable<Occupation> getOccupations(@PathVariable Long systemConfigId, @PathVariable Long date) {

        var searchDate = date.equals(0L)
                ? LocalDate.now()
                : Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate();

        var occupations = occupationRepository.findBySystemConfigIdAndDate(systemConfigId, searchDate);

        return StreamSupport.stream(occupations.spliterator(), false)
                .map(OccupationMapper::map)
                .toList();
    }

    @GetMapping("/my")
    public Iterable<Reservation> getMyReservations() {
        var user = loggedinUserService.getLoggedInUser();
        return StreamSupport.stream(
                        reservationRepository
                                .findByUserOrderByDateDesc(user)
                                .spliterator(),
                        false)
                .map(ReservationMapper::map)
                .toList();
    }

    @GetMapping("/systemconfig/{id}")
    public @ResponseBody ReservationSystemConfig getSystemConfig(@PathVariable long id) {
        return systemConfigRepository.get(id);
    }

    private OccupationEntity saveOccupation(OccupationEntity o, UserEntity user) {
        var savedOccupation = occupationRepository.save(o);
        protocolRepository.save(new ProtocolEntity(savedOccupation, ActionType.CREATE, user));
        return savedOccupation;
    }

    private void deleteOccupation(OccupationEntity occupation, UserEntity user) {
        occupationRepository.delete(occupation);
        protocolRepository.save(new ProtocolEntity(occupation, ActionType.DELETE, user));
    }

    private void createOccupations(Reservation reservation) {

        var repeatUntil = repeatUntil(reservation);
        int plusDays = plusDays(reservation.getRepeatType());

        var occupationDate = reservation.getDate();
        while (!occupationDate.isAfter(repeatUntil)) {
            log.info("create occupation for {} until {} plus days {}", occupationDate, repeatUntil, plusDays);
            Occupation occupation = createOccupation(reservation);
            occupation.setDate(occupationDate);

            for (int court : reservation.getCourtsAsArray()) {
                if (occupation.getCourt() == 0) {
                    occupation.setCourt(court);
                    occupation.setLastCourt(court);
                } else if (court == occupation.getLastCourt() + 1) {
                    occupation.setLastCourt(court);
                } else {
                    reservation.getOccupations().add(occupation);
                    occupation = createOccupation(reservation);
                    occupation.setDate(occupationDate);
                    occupation.setCourt(court);
                    occupation.setLastCourt(court);
                }
            }
            if (occupation.getCourt() > 0) {
                reservation.getOccupations().add(occupation);
            }

            occupationDate = occupationDate.plusDays(plusDays);
        }
    }

    private Occupation createOccupation(Reservation reservation) {
        var occupation = new Occupation();
        occupation.setSystemConfigId(reservation.getSystemConfigId());
        occupation.setText(reservation.getText());
        occupation.setType(reservation.getType());
        occupation.setDate(reservation.getDate());
        occupation.setStart(reservation.getStart());
        occupation.setDuration(reservation.getDuration());
        return occupation;
    }

    private UserEntity verifyCanDelete(long userId) {
        var loggedInUser = loggedinUserService.getLoggedInUser();
        if (UserUtils.isActive(loggedInUser)
                && (UserUtils.is(loggedInUser, userId) || UserUtils.hasRole(loggedInUser, UserRole.ADMIN, UserRole.TRAINER))) {
            return loggedInUser;
        }
        throw new AuthorizationException(reservationValidator.validator.messageUtil, ErrorCode.USER_NOT_AUTHORIZED);
    }
}
