/**
 * 
 * This file is part of PhysCondDB.
 *
 *   PhysCondDB is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   PhysCondDB is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with PhysCondDB.  If not, see <http://www.gnu.org/licenses/>.
 **/
package hep.crest.data.serializers;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * @author formica
 *
 */
//@//Component
public class DateSerializer extends JsonSerializer<Date> {

    /**
     * The pattern: default to ISO_OFFSET_DATE_TIME.
     */
    private String pattern = "ISO_OFFSET_DATE_TIME";

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(DateSerializer.class);

    /**
     * Date Formatter.
     */
    private DateTimeFormatter locFormatter = null;

    /**
     * @param pattern
     *            the pattern to set
     */
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.fasterxml.jackson.databind.JsonSerializer#serialize(java.lang.Object,
     * com.fasterxml.jackson.core.JsonGenerator,
     * com.fasterxml.jackson.databind.SerializerProvider)
     */
    @Override
    public void serialize(Date ts, JsonGenerator jg, SerializerProvider sp) throws IOException {
        log.debug("Use private version of serializer....{}", getLocformatter());
        jg.writeString(this.format(ts));
    }

    /**
     * @param ts
     *            the Date
     * @return String
     */
    protected String format(Date ts) {
        final Instant fromEpochMilli = Instant.ofEpochMilli(ts.getTime());
        final ZonedDateTime zdt = fromEpochMilli.atZone(ZoneId.of("Z"));
        return zdt.format(getLocformatter());
    }

    /**
     * @return DateTimeFormatter
     */
    protected DateTimeFormatter getLocformatter() {
        if (this.locFormatter != null) {
            return locFormatter;
        }
        if ("ISO_OFFSET_DATE_TIME".equals(pattern)) {
            locFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        }
        else if ("ISO_LOCAL_DATE_TIME".equals(pattern)) {
            locFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        }
        else {
            locFormatter = DateTimeFormatter.ofPattern(pattern);
        }
        return locFormatter;
    }

}
