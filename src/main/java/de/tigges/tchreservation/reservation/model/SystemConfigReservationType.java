package de.tigges.tchreservation.reservation.model;

import de.tigges.tchreservation.user.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigReservationType {
    private long id;
    private int type;
    private String name;
    private int maxDuration;
    private int maxDaysReservationInFuture;
    private int maxCancelInHours;
    private Collection<UserRole> roles;
}
