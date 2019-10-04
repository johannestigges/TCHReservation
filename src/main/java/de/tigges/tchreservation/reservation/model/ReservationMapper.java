package de.tigges.tchreservation.reservation.model;

import de.tigges.tchreservation.reservation.jpa.ReservationEntity;
import de.tigges.tchreservation.user.UserMapper;

public class ReservationMapper {

	public static Reservation map(ReservationEntity e) {
		if (e == null) {
			return null;
		}
		Reservation r = new Reservation();
		r.setId(e.getId());
		r.setSystemConfigId(e.getSystemConfigId());
		r.setText(e.getText());
		r.setDate(e.getDate());
		r.setStart(e.getStart());
		r.setDuration(e.getDuration());
		r.setCourts(e.getCourts());
		r.setType(e.getType());
		r.setWeeklyRepeatUntil(e.getWeeklyRepeatUntil());
		r.setUser(UserMapper.map(e.getUser()));
		return r;
	}

	public static ReservationEntity map(Reservation r) {
		if (r == null) {
			return null;
		}
		ReservationEntity e = new ReservationEntity();
		e.setId(r.getId());
		e.setSystemConfigId(r.getSystemConfigId());
		e.setText(r.getText());
		e.setDate(r.getDate());
		e.setStart(r.getStart());
		e.setDuration(r.getDuration());
		e.setCourts(r.getCourts());
		e.setType(r.getType());
		e.setWeeklyRepeatUntil(r.getWeeklyRepeatUntil());
		e.setUser(UserMapper.map(r.getUser()));
		return e;
	}
}
