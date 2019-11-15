/**
 * 
 */
package hep.crest.data.handlers;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author formica
 *
 */
public class DateFormatterHandler {

    /**
     * The pattern: default to ISO_OFFSET_DATE_TIME.
     */
    private static String datePATTERN = "ISO_OFFSET_DATE_TIME";

    /**
     * The Date formatter.
     */
    private DateTimeFormatter locFormatter = null;

    /**
     * @param tstamp
     *            the String
     * @return Timestamp
     */
    public Timestamp format(String tstamp) {
        final ZonedDateTime zdt = ZonedDateTime.parse(tstamp, getLocformatter());
        return new Timestamp(zdt.toInstant().toEpochMilli());
    }

    /**
     * @param ts
     *            the Timestamp
     * @return String
     */
    public String format(Timestamp ts) {
        final Instant fromEpochMilli = Instant.ofEpochMilli(ts.getTime());
        final ZonedDateTime zdt = fromEpochMilli.atZone(ZoneId.of("Z"));
        return zdt.format(getLocformatter());
    }

    /**
     * @return DateTimeFormatter
     */
    public DateTimeFormatter getLocformatter() {
        if (this.locFormatter != null) {
            return locFormatter;
        }
        if (datePATTERN.equals("ISO_OFFSET_DATE_TIME")) {
            locFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        }
        else if (datePATTERN.equals("ISO_LOCAL_DATE_TIME")) {
            locFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        }
        else {
            locFormatter = DateTimeFormatter.ofPattern(datePATTERN);
        }
        return locFormatter;
    }
}
