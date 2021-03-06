package de.tigges.tchreservation.reservation;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import de.tigges.tchreservation.exception.BadRequestException;
import de.tigges.tchreservation.exception.NotFoundException;
import de.tigges.tchreservation.protocol.ActionType;
import de.tigges.tchreservation.protocol.EntityType;
import de.tigges.tchreservation.protocol.jpa.ProtocolEntity;
import de.tigges.tchreservation.protocol.jpa.ProtocolRepository;
import de.tigges.tchreservation.reservation.jpa.OccupationEntity;
import de.tigges.tchreservation.reservation.jpa.OccupationRepository;
import de.tigges.tchreservation.reservation.jpa.ReservationEntity;
import de.tigges.tchreservation.reservation.jpa.ReservationRepository;
import de.tigges.tchreservation.reservation.model.Occupation;
import de.tigges.tchreservation.reservation.model.OccupationMapper;
import de.tigges.tchreservation.reservation.model.Reservation;
import de.tigges.tchreservation.reservation.model.ReservationMapper;
import de.tigges.tchreservation.reservation.model.ReservationSystemConfig;
import de.tigges.tchreservation.user.UserAwareService;
import de.tigges.tchreservation.user.jpa.UserEntity;
import de.tigges.tchreservation.user.jpa.UserRepository;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/reservation")
@Slf4j
public class ReservationService extends UserAwareService {

	private final ReservationRepository reservationRepository;
	private final OccupationRepository occupationRepository;
	private final ReservationSystemConfigRepository systemConfigRepository;
	private final ProtocolRepository protocolRepository;
	private final ReservationValidator reservationValidator;

	public ReservationService(ReservationRepository reservationRepository, OccupationRepository occupationRepository,
			ReservationSystemConfigRepository systemConfigRepository, UserRepository userRepository,
			ProtocolRepository protocolRepository, ReservationValidator reservationChecker) {
		super(userRepository);
		this.reservationRepository = reservationRepository;
		this.occupationRepository = occupationRepository;
		this.systemConfigRepository = systemConfigRepository;
		this.protocolRepository = protocolRepository;
		this.reservationValidator = reservationChecker;
	}

	/**
	 * add one Reservation to the system
	 * 
	 * If the reservation has no Occupations, then all Occupations will be
	 * generated.
	 * 
	 * @param reservation
	 * @return {@link ResponseBody}
	 */
	@PostMapping("/add")
	@ResponseStatus(HttpStatus.CREATED)
	public @ResponseBody Reservation addReservation(@RequestBody Reservation reservation) {

		log.info("add reservation {}", reservation.getText());
		UserEntity loggedInUser = getLoggedInUser();

		reservationValidator.validateReservation(reservation, loggedInUser);

		if (reservation.getOccupations().isEmpty()) {
			createOccupations(reservation);
		}

		reservationValidator.validateOccupations(reservation, loggedInUser);

		ReservationEntity savedReservation = reservationRepository.save(ReservationMapper.map(reservation));
		protocolRepository.save(new ProtocolEntity(savedReservation, ActionType.CREATE, loggedInUser));

		reservation.getOccupations().forEach(o -> {
			OccupationEntity occupationEntity = OccupationMapper.map(o);
			occupationEntity.setReservation(savedReservation);
			OccupationEntity saveOccupation = saveOccupation(occupationEntity, loggedInUser);
			o.setId(saveOccupation.getId());
		});
		Reservation r = ReservationMapper.map(savedReservation);
		r.getOccupations().addAll(reservation.getOccupations());
		return r;
	}

	/**
	 * checks all Occupations of one Reservation
	 * 
	 * @param reservation
	 * @return checked Reservation or {@link BadRequestException}
	 */
	@GetMapping("/checkOccupations")
	public @ResponseBody Reservation checkOccupations(@RequestBody Reservation reservation) {

		log.info("check occupations for reservation {}", reservation.getText());
		UserEntity loggedInUser = getLoggedInUser();

		if (reservation.getOccupations().isEmpty()) {
			createOccupations(reservation);
		}

		// validate occupations
		reservationValidator.validateOccupations(reservation, loggedInUser);

		return reservation;
	}

	/**
	 * update one Occupation
	 * 
	 * @param occupation
	 * @return saved occupation or {@link BadRequestException} or
	 *         {@link NotFoundException}
	 */
	@PutMapping("/update/occupation")
	@Transactional
	public @ResponseBody Occupation updateOccupation(@RequestBody Occupation occupation) {

		log.info("update occupation {}", occupation.getText());

		OccupationEntity dbOccupation = occupationRepository.findById(occupation.getId())
				.orElseThrow(() -> new NotFoundException(EntityType.OCCUPATION, occupation.getId()));

		UserEntity loggedInUser = getLoggedInUser();

		reservationValidator.validateOccupation(occupation, loggedInUser);

		OccupationEntity occupationEntity = OccupationMapper.map(occupation);
		occupationEntity.setReservation(dbOccupation.getReservation());
		OccupationEntity savedOccupation = occupationRepository.save(occupationEntity);
		protocolRepository.save(new ProtocolEntity(savedOccupation, dbOccupation, loggedInUser));

		return OccupationMapper.map(savedOccupation);
	}

	/**
	 * remove one Occupation
	 * 
	 * @param id
	 */
	@DeleteMapping("/delete/occupation/{id}")
	@ResponseStatus(HttpStatus.OK)
	public void deleteOccupation(@PathVariable long id) {

		log.info("delete occupation {}", id);

		OccupationEntity occupation = occupationRepository.findById(id)
				.orElseThrow(() -> new NotFoundException(EntityType.OCCUPATION, id));

		protocolRepository.save(new ProtocolEntity(occupation, ActionType.DELETE, getLoggedInUser()));
		occupationRepository.delete(occupation);
	}

	/**
	 * delete one Reservation with all Occupations
	 * 
	 * @param id
	 */
	@DeleteMapping("/delete/{id}")
	@ResponseStatus(HttpStatus.OK)
	public void deleteReservation(@PathVariable long id) {

		log.info("delete reservation {}", id);

		UserEntity loggedInUser = getLoggedInUser();

		occupationRepository.findByReservationId(id).forEach(o -> this.deleteOccupation(o, loggedInUser));

		ReservationEntity reservation = reservationRepository.findById(id)
				.orElseThrow(() -> new NotFoundException(EntityType.RESERVATION, id));
		protocolRepository.save(new ProtocolEntity(reservation, ActionType.DELETE, loggedInUser));
		reservationRepository.delete(reservation);
	}

	/**
	 * get one reservation by id
	 * <p>
	 * All occupations of the reservation are included
	 * 
	 * @param reservationId
	 * @return all reservations belonging to that user
	 */
	@GetMapping("/get/{id}")
	public Optional<Reservation> getReservation(@PathVariable long id) {
		log.info("get reservation {}", id);
		return reservationRepository.findById(id).map(this::map);
	}

	private Reservation map(ReservationEntity reservationEntity) {
		Reservation reservation = ReservationMapper.map(reservationEntity);
		occupationRepository.findByReservationId(reservationEntity.getId())
				.forEach(o -> reservation.getOccupations().add(OccupationMapper.map(o)));
		return reservation;
	}

	/**
	 * get one occupation by id
	 * 
	 * @param id
	 * @return {@link Occupation}
	 */
	@GetMapping("/get/occupation/{id}")
	public Optional<Occupation> getOccupation(@PathVariable long id) {
		log.info("get occupation {}", id);

		return occupationRepository.findById(id).map(OccupationMapper::map);
	}

	/**
	 * get all occupations of one day
	 * 
	 * @return list of all occupations for one day
	 */
	@GetMapping("/getOccupations/{systemConfigId}/{date}")
	public Iterable<Occupation> getOccupations(@PathVariable Long systemConfigId, @PathVariable Long date) {

		LocalDate searchDate;
		if (date.equals(0L)) {
			searchDate = LocalDate.now();
		} else {
			searchDate = Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate();
		}

		log.info("get occupations for date {} ({})", searchDate.toString(), date);

		Iterable<OccupationEntity> occupations = occupationRepository.findBySystemConfigIdAndDate(systemConfigId,
				searchDate);

		return StreamSupport.stream(occupations.spliterator(), false).map(OccupationMapper::map)
				.collect(Collectors.toList());
	}

	/**
	 * get the reservation system configuration
	 * 
	 * @param systemId
	 * @return
	 */
	@GetMapping("/systemconfig/{id}")
	public @ResponseBody ReservationSystemConfig getSystemConfig(@PathVariable long id) {
		return systemConfigRepository.get(id);
	}

	private OccupationEntity saveOccupation(OccupationEntity o, UserEntity user) {
		OccupationEntity savedOccupation = occupationRepository.save(o);
		protocolRepository.save(new ProtocolEntity(savedOccupation, ActionType.CREATE, user));
		return savedOccupation;
	}

	private void deleteOccupation(OccupationEntity occupation, UserEntity user) {
		occupationRepository.delete(occupation);
		protocolRepository.save(new ProtocolEntity(occupation, ActionType.DELETE, user));
	}

	/**
	 * create all occupations for a reservation
	 * 
	 * @param reservation
	 * @return list of created occupations
	 */
	private void createOccupations(Reservation reservation) {

		LocalDate occupationDate = reservation.getDate();
		LocalDate repeatUntil = reservation.getDate();
		if (reservation.getWeeklyRepeatUntil() != null) {
			repeatUntil = reservation.getWeeklyRepeatUntil();
		}
		while (!occupationDate.isAfter(repeatUntil)) {
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

			occupationDate = occupationDate.plusDays(7);
		}
	}

	private Occupation createOccupation(Reservation reservation) {
		Occupation occupation = new Occupation();
		occupation.setSystemConfigId(reservation.getSystemConfigId());
		occupation.setText(reservation.getText());
		occupation.setType(reservation.getType());
		occupation.setDate(reservation.getDate());
		occupation.setStart(reservation.getStart());
		occupation.setDuration(reservation.getDuration());
		return occupation;
	}
}
