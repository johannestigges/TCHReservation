package de.tigges.tchreservation.reservation;

import de.tigges.tchreservation.exception.AuthorizationException;
import de.tigges.tchreservation.exception.BadRequestException;
import de.tigges.tchreservation.exception.ErrorCode;
import de.tigges.tchreservation.reservation.model.RepeatType;
import de.tigges.tchreservation.reservation.model.Reservation;
import de.tigges.tchreservation.reservation.model.ReservationSystemConfig;
import de.tigges.tchreservation.user.jpa.UserEntity;
import de.tigges.tchreservation.user.model.User;
import de.tigges.tchreservation.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationValidator {

    private final OccupationValidator occupationValidator;
    private final Validator validator;

    public void validateReservation(
            Reservation reservation,
            UserEntity loggedInUser,
            ReservationSystemConfig systemConfig) {
        validator.clearErrorMessages();

        validateUser(reservation, loggedInUser);
        validator.checkNotEmpty("court", reservation.getCourts());
        validator.checkNotEmpty("date", reservation.getDate());
        validator.checkNotEmpty("start", reservation.getStart());

        validateCourts(reservation, systemConfig);
        validateRepeat(reservation);

        validator.checkErrorMessages();
    }

    public void validateOccupations(
            Reservation reservation,
            UserEntity loggedInUser,
            ReservationSystemConfig systemConfig) {

        validator.clearErrorMessages();

        reservation.getOccupations().forEach(occupation -> {
            try {
                occupationValidator.validateOccupation(occupation, loggedInUser, systemConfig);
            } catch (BadRequestException e) {
                validator.addErrorMessages(e.getErrorMessages());
            }
        });

        validator.checkErrorMessages();
    }

    private void validateUser(Reservation reservation, UserEntity loggedInUser) {
        if (!checkUser(reservation.getUser(), loggedInUser)) {
            throw new AuthorizationException(ErrorCode.WRONG_USER,
                    validator.msg("error_wrong_user"));
        }
    }

    private void validateRepeat(Reservation reservation) {
        if (RepeatType.daily.equals(reservation.getRepeatType())
                || RepeatType.weekly.equals(reservation.getRepeatType())) {
            if (reservation.getRepeatUntil() == null) {
                validator.addFieldErrorMessage("repeatUntil", ErrorCode.REPEATUNTIL_EMPTY, "error_repeatUntil_empty");
            } else if (reservation.getRepeatUntil().isBefore(reservation.getDate())) {
                validator.addFieldErrorMessage("repeatUntil", ErrorCode.REPEATUNTIL_BEFORE_START, "error_repeatUntil_before_start");
            }
        }
    }

    private void validateCourts(Reservation reservation, ReservationSystemConfig systemConfig) {
        for (int court : reservation.getCourtsAsArray()) {
            if (court < 1) {
                validator.addFieldErrorMessage("court", ErrorCode.COURT_TOO_SMALL,
                        "error_court_too_small", court);
            }
            if (court > systemConfig.courts().size()) {
                validator.addFieldErrorMessage("court", ErrorCode.COURT_TOO_BIG,
                        "error_court_too_big", court, systemConfig.courts().size());
            }
        }
    }

    private boolean checkUser(User user, UserEntity loggedInUser) {
        if (user == null) {
            return true;
        }
        return user.getName().equals(loggedInUser.getName())
                && user.getStatus().equals(loggedInUser.getStatus())
                && user.getRole().equals(loggedInUser.getRole());
    }
}
