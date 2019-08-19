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
import hep.crest.data.pojo.TagMeta;
import hep.crest.swagger.model.TagMetaDto;


/**
 * @author formica
 *
 */
public interface TagMetaDataBaseCustom {
	TagMeta find(String id);
	/**
	 * The method does not access blob data.
	 * @param id
	 * @return The tag metadata or null.
	 */
	TagMeta findMetaInfo(String id);

	/**
	 * @param entity
	 * @return Either the entity which has been saved or null.
	 * @throws CdbServiceException
	 * 	It should in reality not throw any exception
	 */
	TagMeta save(TagMetaDto entity) throws CdbServiceException;
	
	/**
	 * @param entity
	 * @return Either the entity which has been updated or null.
	 * @throws CdbServiceException
	 * 	It should in reality not throw any exception
	 */
	TagMeta update(TagMetaDto entity) throws CdbServiceException;
	
	void delete(String id);
}
