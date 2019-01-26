package hep.creat.data.test.tools;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

public class IovPropertyConfigurator {

//	public static final BigDecimal INFINITY = new BigDecimal("253402297199000");
	public static final BigDecimal INFINITY = new BigDecimal("253402297199000000000");
	
	public static void main(String[] args) {
		
		Instant now = Instant.ofEpochMilli((new Date()).getTime());
		
		TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
		Instant inf = Instant.ofEpochMilli(253402297199000L);
		System.out.println("Inf is "+inf);
		
		ZonedDateTime zdtcet = inf.atZone(ZoneId.of("Europe/Paris"));
		System.out.println("Print using timezone Europe/Paris: "+zdtcet.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
		
		ZonedDateTime zdtgmt = inf.atZone(ZoneId.of("Z"));
		System.out.println("Print using timezone Z: "+zdtgmt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

		ZonedDateTime zdtusa = inf.atZone(ZoneId.of("America/Los_Angeles"));
		System.out.println("Print using timezone America LA: "+zdtusa.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

		Instant infcms = Instant.ofEpochMilli(253402300799000L);
		System.out.println("Infcms is "+infcms);
		zdtcet = infcms.atZone(ZoneId.of("Europe/Paris"));
		System.out.println("Print using timezone Europe/Paris: "+zdtcet.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
		zdtgmt = infcms.atZone(ZoneId.of("Z"));
		System.out.println("Print using timezone Z: "+zdtgmt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
		zdtusa = infcms.atZone(ZoneId.of("America/Los_Angeles"));
		System.out.println("Print using timezone America LA: "+zdtusa.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

		System.out.println("Now is "+now+" corresponding to "+now.getEpochSecond());
		zdtcet = now.atZone(ZoneId.of("Europe/Paris"));
		System.out.println("Print using timezone Europe/Paris: "+zdtcet.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
		zdtgmt = now.atZone(ZoneId.of("Z"));
		System.out.println("Print using timezone Z: "+zdtgmt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
		zdtusa = now.atZone(ZoneId.of("America/Los_Angeles"));
		System.out.println("Print using timezone America LA: "+zdtusa.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

	}
	
}
