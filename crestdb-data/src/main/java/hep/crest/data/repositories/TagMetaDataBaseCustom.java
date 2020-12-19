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

import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.swagger.model.TagMetaDto;

/**
 * @author formica
 *
 */
public interface TagMetaDataBaseCustom {

    /**
     * @param id
     *            the String
     * @return TagMetaDto
     */
    TagMetaDto find(String id);

    /**
     * The method does not access blob data.
     *
     * @param id
     *            the String
     * @return The tag metadata or null.
     */
    TagMetaDto findMetaInfo(String id);

    /**
     * @param entity
     *            the TagMetaDto
     * @return Either the entity which has been saved or null.
     * @throws CdbServiceException
     *             It should in reality not throw any exception
     */
    TagMetaDto save(TagMetaDto entity);

    /**
     * @param entity
     *            the TagMetaDto
     * @return Either the entity which has been updated or null.
     * @throws CdbServiceException
     *             It should in reality not throw any exception
     */
    TagMetaDto update(TagMetaDto entity);

    /**
     * @param id
     *            the String
     * @return
     */
    void delete(String id);
}
