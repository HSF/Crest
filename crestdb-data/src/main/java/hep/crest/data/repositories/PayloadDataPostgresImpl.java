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

import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.handlers.PostgresBlobHandler;
import hep.crest.data.repositories.externals.SqlRequests;
import hep.crest.swagger.model.PayloadDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

/**
 * An implementation for requests using Postgres database.
 *
 * @author formica
 */
public class PayloadDataPostgresImpl extends AbstractPayloadDataGeneral implements PayloadDataBaseCustom {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(PayloadDataPostgresImpl.class);

    /**
     * Create Blob handler for postgres.
     */
    private PostgresBlobHandler bhandler = new PostgresBlobHandler();

    /**
     * @param ds the DataSource
     */
    public PayloadDataPostgresImpl(DataSource ds) {
        super(ds);
    }

    @Override
    protected InputStream getBlobAsStream(ResultSet rs, String key) {
        try {
            // Use the local getBlob
            byte[] buf = this.getBlob(rs, key);
            return new ByteArrayInputStream(buf);
        }
        catch (SQLException e) {
            log.error("Cannot get stream from byte array: {}", e.getMessage());
        }
        return null;
    }

    @Override
    protected byte[] getBlob(ResultSet rs, String key) throws SQLException {
        byte[] buf = null;
        Long oid = rs.getLong(key);
        log.info("Retrieve blob from oid {}", oid);
        try (Connection conn = super.getDs().getConnection();) {
            conn.setAutoCommit(false);
            buf = bhandler.getlargeObj(oid, conn);
        }
        return buf;
    }

    @Override
    protected PayloadDto saveBlobAsBytes(PayloadDto entity) {

        final String tablename = this.tablename();

        final String sql = SqlRequests.getInsertAllQuery(tablename);

        log.info("Insert Payload {} using JDBCTEMPLATE ", entity.getHash());

        final InputStream is = new ByteArrayInputStream(entity.getData());
        final InputStream sis = new ByteArrayInputStream(entity.getStreamerInfo());

        execute(is, sis, sql, entity);
        log.debug("Search for stored payload as a verification, use hash {}", entity.getHash());
        return findMetaInfo(entity.getHash());
    }

    @Override
    protected PayloadDto saveBlobAsStream(PayloadDto entity, InputStream is) {
        final String tablename = this.tablename();

        final String sql = SqlRequests.getInsertAllQuery(tablename);

        log.info("Insert Payload {} using JDBCTEMPLATE", entity.getHash());
        log.debug("Streamer info {} ", entity.getStreamerInfo());
        final InputStream sis = new ByteArrayInputStream(entity.getStreamerInfo());

        execute(is, sis, sql, entity);
        return findMetaInfo(entity.getHash());
    }

    /**
     * @param is     the InputStream
     * @param sis    the InputStream
     * @param sql    the String
     * @param entity the PayloadDto
     * @return
     * @throws CdbServiceException If an Exception occurred
     */
    protected void execute(InputStream is, InputStream sis, String sql, PayloadDto entity) {
        final Calendar calendar = Calendar.getInstance();
        final java.sql.Date inserttime = new java.sql.Date(calendar.getTime().getTime());
        entity.setInsertionTime(calendar.getTime());

        try (Connection conn = super.getDs().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);) {
            conn.setAutoCommit(false);
            final long oid = bhandler.getLargeObjectId(conn, is, entity);
            final long sioid = bhandler.getLargeObjectId(conn, sis, null);

            ps.setString(1, entity.getHash());
            ps.setString(2, entity.getObjectType());
            ps.setString(3, entity.getVersion());
            ps.setLong(4, oid);
            ps.setLong(5, sioid);
            ps.setDate(6, inserttime);
            ps.setInt(7, entity.getSize());
            log.info("Dump preparedstatement {} ", ps);
            ps.executeUpdate();
            conn.commit();
        }
        catch (final SQLException e) {
            log.error("Sql exception when storing payload with sql {} : {}", sql, e.getMessage());
        }
        finally {
            try {
                is.close();
                sis.close();
            }
            catch (final IOException e) {
                log.error("Error in closing streams...potential leak: {}", e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public void delete(String id) {
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(super.getDs());
        final String tablename = this.tablename();
        final String sqlget = SqlRequests.getFindDataQuery(tablename);
        final String sql = SqlRequests.getDeleteQuery(tablename);
        log.info("Remove payload with hash {} using JDBC", id);
        Long oid = jdbcTemplate.queryForObject(sqlget,
                new Object[]{id},
                (rs, row) -> rs.getLong(1));
        jdbcTemplate.execute("select lo_unlink(" + oid + ")");
        jdbcTemplate.update(sql, id);
        log.debug("Entity removal done...");
    }

}
