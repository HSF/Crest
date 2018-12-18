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
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * @author formica
 *
 */
@Component
public class ByteArrayDeserializer extends JsonDeserializer<byte[]> {

	private Logger log = LoggerFactory.getLogger(this.getClass()); 

	public ByteArrayDeserializer(){
    }
	

	@Override
	public byte[]  deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		try {
			log.debug("Trying to deserialize json parser {}",jp);
			String blobstr = new String(jp.getTextCharacters());
			byte[] mblob = Base64.getDecoder().decode(blobstr);
			return mblob;
		} catch (Exception ex) {
			log.error("Failed to deserialize byte array {}",jp.getText());
			throw new JsonParseException(ex.getMessage(), jp.getCurrentLocation());
		}
	}
}
