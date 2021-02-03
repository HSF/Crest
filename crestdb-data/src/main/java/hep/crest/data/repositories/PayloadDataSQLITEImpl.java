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

import hep.crest.data.handlers.PayloadHandler;

import javax.sql.DataSource;
import javax.sql.rowset.serial.SerialBlob;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * An implementation for requests using SQLite database.
 *
 * @author formica
 */
public class PayloadDataSQLITEImpl extends AbstractPayloadDataGeneral implements PayloadDataBaseCustom {

    /**
     * @param ds the DataSource
     */
    public PayloadDataSQLITEImpl(DataSource ds) {
        super(ds);
    }

    @Override
    protected byte[] getBlob(ResultSet rs, String key) throws SQLException {
        SerialBlob blob = new SerialBlob(rs.getBytes(key));
        return PayloadHandler.getBytesFromInputStream(blob.getBinaryStream());
    }

    @Override
    protected InputStream getBlobAsStream(ResultSet rs, String key) throws SQLException {
        SerialBlob blob = new SerialBlob(rs.getBytes(key));
        return blob.getBinaryStream();
    }
}
