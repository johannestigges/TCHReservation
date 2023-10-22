package de.tigges.tchreservation.reservation.model;

import de.tigges.tchreservation.reservation.jpa.OccupationEntity;

public class OccupationMapper {

	public static OccupationEntity map(Occupation o) {
		if (o == null) {
			return null;
		}
		var e = new OccupationEntity();
		e.setId(o.getId());
		e.setText(o.getText());
		e.setDate(o.getDate());
		e.setStart(o.getStart());
		e.setDuration(o.getDuration());
		e.setCourt(o.getCourt());
		e.setLastCourt(o.getLastCourt());
		e.setType(o.getType());
		e.setSystemConfigId(o.getSystemConfigId());
		e.setReservation(ReservationMapper.map(o.getReservation()));
		return e;
	}

	public static Occupation map(OccupationEntity e) {
		if (e == null) {
			return null;
		}
		var o = new Occupation();
		o.setId(e.getId());
		o.setText(e.getText());
		o.setDate(e.getDate());
		o.setStart(e.getStart());
		o.setDuration(e.getDuration());
		o.setCourt(e.getCourt());
		o.setLastCourt(e.getLastCourt());
		o.setType(e.getType());
		o.setSystemConfigId(e.getSystemConfigId());
		o.setReservation(ReservationMapper.map(e.getReservation()));
		return o;
	}
}
