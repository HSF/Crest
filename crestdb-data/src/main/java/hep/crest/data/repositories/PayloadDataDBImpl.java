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
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import hep.crest.data.exceptions.PayloadEncodingException;
import hep.crest.data.pojo.Payload;
import hep.crest.data.repositories.externals.PayloadRequests;

/**
 * An implementation for requests using Oracle and other database.
 *
 * @author formica
 *
 */
public class PayloadDataDBImpl extends AbstractPayloadDataGeneral implements PayloadDataBaseCustom {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(PayloadDataDBImpl.class);

    /**
     * @param ds
     *            the DataSource
     */
    public PayloadDataDBImpl(DataSource ds) {
        super(ds);
    }

 
    /* (non-Javadoc)
     * @see hep.crest.data.repositories.PayloadDataGeneral#getBlob(java.sql.ResultSet, java.lang.String)
     */
    @Override
    protected byte[] getBlob(ResultSet rs, String key) throws SQLException {
        return rs.getBytes(key);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * hep.crest.data.repositories.PayloadDataBaseCustom#findData(java.lang.String)
     */
    @Override
    @Transactional
    public InputStream findData(String id) {
        log.info("Find payload data {} using JDBCTEMPLATE", id);
        try {
            final JdbcTemplate jdbcTemplate = new JdbcTemplate(super.getDs());
            final String tablename = this.tablename();

            final String sql = PayloadRequests.getFindDataQuery(tablename);
            return jdbcTemplate.queryForObject(sql, new Object[] {id}, (rs, num) ->
                rs.getBlob("DATA").getBinaryStream()
            );
        }
        catch (final DataAccessException e) {
            log.error("Cannot find payload with data for hash {}: {}", id, e);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see hep.phycdb.svc.repositories.PayloadDataBaseCustom#saveNull()
     */
    @Override
    public Payload saveNull() throws IOException, PayloadEncodingException {
        log.warn("Method not implemented");
        return null;
    }

}
