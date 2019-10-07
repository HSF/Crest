package hep.crest.server.test;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateFormatExample {

	private static String DATE_PATTERN = "yyyyMMdd'T'HHmmssX";

	public DateFormatExample() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String args[]) {
		System.out.println("Testing date pattern in java " + DateFormatExample.DATE_PATTERN);
//		DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy'T'HH:mm:ss:SSS z");
		DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DateFormatExample.DATE_PATTERN);

		// Date string with zone information
		//String dateString = "08/03/2019T16:20:17:717 UTC+05:30";
		//String dateString = "20190308T162017 UTC+02:00";
		String dateString = "20190923T005000Z";

		// Instance with given zone
		ZonedDateTime zdtInstanceAtOffset = ZonedDateTime.parse(dateString, DATE_TIME_FORMATTER);

		// Instance in UTC
		ZonedDateTime zdtInstanceAtUTC = zdtInstanceAtOffset.withZoneSameInstant(ZoneOffset.UTC);
		ZonedDateTime zdtInstanceAtParis = zdtInstanceAtOffset.withZoneSameInstant(ZoneOffset.of("+0200"));

		// Formatting to string
		String dateStringInUTC = zdtInstanceAtUTC.format(DATE_TIME_FORMATTER);

		System.out.println(zdtInstanceAtOffset);
		System.out.println(zdtInstanceAtUTC);
		System.out.println(zdtInstanceAtParis);
		System.out.println(dateStringInUTC);

		// Convert ZonedDateTime to instant which is in UTC
		System.out.println("orig "+zdtInstanceAtOffset.toInstant());
		System.out.println("paris "+zdtInstanceAtParis.toInstant());
	
		System.out.println(zdtInstanceAtUTC.toInstant().toEpochMilli());
		System.out.println(zdtInstanceAtOffset.toInstant().toEpochMilli());
		
		Timestamp now = new Timestamp(Instant.now().toEpochMilli());
		LocalDateTime ldt = now.toLocalDateTime();
        final ZoneId zoneId = ZoneId.of("Europe/Paris");
		System.out.println(now.toInstant().toEpochMilli());
		System.out.println(ldt.atZone(zoneId).withZoneSameInstant(ZoneId.of("UTC")));
		System.out.println(ldt);
		
	}
}
