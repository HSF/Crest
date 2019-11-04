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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

import javax.sql.DataSource;
import javax.sql.rowset.serial.SerialBlob;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.exceptions.PayloadEncodingException;
import hep.crest.data.handlers.PayloadHandler;
import hep.crest.data.pojo.Payload;
import hep.crest.data.repositories.externals.PayloadRequests;
import hep.crest.swagger.model.PayloadDto;

/**
 * An implementation for requests using SQLite database.
 *
 * @author formica
 *
 */
public class PayloadDataSQLITEImpl extends PayloadDataGeneral implements PayloadDataBaseCustom {

    /**
     * Logger.
     */
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * @param ds
     *            the DataSource
     */
    public PayloadDataSQLITEImpl(DataSource ds) {
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
     * @see hep.phycdb.svc.repositories.PayloadDataBaseCustom#saveNull()
     */
    @Override
    public Payload saveNull() throws IOException, PayloadEncodingException {
        return null;
    }

    @Override
    protected PayloadDto saveBlobAsBytes(PayloadDto entity) throws CdbServiceException {

        final String tablename = this.tablename();
        final String sql = PayloadRequests.getInsertAllQuery(tablename);

        log.info("Insert Payload with hash {} using saveBlobAsBytes", entity.getHash());
        execute(null, sql, entity);
        return findMetaInfo(entity.getHash());
    }

    @Override
    protected PayloadDto saveBlobAsStream(PayloadDto entity, InputStream is) throws CdbServiceException {
        final String tablename = this.tablename();

        final String sql = PayloadRequests.getInsertAllQuery(tablename);

        log.info("Insert Payload with hash {} using saveBlobAsStream", entity.getHash());
        execute(is, sql, entity);
        return findMetaInfo(entity.getHash());
    }

    /**
     * @param is
     *            the InputStream
     * @param sql
     *            the String
     * @param entity
     *            the PayloadDto
     * @throws IOException
     *             If an Exception occurred
     * @return
     */
    protected void execute(InputStream is, String sql, PayloadDto entity) {

        final Calendar calendar = Calendar.getInstance();
        final java.sql.Date inserttime = new java.sql.Date(calendar.getTime().getTime());
        entity.setInsertionTime(calendar.getTime());

        if (is != null) {
            final byte[] blob = PayloadHandler.getBytesFromInputStream(is);
            if (blob != null) {
                entity.setSize(blob.length);
                entity.setData(blob);
                log.debug("Read data blob of length {} and streamer info length {}", blob.length,
                        entity.getStreamerInfo().length);
            }
        }
        try (Connection conn = super.getDs().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            log.info("Getting connection {}", conn);

            ps.setString(1, entity.getHash());
            ps.setString(2, entity.getObjectType());
            ps.setString(3, entity.getVersion());
            ps.setBytes(4, entity.getData());
            ps.setBytes(5, entity.getStreamerInfo());
            ps.setDate(6, inserttime);
            ps.setInt(7, entity.getSize());
            log.info("Dump preparedstatement {}", ps);
            ps.execute();
            //conn.commit();
        }
        catch (final SQLException e) {
            log.error("Sql exception when storing payload with sql {} : {}", sql, e.getMessage());
        }
        finally {
            try {
                if (is != null) {
                    is.close();
                }
            }
            catch (final IOException e) {
                log.error("Error in closing streams...potential leak");
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * hep.crest.data.repositories.PayloadDataBaseCustom#findData(java.lang.String)
     */
    @Override
    public InputStream findData(String id) {
        log.info("Find payload data for hash {}", id);
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(super.getDs());
        final String tablename = this.tablename();

        final String sql = PayloadRequests.getFindDataQuery(tablename);
        return jdbcTemplate.queryForObject(sql, new Object[] {id}, (rs, num) -> {
            final SerialBlob blob = new SerialBlob(rs.getBytes("DATA"));
            return blob.getBinaryStream();
        });
    }

}
