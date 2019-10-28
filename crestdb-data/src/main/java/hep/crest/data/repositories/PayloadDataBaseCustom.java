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
package hep.crest.data.repositories;

import java.io.IOException;
import java.io.InputStream;

import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.exceptions.PayloadEncodingException;
import hep.crest.data.pojo.Payload;
import hep.crest.swagger.model.PayloadDto;

/**
 * @author formica
 *
 */
public interface PayloadDataBaseCustom {
    /**
     * @param id
     *            the String
     * @return Payload
     */
    Payload find(String id);

    /**
     * @param id
     *            the String
     * @return Payload
     */
    Payload findData(String id);

    /**
     * The method does not access blob data.
     *
     * @param id
     *            the String
     * @return The payload or null.
     */
    Payload findMetaInfo(String id);

    /**
     * @param entity
     *            the PayloadDto
     * @return Either the entity which has been saved or null.
     * @throws CdbServiceException
     *             It should in reality not throw any exception
     */
    Payload save(PayloadDto entity) throws CdbServiceException;

    /**
     * @param entity
     *            the PayloadDto
     * @param is
     *            the InputStream
     * @return Either the entity which has been saved or null.
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    Payload save(PayloadDto entity, InputStream is) throws CdbServiceException;

    /**
     * @throws IOException
     *             If an Exception occurred
     * @throws PayloadEncodingException
     *             If an Exception occurred
     * @return Payload
     */
    Payload saveNull() throws IOException, PayloadEncodingException;

    /**
     * @param id
     *            the String
     * @return
     */
    void delete(String id);
}
