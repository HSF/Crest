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
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * @author formica
 *
 */
@Component
public class TimestampDeserializer extends JsonDeserializer<Timestamp> {

	private String pattern = "ISO_OFFSET_DATE_TIME";

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private DateTimeFormatter locFormatter = null;

	public TimestampDeserializer() {
		/* default ctor */
	}

	/* (non-Javadoc)
	 * @see com.fasterxml.jackson.databind.JsonDeserializer#deserialize(com.fasterxml.jackson.core.JsonParser, com.fasterxml.jackson.databind.DeserializationContext)
	 */
	@Override
	public Timestamp deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException {
		try {
			String tstampstr = jp.getText();
			return this.format(tstampstr);
		} catch (Exception ex) {
			log.error("Failed to deserialize using format {}", getLocformatter());
			throw new JsonParseException(jp, ex.getMessage(), jp.getCurrentLocation());
		}
	}

	/**
	 * @param tstamp
	 * @return
	 */
	protected Timestamp format(String tstamp) {
		log.debug("Use private version of deserializer....{}", getLocformatter());
		ZonedDateTime zdt = ZonedDateTime.parse(tstamp, getLocformatter());
		return new Timestamp(zdt.toInstant().toEpochMilli());
	}

	/**
	 * @return
	 */
	protected DateTimeFormatter getLocformatter() {
		if (this.locFormatter != null)
			return locFormatter;
		if (pattern.equals("ISO_OFFSET_DATE_TIME")) {
			locFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
		} else if (pattern.equals("ISO_LOCAL_DATE_TIME")) {
			locFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
		} else {
			locFormatter = DateTimeFormatter.ofPattern(pattern);
		}
		return locFormatter;
	}

}
