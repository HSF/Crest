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
     * Correspond to string like: 2011-12-03T10:15:30+01:00 .
     * @see java.time.format.DateTimeFormatter
     * The pattern: default to ISO_OFFSET_DATE_TIME.
     */
    private static String datePATTERN = "ISO_OFFSET_DATE_TIME";

    /**
     * The Date formatter.
     */
    private DateTimeFormatter locFormatter = null;

    /**
     * Set other pattern options.
     * @param datePATTERN the String.
     */
    public static void setDatePATTERN(String datePATTERN) {
        DateFormatterHandler.datePATTERN = datePATTERN;
    }
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
     * Create the formatter if null, or send back the existing formatter.
     * @return DateTimeFormatter
     */
    public DateTimeFormatter getLocformatter() {
        if (this.locFormatter == null) {
            if ("ISO_OFFSET_DATE_TIME".equals(datePATTERN)) {
                locFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
            }
            else if ("ISO_LOCAL_DATE_TIME".equals(datePATTERN)) {
                locFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            }
            else {
                locFormatter = DateTimeFormatter.ofPattern(datePATTERN);
            }
        }
        return locFormatter;
    }
}
