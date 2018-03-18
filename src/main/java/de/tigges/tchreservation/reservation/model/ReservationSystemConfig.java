package de.tigges.tchreservation.reservation.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.sql.rowset.serial.SerialClob;

@Entity
public class ReservationSystemConfig {
	
	public ReservationSystemConfig() {
	}
	
	public ReservationSystemConfig (String name, int courts, int durationUnitInMinutes, int openingHour, int closingHour) {
		setName(name);
		setCourts(courts);
		setDurationUnitInMinutes(durationUnitInMinutes);
		setOpeningHour(openingHour);
		setClosingHour(closingHour);
	}
	
	@Id
	@GeneratedValue
	private long id;
	
	private String name;
    private int courts;
    private int durationUnitInMinutes;
    private int openingHour;
    private int closingHour;

    public int getCourts() {
        return courts;
    }
    
    public void setCourts(int courts) {
    	this.courts = courts;
    }

    public int getDurationUnitInMinutes() {
        return durationUnitInMinutes;
    }
    
    public void setDurationUnitInMinutes(int durationUnitInMinutes) {
    	this.durationUnitInMinutes = durationUnitInMinutes;
    }

    public int getOpeningHour() {
        return openingHour;
    }
    
    public void setOpeningHour(int openingHour) {
    	this.openingHour = openingHour;
    }

    public int getClosingHour() {
        return closingHour;
    }
    
    public void setClosingHour(int closingHour) {
    	this.closingHour = closingHour;
    }

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
