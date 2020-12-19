/**
 * This file is part of PhysCondDB.
 * <p>
 * PhysCondDB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * PhysCondDB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with PhysCondDB.  If not, see <http://www.gnu.org/licenses/>.
 **/
package hep.crest.data.repositories;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author formica
 */
public class TagMetaDBImpl extends TagMetaGeneral implements TagMetaDataBaseCustom {

    /**
     * Default ctor.
     *
     * @param ds the DataSource
     */
    public TagMetaDBImpl(DataSource ds) {
        super(ds);
    }

    /*
     * (non-Javadoc)
     *
     * @see hep.crest.data.repositories.TagMetaGeneral#getBlob(java.sql.ResultSet,
     * java.lang.String)
     */
    @Override
    protected String getBlob(ResultSet rs, String key) throws SQLException {
        return new String(rs.getBytes(key));
    }
}
