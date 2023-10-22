package de.tigges.tchreservation;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
/**
 * utility class to show a unix timestamp
 * @author johannes
 *
 */
public class DateUtil {

	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("usage: DateUtil <unix timestamp>\n show a unix timestamp in human readable format");
			System.exit(1);
		}
		var epoch = Long.parseLong(args[0]);

		var localDate = Instant.ofEpochMilli(epoch).atZone(ZoneId.systemDefault()).toLocalDate();
		var localTime = Instant.ofEpochMilli(epoch).atZone(ZoneId.systemDefault()).toLocalTime();

		var localDateLong = localDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000;
		var localTimeLong = localTime.atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toEpochSecond() * 1000;

		System.out.printf("convert %d  to date %s (%d) and to time %s (%d)\n", epoch, localDate, localDateLong,
				localTime, localTimeLong);
		var d = new Date();
		d.setTime(Long.parseLong(args[0]));
		System.out.printf("convert %d to %s\n", epoch, d);
	}
}
