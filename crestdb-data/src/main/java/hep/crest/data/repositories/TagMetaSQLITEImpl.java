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

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author formica
 *
 */
public class TagMetaSQLITEImpl extends TagMetaDBImpl implements TagMetaDataBaseCustom {

    /**
     * The logger.
     */
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Default ctor.
     *
     * @param ds
     *            the DataSource
     */
    public TagMetaSQLITEImpl(DataSource ds) {
        super(ds);
    }

}
