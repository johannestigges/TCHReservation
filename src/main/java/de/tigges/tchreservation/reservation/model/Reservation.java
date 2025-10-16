package de.tigges.tchreservation.reservation.model;

import de.tigges.tchreservation.user.model.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class Reservation {

    private long id;

    private long systemConfigId;
    private String text;
    private LocalDate date;
    private LocalTime start;
    private int duration;
    private String courts;
    private int type;
    private RepeatType repeatType;
    private LocalDate repeatUntil;

    private User user;

    private List<Occupation> occupations;

    public Reservation(long systemConfigId, User user, String text, String courts, LocalDate date, LocalTime start,
                       int duration, int type) {
        setSystemConfigId(systemConfigId);
        setUser(user);
        setText(text);
        setCourts(courts);
        setDate(date);
        setStart(start);
        setDuration(duration);
        setType(type);
    }

    public List<Occupation> getOccupations() {
        if (occupations == null) {
            occupations = new ArrayList<>();
        }
        return occupations;
    }

    public void setCourtsFromInteger(int... courts) {
        this.courts = toCourts(courts);
    }
    
    public int[] getCourtsAsArray() {
        return toCourts(this.courts);
    }

    public int[] toCourts(String courtsString) {
        if (courtsString == null) {
            return new int[0];
        }
        var c = courtsString.split(" ");
        var courts = new int[c.length];
        for (int i = 0; i < courts.length; i++) {
            courts[i] = Integer.parseInt(c[i]);
        }
        return courts;
    }

    public String toCourts(int... courts) {
        return Arrays.stream(courts).mapToObj(String::valueOf).collect(Collectors.joining(" "));
    }
}
