package de.tigges.tchreservation.reservation.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import de.tigges.tchreservation.user.model.User;
import lombok.Data;
import lombok.NoArgsConstructor;

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
	private ReservationType type;
	private RepeatType repeatType;
	private LocalDate repeatUntil;

	private User user;

	private List<Occupation> occupations;

	public Reservation(long systemConfigId, User user, String text, String courts, LocalDate date, LocalTime start,
			int duration, ReservationType type) {
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

	/**
	 * set the courts
	 * 
	 * @param courts
	 */
	public void setCourtsFromInteger(int... courts) {
		this.courts = toCourts(courts);
	}

	/**
	 * add courts to the list of courts
	 * 
	 * @param courts
	 */
	public void addCourts(int... courts) {
		this.courts = this.courts + ' ' + toCourts(courts);
	}

	/**
	 * get the courts as int[]
	 * 
	 * @return courts as int[]
	 */
	public int[] getCourtsAsArray() {
		return toCourts(this.courts);
	}

	/**
	 * convert courts from String to int[]
	 * 
	 * @param courtsString
	 * @return int[]
	 */
	public int[] toCourts(String courtsString) {
		if (courtsString == null) {
			return new int[0];
		}
		String[] c = courtsString.split(" ");
		int[] courts = new int[c.length];
		for (int i = 0; i < courts.length; i++) {
			courts[i] = Integer.parseInt(c[i]);
		}
		return courts;
	}

	/**
	 * convert courts from int... to String
	 * 
	 * @param courts
	 * @return courtsString
	 */
	public String toCourts(int... courts) {
		return Arrays.stream(courts).mapToObj(String::valueOf).collect(Collectors.joining(" "));
	}
}
