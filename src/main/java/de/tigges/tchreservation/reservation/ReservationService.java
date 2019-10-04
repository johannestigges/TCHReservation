package de.tigges.tchreservation.reservation;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@RestController
@RequestMapping("/reservation")
public class ReservationService extends UserAwareService {

	public static final Logger logger = LoggerFactory.getLogger(ReservationService.class);

	private ReservationRepository reservationRepository;
	private OccupationRepository occupationRepository;
	private ReservationSystemConfigRepository systemConfigRepository;
	private ProtocolRepository protocolRepository;
	private ReservationValidator reservationValidator;

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

	@PostMapping("/add")
	@ResponseStatus(HttpStatus.CREATED)
	public @ResponseBody Reservation addReservation(@RequestBody Reservation reservation) {

		logger.info("add reservation {}", reservation.getText());
		UserEntity loggedInUser = getLoggedInUser();

		// check reservation data consistency
		checkOccupations(reservation);

		// save occupations and reservation
		ReservationEntity savedReservation = reservationRepository.save(ReservationMapper.map(reservation));
		protocolRepository.save(new ProtocolEntity(savedReservation, ActionType.CREATE, loggedInUser));

		reservation.getOccupations().forEach(o -> {
			OccupationEntity occupationEntity = OccupationMapper.map(o);
			occupationEntity.setReservation(savedReservation);
			saveOccupation(occupationEntity, loggedInUser);
		});
		return ReservationMapper.map(savedReservation);
	}

	@GetMapping("/checkOccupations")
	public @ResponseBody Reservation checkOccupations(@RequestBody Reservation reservation) {

		logger.info("check occupations for reservation {}", reservation.getText());
		UserEntity loggedInUser = getLoggedInUser();

		// validate reservation
		reservationValidator.validateReservation(reservation, loggedInUser);

		if (reservation.getOccupations().isEmpty()) {
			createOccupations(reservation).forEach(o -> reservation.addOccupation(o));
		}

		// validate occupations
		reservationValidator.validateOccupations(reservation, loggedInUser);

		return reservation;
	}

	@PutMapping("/update")
	@Transactional
	public @ResponseBody Reservation updateReservation(@RequestBody Reservation reservation) {

		logger.info("update reservation {}", reservation.getText());

		ReservationEntity dbReservation = reservationRepository.findById(reservation.getId())
				.orElseThrow(() -> new NotFoundException(EntityType.RESERVATION, reservation.getId()));

		UserEntity loggedInUser = getLoggedInUser();

		if (reservation.getOccupations().isEmpty()) {
			createOccupations(reservation).forEach(o -> reservation.addOccupation(o));
		} else {
			reservation.getOccupations().forEach(o -> o.setReservation(reservation));
		}

		// check reservation data consistency
		reservationValidator.validateReservation(reservation, loggedInUser);

		ReservationEntity savedReservation = reservationRepository.save(ReservationMapper.map(reservation));
		protocolRepository.save(new ProtocolEntity(savedReservation, dbReservation, loggedInUser));

		occupationRepository.deleteByReservationId(reservation.getId());
		reservation.getOccupations().forEach(o -> saveOccupation(OccupationMapper.map(o), loggedInUser));
		return ReservationMapper.map(savedReservation);
	}

	@DeleteMapping("/delete/{id}")
	@ResponseStatus(HttpStatus.OK)
	public void delete(@PathVariable long id) {

		logger.info("delete reservation {}", id);

		ReservationEntity reservation = reservationRepository.findById(id)
				.orElseThrow(() -> new NotFoundException(EntityType.RESERVATION, id));

		UserEntity loggedInUser = getLoggedInUser();

		occupationRepository.findByReservationId(id).forEach(o -> deleteOccupation(o, loggedInUser));
		reservationRepository.delete(reservation);
		protocolRepository.save(new ProtocolEntity(reservation, ActionType.DELETE, loggedInUser));
	}

	/**
	 * get one reservation by key
	 * <p>
	 * All occupations of the reservation are included
	 * 
	 * @param reservationId
	 * @return all reservations belonging to that user
	 */
	@GetMapping("/get/{id}")
	public Optional<Reservation> getReservation(@PathVariable long id) {

		logger.info("get reservation {}", id);

		Optional<Reservation> reservation = reservationRepository.findById(id).map(ReservationMapper::map);

		reservation.ifPresent(r -> occupationRepository.findByReservationId(r.getId())
				.forEach(o -> r.addOccupation(OccupationMapper.map(o))));

		return reservation;
	}

	/**
	 * get all occupations for one day
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

		logger.info("get occupations for date {} ({})", searchDate.toString(), date);

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
	private List<Occupation> createOccupations(Reservation reservation) {
		List<Occupation> occupations = new ArrayList<>();

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
					occupations.add(occupation);
					occupation = createOccupation(reservation);
					occupation.setDate(occupationDate);
					occupation.setCourt(court);
					occupation.setLastCourt(court);
				}
			}
			if (occupation.getCourt() > 0) {
				occupations.add(occupation);
			}

			occupationDate = occupationDate.plusDays(7);
		}
		return occupations;
	}

	private Occupation createOccupation(Reservation reservation) {
		Occupation occupation = new Occupation();
		occupation.setSystemConfigId(reservation.getSystemConfigId());
		occupation.setReservation(reservation);
		occupation.setText(reservation.getText());
		occupation.setType(reservation.getType());
		occupation.setDate(reservation.getDate());
		occupation.setStart(reservation.getStart());
		occupation.setDuration(reservation.getDuration());
		return occupation;
	}
}
