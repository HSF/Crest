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
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * An implementation for requests using Oracle and other database.
 *
 * @author formica
 */
public class PayloadDataDBImpl extends AbstractPayloadDataGeneral implements PayloadDataBaseCustom {

    /**
     * @param ds the DataSource
     */
    public PayloadDataDBImpl(DataSource ds) {
        super(ds);
    }

    /**
     * @param rs
     * @param key
     * @return byte[]
     * @throws SQLException
     */
    @Override
    protected byte[] getBlob(ResultSet rs, String key) throws SQLException {
        return rs.getBytes(key);
    }


    /**
     * Transform the byte array from the Blob into a binary stream.
     *
     * @param rs
     * @param key
     * @return InputStream
     * @throws SQLException
     */
    @Override
    protected InputStream getBlobAsStream(ResultSet rs, String key) throws SQLException {
        return rs.getBlob(key).getBinaryStream();
    }

}
