package hep.crest.server.converters;

import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Custom mapper for mapstruct.
 */
@Component
public class CustomMapper {

    /**
     * Convert a date to an OffsetDateTime.
     * @param date
     * @return OffsetDateTime
     */
    @Named("toOffsetDateTime")
    public OffsetDateTime toOffsetDateTime(Date date) {
        if (date == null) {
            return null;
        }
        Instant instant = date.toInstant();
        return instant.atOffset(ZoneOffset.UTC);
    }

    /**
     * Convert an OffsetDateTime to a Date.
     * @param offsetDateTime
     * @return Date
     */
    @Named("toDate")
    public Date toDate(OffsetDateTime offsetDateTime) {
        if (offsetDateTime == null) {
            return null;
        }
        Instant instant = offsetDateTime.toInstant();
        return Date.from(instant);
    }

    /**
     * Convert a Timestamp to an OffsetDateTime.
     * @param timestamp
     * @return OffsetDateTime
     */
    @Named("timestampToOffsetDateTime")
    public OffsetDateTime asOffsetDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        Instant instant = timestamp.toInstant();
        return instant.atOffset(ZoneOffset.UTC);
    }

    /**
     * Convert an OffsetDateTime to a Timestamp.
     * @param offsetDateTime
     * @return Timestamp
     */
    @Named("offsetDateTimeToTimestamp")
    public Timestamp asTimestamp(OffsetDateTime offsetDateTime) {
        if (offsetDateTime == null) {
            return null;
        }
        Instant instant = offsetDateTime.toInstant();
        return Timestamp.from(instant);
    }

    /**
     * Convert a Date to a Long.
     * @param date
     * @return Long
     */
    @Named("toMilli")
    public Long toMilli(Date date) {
        return date != null ? date.getTime() : null;
    }

    /**
     * Convert a char to a String.
     * @param c
     * @return String
     */
    @Named("charToString")
    public String charToString(char c) {
        return String.valueOf(c);
    }

    /**
     * Convert a String to a char.
     * @param s
     * @return char
     */
    @Named("stringToChar")
    public char stringToChar(String s) {
        return s != null && !s.isEmpty() ? s.charAt(0) : '\0';
    }

    /**
     * Convert a BigInteger to a BigDecimal.
     * @param value
     * @return BigDecimal
     */
    @Named("bigIntToBigDecimal")
    public BigDecimal bigIntToBigDecimal(BigInteger value) {
        return value != null ? new BigDecimal(value) : null;
    }

    /**
     * Convert a BigDecimal to a BigInteger.
     * @param value
     * @return BigInteger
     */
    @Named("bigDecimalToBigInt")
    public BigInteger bigDecimalToBigInt(BigDecimal value) {
        return value != null ? value.toBigInteger() : null;
    }

    /**
     * Convert a byte array to a String.
     * @param byteArray
     * @return String
     */
    @Named("byteArrayToString")
    public String byteArrayToString(byte[] byteArray) {
        return byteArray != null ? new String(byteArray, StandardCharsets.UTF_8) : null;
    }

    /**
     * Convert a String to a byte array.
     * @param string
     * @return byte[]
     */
    @Named("stringToByteArray")
    public byte[] stringToByteArray(String string) {
        return string != null ? string.getBytes(StandardCharsets.UTF_8) : null;
    }

}
