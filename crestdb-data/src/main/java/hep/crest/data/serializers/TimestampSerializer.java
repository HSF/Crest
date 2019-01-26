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
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * @author formica
 *
 */
@Component
public class TimestampSerializer extends JsonSerializer<Timestamp> {

	private String pattern="ISO_OFFSET_DATE_TIME";
	
	private Logger log = LoggerFactory.getLogger(this.getClass()); 

	private DateTimeFormatter locFormatter = null;
	
	/* (non-Javadoc)
	 * @see com.fasterxml.jackson.databind.JsonSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
	 */
	@Override
	public void serialize(Timestamp ts, JsonGenerator jg,
			SerializerProvider sp) throws IOException {
		try {
			log.debug("Use private version of serializer....{}",getLocformatter());
			jg.writeString(this.format(ts));
		} catch (Exception ex) {
			log.error("Failed to serialize using format {}",getLocformatter());
		}
	}

	/**
	 * @param ts
	 * @return
	 */
	protected String format(Timestamp ts) {
		Instant fromEpochMilli = Instant.ofEpochMilli(ts.getTime());
		ZonedDateTime zdt = fromEpochMilli.atZone(ZoneId.of("Z"));
		return zdt.format(getLocformatter());
	}
	
	protected DateTimeFormatter getLocformatter() {
		if(this.locFormatter != null)
			return locFormatter;
		if(pattern.equals("ISO_OFFSET_DATE_TIME")){
			locFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
		} else if(pattern.equals("ISO_LOCAL_DATE_TIME")) {
			locFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
		}else{
			locFormatter = DateTimeFormatter.ofPattern(pattern);
		}
		return locFormatter;
	}

}
