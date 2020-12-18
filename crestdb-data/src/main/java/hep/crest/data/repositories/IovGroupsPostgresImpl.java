/**
 * This file is part of Crest.
 * <p>
 * PhysCondDB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Crest is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Crest.  If not, see <http://www.gnu.org/licenses/>.
 **/
package hep.crest.data.repositories;

import hep.crest.data.handlers.PostgresBlobHandler;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * An implementation for groups queries.
 *
 * @author formica
 */
public class IovGroupsPostgresImpl extends IovGroupsImpl implements IovGroupsCustom {

    /**
     * Create Blob handler for postgres.
     */
    private PostgresBlobHandler bhandler = new PostgresBlobHandler();

    /**
     * @param ds the DataSource
     */
    public IovGroupsPostgresImpl(DataSource ds) {
        super(ds);
    }

    @Override
    protected String getBlob(ResultSet rs, String key) throws SQLException {
        byte[] buf = null;
        Long oid = rs.getLong(key);
        try (Connection conn = super.getDs().getConnection();) {
            conn.setAutoCommit(false);
            buf = bhandler.getlargeObj(oid, conn);
        }
        return this.getStringFromBuf(buf);
    }
}
