package de.tigges.tchreservation.reservation;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tigges.tchreservation.ProtocolTest;
import de.tigges.tchreservation.TchReservationApplication;
import de.tigges.tchreservation.protocol.ActionType;
import de.tigges.tchreservation.reservation.jpa.OccupationEntity;
import de.tigges.tchreservation.reservation.jpa.OccupationRepository;
import de.tigges.tchreservation.reservation.jpa.ReservationEntity;
import de.tigges.tchreservation.reservation.jpa.ReservationRepository;
import de.tigges.tchreservation.reservation.model.*;
import de.tigges.tchreservation.systemconfig.jpa.ReservationTypeEntity;
import de.tigges.tchreservation.systemconfig.jpa.ReservationTypeRepository;
import de.tigges.tchreservation.systemconfig.jpa.SystemConfigEntity;
import de.tigges.tchreservation.systemconfig.jpa.SystemConfigRepository;
import de.tigges.tchreservation.user.UserMapper;
import de.tigges.tchreservation.user.jpa.UserEntity;
import de.tigges.tchreservation.user.model.ActivationStatus;
import de.tigges.tchreservation.user.model.UserRole;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TchReservationApplication.class)
@ActiveProfiles("test")
public class ReservationServiceTest extends ProtocolTest {

    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private OccupationRepository occupationRepository;
    @Autowired
    private SystemConfigRepository systemConfigRepository;
    @Autowired
    private ReservationTypeRepository reservationTypeRepository;
    private UserEntity user;
    private UserEntity trainer;
    private UserEntity admin;

    @BeforeEach
    public void init() {
        this.protocolRepository.deleteAll();
        this.occupationRepository.deleteAll();
        this.reservationRepository.deleteAll();
        this.userRepository.deleteAll();
        this.reservationTypeRepository.deleteAll();
        this.systemConfigRepository.deleteAll();

        user = addUser(UserRole.REGISTERED);
        trainer = addUser(UserRole.TRAINER);
        admin = addUser(UserRole.ADMIN);

        SystemConfigEntity system1 = new SystemConfigEntity();
        system1.setId(1L);
        system1.setName("Platzbelegung");
        system1.setCourts("Platz 1, Platz 2\tPlatz 3,Platz 4,Platz 5, Platz 6");
        system1.setDurationUnitInMinutes(30);
        system1.setMaxDuration(3);
        system1.setMaxDaysReservationInFuture(1);
        system1.setOpeningHour(8);
        system1.setClosingHour(22);
        system1.setTypes(Set.of(
                createReservationType(0, "Quickbuchung", 3, 60, 0,
                        UserRole.REGISTERED, UserRole.TRAINER, UserRole.KIOSK, UserRole.TECHNICAL, UserRole.TEAMSTER),
                createReservationType(1, "Training", UserRole.TRAINER, UserRole.TEAMSTER),
                createReservationType(2, "Meisterschaft", UserRole.TRAINER, UserRole.TEAMSTER),
                createReservationType(3, "Dauerbuchung"),
                createReservationType(4, "Gesperrt", UserRole.TRAINER)
        ));
        systemConfigRepository.save(system1);
        saveTypes(system1);

        SystemConfigEntity system2 = new SystemConfigEntity();
        system2.setId(2L);
        system2.setName("Hallenplätze");
        system2.setCourts("Center Court\tNebenplatz");
        system2.setDurationUnitInMinutes(60);
        system2.setMaxDuration(2);
        system2.setMaxDaysReservationInFuture(14);
        system2.setOpeningHour(8);
        system2.setClosingHour(22);
        system2.setTypes(Set.of(
                createReservationType(0, "Quickbuchung", 1, 14, 8,
                        UserRole.REGISTERED, UserRole.TRAINER, UserRole.KIOSK, UserRole.TECHNICAL, UserRole.TEAMSTER, UserRole.GUEST),
                createReservationType(1, "Training", UserRole.TRAINER, UserRole.TEAMSTER),
                createReservationType(2, "Meisterschaft", UserRole.TRAINER, UserRole.TEAMSTER),
                createReservationType(3, "Dauerbuchung)"),
                createReservationType(4, "Gesperrt", UserRole.TRAINER),
                createReservationType(5, "Jugendtraining", 1, 1, 0,
                        UserRole.REGISTERED, UserRole.TEAMSTER)
        ));
        systemConfigRepository.save(system2);
        saveTypes(system2);

        SystemConfigEntity system3 = new SystemConfigEntity();
        system3.setId(3L);
        system3.setName("Testsystem für Unittests");
        system3.setCourts("Center Court");
        system3.setDurationUnitInMinutes(30);
        system3.setMaxDuration(3);
        system3.setMaxDaysReservationInFuture(2);
        system3.setOpeningHour(8);
        system3.setClosingHour(10);
        system3.setTypes(Set.of(
                createReservationType(0, "Quickbuchung", 1, 0, 0, UserRole.TRAINER, UserRole.REGISTERED)
        ));
        systemConfigRepository.save(system3);
        saveTypes(system3);
    }

    private void saveTypes(SystemConfigEntity systemConfig) {
        systemConfig.getTypes().forEach(type -> {
            type.setSystemConfig(systemConfig);
            reservationTypeRepository.save(type);
        });
    }

    @Test
    @WithMockUser(username = "TRAINER")
    public void addReservation() throws Exception {
        addReservation(createReservation(1, 1, 10, 2));
    }

    @Test
    @WithMockUser(username = "TRAINER")
    public void addReservationWithUser() throws Exception {
        addReservation(createReservation(1, trainer, 1, 10, 2));
    }

    @Test
    @WithMockUser(username = "TRAINER")
    public void addReservationWithWrongUser() throws Exception {
        addReservationError(createReservation(1, user, 1, 10, 2), HttpStatus.UNAUTHORIZED,
                "Der an der Reservierung angegebene Benutzer ist nicht der angemeldete Benutzer.");
    }

    @Test
    @WithMockUser(username = "TRAINER")
    public void addReservationInDifferentSystemConfigs() throws Exception {
        addReservation(createReservation(1, 1, 10, 1));
        addReservation(createReservation(2, 1, 10, 1));
    }

    @Test
    @WithMockUser(username = "TRAINER")
    public void addReservationWithDifferentCourts() throws Exception {
        addReservation(createReservation(1, 1, 10, 2));
        addReservation(createReservation(1, 2, 10, 2));
    }

    @Test
    @WithMockUser(username = "TRAINER")
    public void addReservationWithDifferentTimes() throws Exception {
        addReservation(createReservation(2, 1, 10, 1));
        addReservation(createReservation(2, 1, 11, 1));
    }

    @Test
    @WithMockUser(username = "TRAINER")
    public void addReservationOverlap() throws Exception {
        addReservation(createReservation(1, 1, 10, 3));
        addReservationOverlap(createReservation(1, 1, 11, 2), 0);
    }

    @Test
    @WithMockUser(username = "TRAINER")
    public void addReservationDuplicate() throws Exception {
        addReservation(createReservation(1, 1, 10, 3));
        addReservationOverlap(createReservation(1, 1, 10, 3), 0);
    }

    @Test
    @WithMockUser(username = "TRAINER")
    public void addReservationOverlap2() throws Exception {
        addReservation(createReservation(1, 1, 10, 6, 1));
        addReservationOverlap(createReservation(1, 1, 11, 2, 1), 0);
    }

    @Test
    @WithMockUser(username = "TRAINER")
    public void addReservationOverlap3() throws Exception {
        addReservation(createReservation(2, 1, 17, 1, 1));
        addReservationOverlap(createReservation(2, 1, 16, 3, 1), 0);
    }

    @Test
    @WithMockUser(username = "TRAINER")
    public void addReservationNotOverlapDifferentSystemConfig() throws Exception {
        addReservation(createReservation(1, 1, 11, 2, 1));
        addReservation(createReservation(2, 1, 11, 2, 1));
    }

    @Test
    @WithMockUser(username = "ADMIN")
    public void addReservationNotOverlapDifferentDays() throws Exception {
        Reservation reservation = createReservation(1, 1, 11, 2);
        reservation.setDate(reservation.getDate().plusDays(1));
        addReservation(reservation);
        addReservation(createReservation(1, 1, 11, 2));
    }

    @Test
    @WithMockUser(username = "REGISTERED")
    public void addReservationNoText() throws Exception {
        Reservation reservation = createReservation(1, 1, 8, 2);
        reservation.setText(null);
        addReservationWithOccupationFieldError(reservation, "text", "Bitte geben Sie einen Wert an");
    }

    @Test
    @WithMockUser(username = "TRAINER")
    public void addReservationNoDate() throws Exception {
        Reservation reservation = createReservation(1, 1, 8, 2);
        reservation.setDate(null);
        addReservationWithFieldError(reservation, "date", "Bitte geben Sie einen Wert an");
    }

    @Test
    @WithMockUser(username = "TRAINER")
    public void addReservationNoStart() throws Exception {
        Reservation reservation = createReservation(1, 1, 8, 2);
        reservation.setStart(null);
        addReservationWithFieldError(reservation, "start", "Bitte geben Sie einen Wert an");
    }

    @Test
    @WithMockUser(username = "TRAINER")
    public void addReservationBeforeOpeningHour() throws Exception {
        Reservation reservation = createReservation(1, 1, 6, 2);
        addReservationWithOccupationFieldError(reservation, "start",
                "Startzeit 06:00 liegt vor der Öffnungszeit 08:00.");
    }

    @Test
    @WithMockUser(username = "TRAINER")
    public void addReservationAfterClosingHour() throws Exception {
        Reservation reservation = createReservation(1, 1, 23, 2);
        addReservationWithOccupationFieldError(reservation, "start",
                "Startzeit 23:00 liegt später als die Endezeit 22:00");
    }

    @Test
    @WithMockUser(username = "TRAINER")
    public void addReservationInvalidDurationUnits() throws Exception {
        Reservation reservation = createReservation(1, 1, 0, 2);
        reservation.setStart(LocalTime.of(9, 25));
        addReservationWithOccupationFieldError(reservation, "start", "fehlerhafte Startzeit mit 25 Minuten.");
    }

    @Test
    @WithMockUser(username = "TRAINER")
    public void addReservationExtendsClosingHour() throws Exception {
        Reservation reservation = createReservation(2, 1, 22, 1);
        addReservationWithOccupationFieldError(reservation, "start", "Startzeit + Dauer zu spät.");
    }

    @Test
    @WithMockUser(username = "TRAINER")
    public void addReservationDurationTooSmall() throws Exception {
        Reservation reservation = createReservation(1, 1, 8, 0);
        addReservationWithOccupationFieldError(reservation, "duration", "Dauer zu gering");
    }

    @Test
    @WithMockUser(username = "REGISTERED")
    public void addReservationDurationTooBig() throws Exception {
        Reservation reservation = createReservation(3, 1, 8, 4);
        reservation.setDate(reservation.getDate().plusDays(1));
        addReservationWithOccupationFieldError(reservation, "duration",
                "Der Benutzer REGISTERED hat nicht die Rechte, eine Reservierung der Dauer 4 anzulegen.");
    }

    @Test
    @WithMockUser(username = "TRAINER")
    public void addReservationDurationEvenTooBigForAdmin() throws Exception {
        Reservation reservation = createReservation(3, 1, 8, 4);
        reservation.setDate(reservation.getDate().plusDays(1));
        addReservationWithOccupationFieldError(reservation, "duration",
                "Der Benutzer TRAINER hat nicht die Rechte, eine Reservierung der Dauer 4 anzulegen.");
    }

    @Test
    @WithMockUser(username = "TRAINER")
    public void addReservationNoCourts() throws Exception {
        Reservation reservation = createReservation(1, 0, 8, 2);
        reservation.setCourts(null);
        addReservationWithFieldError(reservation, "court", "Bitte geben Sie einen Wert an");
    }

    @Test
    @WithMockUser(username = "TRAINER")
    public void addReservationTooManyCourts() throws Exception {
        Reservation reservation = createReservation(1, 0, 8, 2);
        reservation.setCourtsFromInteger(1, 2, 3, 4, 5, 6, 7);
        addReservationWithFieldError(reservation, "court", "Platznummer 7 zu groß. Mehr als 6 Plätze gibt es nicht.");
    }

    @Test
    @WithMockUser(username = "TRAINER")
    public void addReservationCourtTooSmall() throws Exception {
        Reservation reservation = createReservation(1, 0, 8, 2);
        addReservationWithFieldError(reservation, "court", "Platznummer 0 zu klein");
    }

    @Test
    @WithMockUser(username = "TRAINER")
    public void addReservationCourtTooBig() throws Exception {
        Reservation reservation = createReservation(1, 7, 8, 2);
        addReservationWithFieldError(reservation, "court", "Platznummer 7 zu groß. Mehr als 6 Plätze gibt es nicht.");
    }

    @Test
    @WithMockUser(username = "TRAINER")
    public void addReservationMultipleCourts() throws Exception {
        checkReservationMultipleCourts(8, 1, 1, 2);
        checkReservationMultipleCourts(9, 1, 1, 2, 3, 4, 5, 6);
        checkReservationMultipleCourts(10, 2, 1, 3);
        checkReservationMultipleCourts(11, 2, 1, 2, 4, 5, 6);
        checkReservationMultipleCourts(12, 3, 2, 4, 6);
    }

    private void checkReservationMultipleCourts(int start, int expectedOccupations, int... courts) throws Exception {
        Reservation reservation = createReservation(1, 1, start, 1, 1);
        reservation.setCourtsFromInteger(courts);
        Reservation savedReservation = getReservation(addReservation(reservation));
        Iterable<OccupationEntity> occupations = occupationRepository.findByReservationId(savedReservation.getId());
        assertThat(StreamSupport.stream(occupations.spliterator(), false)).hasSize(expectedOccupations);
    }

    @Test
    @WithMockUser(username = "ADMIN")
    public void addReservationDailyRepeat() throws Exception {
        checkReservationRepeatDaily(8, 3, 4, 2);
        checkReservationRepeatDaily(8, 1, 2, 3);
    }

    private Reservation checkReservationRepeatDaily(int hour, int repeatDays, int expectedOccupations, int... courts)
            throws Exception {
        Reservation reservation = createReservation(1, 1, hour, 2);
        reservation.setRepeatUntil(reservation.getDate().plusDays(repeatDays));
        reservation.setRepeatType(RepeatType.daily);
        reservation.setCourtsFromInteger(courts);
        Reservation savedReservation = getReservation(addReservation(reservation));
        Iterable<OccupationEntity> occupations = occupationRepository.findByReservationId(savedReservation.getId());
        assertThat(StreamSupport.stream(occupations.spliterator(), false)).hasSize(expectedOccupations);

        return savedReservation;
    }

    @Test
    @WithMockUser(username = "ADMIN")
    public void addReservationWeeklyRepeat() throws Exception {
        checkReservationRepeatWeekly(8, 2, 1, 1);
        checkReservationRepeatWeekly(8, 7, 2, 2);
        checkReservationRepeatWeekly(8, 21, 8, 3, 5);
        checkReservationRepeatWeekly(9, 20, 6, 3, 5);
        checkReservationRepeatWeekly(10, 21, 8, 3, 5);
        checkReservationRepeatWeekly(11, 21, 8, 3, 5);
    }

    private void checkReservationRepeatWeekly(int hour, int repeatDays, int expectedOccupations, int... courts)
            throws Exception {
        Reservation reservation = createReservation(1, 1, hour, 2);
        reservation.setRepeatUntil(reservation.getDate().plusDays(repeatDays));
        reservation.setRepeatType(RepeatType.weekly);
        reservation.setCourtsFromInteger(courts);
        Reservation savedReservation = getReservation(addReservation(reservation));
        Iterable<OccupationEntity> occupations = occupationRepository.findByReservationId(savedReservation.getId());
        assertThat(StreamSupport.stream(occupations.spliterator(), false)).hasSize(expectedOccupations);
    }

    @Test
    @WithMockUser(username = "TRAINER")
    public void addReservationNoRepeatUntil() throws Exception {
        Reservation reservation = createReservation(1, 3, 12, 2);
        reservation.setRepeatType(RepeatType.daily);
        addReservationWithFieldError(reservation, "repeatUntil", "Kein Datum Wiederholung bis angegeben");
    }

    @Test
    @WithMockUser(username = "TRAINER")
    public void addReservationRepeatUntilBeforeStart() throws Exception {
        Reservation reservation = createReservation(1, 3, 12, 2);
        reservation.setRepeatType(RepeatType.daily);
        reservation.setRepeatUntil(reservation.getDate().minusDays(1));
        addReservationWithFieldError(reservation, "repeatUntil",
                "Datum Wiederholung bis muss hinter dem Reservierungsdatum liegen");
    }

    @Test
    @WithMockUser(username = "TRAINER")
    public void addReservationNull() throws Exception {
        addReservationNoCheck(null).andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "TRAINER")
    public void addReservationNoSystemConfig() throws Exception {
        addReservationError(createReservation(0, 1, 10, 6), HttpStatus.NOT_FOUND,
                "SYSTEM_CONFIGURATION with id 0 not found");
    }

    @Test
    @WithMockUser(username = "TRAINER")
    public void addReservationUnknownSystemConfig() throws Exception {
        addReservationError(createReservation(10, 1, 10, 6), HttpStatus.NOT_FOUND,
                "SYSTEM_CONFIGURATION with id 10 not found");
    }

    @Test
    @WithMockUser(username = "TRAINER")
    public void addReservationUnknownUserName() throws Exception {
        UserEntity unknownUser = new UserEntity();
        unknownUser.setName("the unknown user");
        unknownUser.setRole(UserRole.ADMIN);
        unknownUser.setStatus(ActivationStatus.ACTIVE);
        addReservationError(createReservation(1, unknownUser, 1, 8, 2), HttpStatus.UNAUTHORIZED,
                "Der an der Reservierung angegebene Benutzer ist nicht der angemeldete Benutzer.");
    }

    @Test
    @WithMockUser(username = "REGISTERED")
    public void addReservationWrongRole() throws Exception {
        UserEntity unknownUser = new UserEntity();
        unknownUser.setName(user.getName());
        unknownUser.setRole(UserRole.ADMIN);
        unknownUser.setStatus(ActivationStatus.ACTIVE);
        addReservationError(createReservation(1, unknownUser, 1, 8, 2), HttpStatus.UNAUTHORIZED,
                "Der an der Reservierung angegebene Benutzer ist nicht der angemeldete Benutzer.");
    }

    @Test
    @WithMockUser(username = "REGISTERED")
    public void addReservationWrongStatus() throws Exception {
        UserEntity unknownUser = new UserEntity();
        unknownUser.setName(user.getName());
        unknownUser.setRole(UserRole.REGISTERED);
        unknownUser.setStatus(ActivationStatus.CREATED);
        addReservationError(createReservation(1, unknownUser, 1, 8, 2), HttpStatus.UNAUTHORIZED,
                "Der an der Reservierung angegebene Benutzer ist nicht der angemeldete Benutzer.");
    }

    @Test
    @WithMockUser(username = "REGISTERED.CREATED")
    public void addReservationUserInvalidStatusCreated() throws Exception {
        addReservationError(createReservation(1, insertUser(UserRole.REGISTERED, ActivationStatus.CREATED), 1, 8, 2),
                HttpStatus.UNAUTHORIZED, "Der Benutzer REGISTERED.CREATED ist nicht aktiviert.");
    }

    @Test
    @WithMockUser(username = "REGISTERED.LOCKED")
    public void addReservationUserInvalidStatusLocked() throws Exception {
        addReservationError(createReservation(1, insertUser(UserRole.REGISTERED, ActivationStatus.LOCKED), 1, 8, 2),
                HttpStatus.UNAUTHORIZED, "Der Benutzer REGISTERED.LOCKED ist nicht aktiviert.");
    }

    @Test
    @WithMockUser(username = "REGISTERED.REMOVED")
    public void addReservationUserInvalidStatusRemoved() throws Exception {
        addReservationError(createReservation(1, insertUser(UserRole.REGISTERED, ActivationStatus.REMOVED), 1, 8, 2),
                HttpStatus.UNAUTHORIZED, "Der Benutzer REGISTERED.REMOVED ist nicht aktiviert.");
    }

    @Test
    @WithMockUser(username = "REGISTERED.VERIFIED_BY_USER")
    public void addReservationUserInvalidStatusVerified() throws Exception {
        addReservationError(
                createReservation(1, insertUser(UserRole.REGISTERED, ActivationStatus.VERIFIED_BY_USER), 1, 8, 2),
                HttpStatus.UNAUTHORIZED, "Der Benutzer REGISTERED.VERIFIED_BY_USER ist nicht aktiviert.");
    }

    @Test
    @WithMockUser(username = "ANONYMOUS.CREATED")
    public void addReservationAnonymousUser() throws Exception {
        addReservationError(createReservation(1, insertUser(UserRole.ANONYMOUS, ActivationStatus.CREATED), 1, 8, 2),
                HttpStatus.UNAUTHORIZED, "Der Benutzer ANONYMOUS.CREATED ist nicht aktiviert.");
    }

    @Test
    @WithMockUser(username = "REGISTERED")
    public void addReservationUnauthorizedDateInThePast() throws Exception {
        Reservation reservation = createReservation(2, 1, 10, 1);
        reservation.setDate(LocalDate.now().minusDays(1));
        addReservationWithOccupationFieldError(reservation, "date",
                "Das Datum darf nicht in der Vergangenheit liegen.");
    }

    @Test
    @WithMockUser(username = "REGISTERED")
    public void addReservationUnauthorizedTimeInThePast() throws Exception {
        int hour = LocalTime.now().getHour();
        ReservationSystemConfig systemConfig = getSystemConfig(1);
        if (hour > systemConfig.openingHour() + 2 && hour < systemConfig.closingHour() - 1) {
            Reservation reservation = createReservation(2, 1, hour - 2, 1);
            reservation.setDate(LocalDate.now());
            addReservationWithOccupationFieldError(reservation, "date",
                    "Das Datum darf nicht in der Vergangenheit liegen.");
        }
    }

    @Test
    @WithMockUser(username = "REGISTERED")
    public void addReservationUnauthorizedInTheFarFuture() throws Exception {
        Reservation reservation = createReservation(1, 1, 8, 1);
        reservation.setDate(LocalDate.now().plusDays(65));
        addReservationWithOccupationFieldError(reservation, "date", "Das Datum liegt zu weit in der Zukunft.");
    }

    @Test
    @WithMockUser(username = "REGISTERED")
    public void addReservationUnauthorizedTypePrepaid() throws Exception {
        Reservation reservation = createReservation(1, 1, 8, 2);
        reservation.setType(3);

        addReservationWithOccupationFieldError(reservation, //
                "type", "Der Benutzer REGISTERED hat nicht die Rechte, eine Reservierung vom Typ Dauerbuchung anzulegen.");
    }

    @Test
    @WithMockUser(username = "REGISTERED")
    public void addReservationUnauthorizedTypeTournament() throws Exception {
        Reservation reservation = createReservation(1, 1, 8, 2, 2);
        addReservationWithOccupationFieldError(reservation, //
                "type",
                "Der Benutzer REGISTERED hat nicht die Rechte, eine Reservierung vom Typ Meisterschaft anzulegen.");
    }

    @Test
    @WithMockUser(username = "REGISTERED")
    public void addReservationUnauthorizedTypeTraining() throws Exception {
        Reservation reservation = createReservation(1, 1, 8, 2, 1);
        addReservationWithOccupationFieldError(reservation, //
                "type", "Der Benutzer REGISTERED hat nicht die Rechte, eine Reservierung vom Typ Training anzulegen.");
    }

    @Test
    @WithMockUser(username = "TRAINER")
    public void updateOccupation() throws Exception {
        Reservation reservation = getResponseJson(addReservation(createReservation(2, 1, 12, 2, 1)), Reservation.class);
        Occupation occupation = reservation.getOccupations().getFirst();
        occupation.setDuration(1);
        checkOccupation(updateOccupation(occupation), occupation, ActionType.MODIFY);
    }

    @Test
    @WithMockUser(username = "ADMIN")
    public void updateOccupations() throws Exception {
        Reservation reservation = checkReservationRepeatDaily(8, 3, 4, 2);
        reservation.setCourts("3");
        reservation.getOccupations().forEach(o -> o.setCourt(3));
        checkReservation(updateReservation(reservation), reservation, ActionType.MODIFY);
    }

    @Test
    @WithMockUser(username = "TRAINER")
    public void updateOccupationNotFound() throws Exception {
        Occupation occupation = new Occupation();
        occupation.setId(0);
        updateOccupation(occupation).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "TRAINER")
    public void updateReservationNull() throws Exception {
        updateOccupation(null).andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "TRAINER")
    public void deleteReservation() throws Exception {
        Reservation reservation = objectMapper.readValue(addReservation(createReservation(1, 1, 10, 2))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), Reservation.class);
        checkReservation(deleteReservation(reservation.getId()), reservation, ActionType.DELETE);

        assertThat(reservationRepository.findById(reservation.getId())).isEmpty();
        assertThat(occupationRepository.findByReservationId(reservation.getId())).isEmpty();
    }

    @Test
    @WithMockUser(username = "TRAINER")
    public void deleteReservationNotFound() throws Exception {
        deleteReservation(543636262L).andExpect(status().isNotFound());
    }

    @Test
    public void getAllNotAuthorized() throws Exception {
        performGet("/reservation/get").andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "ADMIN")
    public void getOccupations() throws Exception {

        Reservation reservation = createReservation(1, 1, 10, 2);
        reservation.setDate(LocalDate.now());
        reservation = getReservation(addReservation(reservation));
        checkOccupations(performGet("/rest/reservation/getOccupations/1/0").andExpect(status().isOk()),
                reservation.getOccupations());
    }

    @Test
    @WithMockUser(username = "TRAINER")
    public void getOccupationsWithDate() throws Exception {
        Reservation reservation = getReservation(addReservation(createReservation(2, 2, 12, 1)));
        long epochMilli = LocalDate.now().plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();
        checkOccupations(performGet("/rest/reservation/getOccupations/2/" + epochMilli).andExpect(status().isOk()),
                reservation.getOccupations());
    }

    @Test
    @WithMockUser(username = "TRAINER")
    public void getReservation() throws Exception {
        ReservationEntity reservation = reservationRepository
                .save(ReservationMapper.map(createReservation(1, user, 1, 10, 2)));

        performGet("/rest/reservation/" + reservation.getId()).andExpect(status().isOk());
    }

    private ResultActions addReservationNoCheck(Reservation reservation) throws Exception {
        return performPost("/rest/reservation", reservation != null ? reservation : "");
    }

    private ResultActions updateOccupation(Occupation occupation) throws Exception {
        return performPut("/rest/reservation/occupation", occupation != null ? occupation : "");
    }

    private ResultActions updateReservation(Reservation reservation) throws Exception {
        return performPut("/rest/reservation", reservation != null ? reservation : "");
    }

    private ResultActions deleteReservation(long id) throws Exception {
        return performDelete("/rest/reservation/" + id);
    }

    private ReservationSystemConfig getSystemConfig(long id) throws Exception {
        String content = performGet("/rest/reservation/systemconfig/" + id).andExpect(status().isOk()) //
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(content, ReservationSystemConfig.class);
    }

    private ResultActions addReservation(Reservation reservation) throws Exception {
        return checkReservation(addReservationNoCheck(reservation).andExpect(status().isCreated()), reservation,
                ActionType.CREATE);
    }

    private ResultActions addReservationError(Reservation reservation, HttpStatus status, String message)
            throws Exception {
        return checkError(addReservationNoCheck(reservation), status, message);
    }

    private void addReservationWithOccupationFieldError(Reservation reservation, String... fieldErrors)
            throws Exception {
        ResultActions resultActions = addReservationError(reservation, HttpStatus.BAD_REQUEST, null);
        int i = 0;
        while (i < fieldErrors.length) {
            assertOccupationFieldError(resultActions, i / 2, fieldErrors[i++], fieldErrors[i++]);
        }
    }

    private void addReservationWithFieldError(Reservation reservation, String... fieldErrors)
            throws Exception {
        ResultActions resultActions = addReservationError(reservation, HttpStatus.BAD_REQUEST, null);
        int i = 0;
        while (i < fieldErrors.length) {
            assertFieldError(resultActions, i / 2, "reservation", fieldErrors[i++], fieldErrors[i++]);
        }
    }

    private void assertOccupationFieldError(ResultActions resultActions, int i, String field, String message)
            throws Exception {
        assertFieldError(resultActions, i, "occupation[0]", field, message);
    }

    private void assertFieldError(ResultActions resultActions, int i, String entity, String field,
                                  String message) throws Exception {
        resultActions //.andExpect(jsonPath("$[" + i + "].entity").value(entity))
                .andExpect(jsonPath("$[" + i + "].field").value(field))
                .andExpect(jsonPath("$[" + i + "].message").value(message));
    }

    private void addReservationOverlap(Reservation reservation, int nrOccupation) throws Exception {
        addReservationWithOccupationFieldError(reservation, null,
                String.format("Reservierung am %tF %tR nicht möglich, weil Platz %s belegt ist.", reservation.getDate(),
                        reservation.getStart(), reservation.getCourts()));
    }

    private ResultActions checkReservation(ResultActions resultActions, Reservation reservation, ActionType actionType)
            throws Exception {
        switch (actionType) {
            case CREATE -> {
                resultActions.andExpect(status().isCreated()) //
                        .andExpect(jsonPath("$.id").isNotEmpty())
                        .andExpect(jsonPath("$.type").value(reservation.getType()));
                Reservation createdReservation = getResponseJson(resultActions, Reservation.class);
                resultActions.andExpect(jsonPath("$.id").value(createdReservation.getId()));
                checkProtocol(ReservationMapper.map(createdReservation), actionType);
            }
            case MODIFY -> {
                resultActions.andExpect(status().isOk());
                checkProtocol(ReservationMapper.map(reservation), actionType);
            }
            case DELETE -> {
                resultActions.andExpect(status().isOk());
                checkProtocol(ReservationMapper.map(reservation), actionType);
            }
        }
        return resultActions;
    }

    private void checkOccupation(ResultActions resultActions, Occupation occupation, ActionType actionType)
            throws Exception {
        switch (actionType) {
            case CREATE:
                resultActions.andExpect(status().isCreated()) //
                        .andExpect(jsonPath("$.id").isNotEmpty())
                        .andExpect(jsonPath("$.type").value(occupation.getType()));
                break;
            case MODIFY:
                resultActions.andExpect(status().isOk());
                checkProtocol(OccupationMapper.map(occupation), actionType);
            case DELETE:
                resultActions.andExpect(status().isOk());
                checkProtocol(OccupationMapper.map(occupation), actionType);
            default:
                break;
        }
    }

    private void checkOccupations(ResultActions resultActions, List<Occupation> occupations) throws Exception {
        if (occupations == null || occupations.isEmpty()) {
            resultActions.andExpect(jsonPath("$").isEmpty());
        } else {
            resultActions.andExpect(jsonPath("$", Matchers.hasSize(occupations.size())));
        }
    }

    private ResultActions checkError(ResultActions resultActions, HttpStatus status, String message) throws Exception {
        if (message != null) {
            resultActions.andExpect(jsonPath("$[0].message").value(message));
        }
        return resultActions.andExpect(status().is(status.value()));
    }

    private Reservation getReservation(ResultActions resultAction) throws Exception {
        String content = resultAction.andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(content, Reservation.class);
    }

    private UserEntity createUser(UserRole role, ActivationStatus status) {
        String name = role.toString() + "." + status.toString();
        return new UserEntity(name + "@myDomain.de", name, "top secret", role, status);
    }

    private UserEntity insertUser(UserRole role, ActivationStatus status) {
        return userRepository.save(createUser(role, status));
    }

    private Reservation createReservation(long systemId, int court, int hour, int duration) {
        return createReservation(systemId, null, court, hour, duration);
    }

    private Reservation createReservation(long systemId, UserEntity user, int court, int hour, int duration) {
        return new Reservation(systemId, UserMapper.map(user), "reservation name", String.valueOf(court),
                LocalDate.now().plusDays(1), LocalTime.of(hour, 0), duration, 0);
    }

    private Reservation createReservation(long systemId, int court, int hour, int duration, int type) {
        return new Reservation(systemId, null, "reservation name", String.valueOf(court),
                LocalDate.now().plusDays(1), LocalTime.of(hour, 0), duration, type);

    }

    private ReservationTypeEntity createReservationType(int type, String name, UserRole... roles) {
        var systemConfigReservationType = new ReservationTypeEntity();
        systemConfigReservationType.setId(Double.valueOf(Math.random() * 100_000).longValue());
        systemConfigReservationType.setType(type);
        systemConfigReservationType.setName(name);
        systemConfigReservationType.setRoles(roles(roles));
        return systemConfigReservationType;
    }

    private ReservationTypeEntity createReservationType(int type, String name, int maxDuration, int maxReservation, int maxCancel, UserRole... roles) {

        var systemConfigReservationType = createReservationType(type, name, roles);
        systemConfigReservationType.setMaxDuration(maxDuration);
        systemConfigReservationType.setMaxDaysReservationInFuture(maxReservation);
        systemConfigReservationType.setMaxCancelInHours(maxCancel);
        return systemConfigReservationType;
    }

    private String roles(UserRole... roles) {
        return Stream.concat(Stream.of(UserRole.ADMIN), Arrays.stream(roles))
                .map(Enum::toString).collect(Collectors.joining(","));
    }
}
