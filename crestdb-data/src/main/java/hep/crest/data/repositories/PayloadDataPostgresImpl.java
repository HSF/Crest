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
import hep.crest.data.exceptions.PayloadEncodingException;
import hep.crest.data.pojo.Payload;
import hep.crest.data.repositories.externals.PayloadRequests;
import hep.crest.swagger.model.PayloadDto;
import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 *
 */
public class PayloadDataPostgresImpl extends AbstractPayloadDataGeneral implements PayloadDataBaseCustom {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(PayloadDataPostgresImpl.class);

    /**
     * The null long.
     */
    private static final Long LONGNULL = null;

    /**
     * @param ds
     *            the DataSource
     */
    public PayloadDataPostgresImpl(DataSource ds) {
        super(ds);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * hep.crest.data.repositories.PayloadDataGeneral#getBlob(java.sql.ResultSet,
     * java.lang.String)
     */
    @Override
    protected byte[] getBlob(ResultSet rs, String key) throws SQLException {
        log.debug("Get a blob as bytes for {}", key);
        return rs.getBytes(key);
    }

    /**
     *
     * @param rs
     * @param key
     * @return byte[]
     * @throws SQLException
     */
    protected byte[] getBlobFromStream(ResultSet rs, String key) throws SQLException {
        byte[] buf = null;
        Long oid = rs.getLong(key);
        log.info("Retrieve blob from oid {}", oid);
        try (Connection conn = super.getDs().getConnection();) {
            buf = getlargeObj(oid, conn);
        }
        return buf;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * hep.crest.data.repositories.PayloadDataBaseCustom#findData(java.lang.String)
     */
    @Override
    @Transactional
    public PayloadDto findMetaInfo(String id) {
        log.info("Find payload meta info {} using Postgres connection", id);
        final String tablename = this.tablename();
        final String sql = PayloadRequests.getFindMetaQuery(tablename);
        Long oid = null;
        ResultSet rs = null;
        byte[] buf = null;
        try (Connection conn = super.getDs().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);) {
            conn.setAutoCommit(false);
            ps.setString(1, id);
            rs = ps.executeQuery();
            final PayloadDto entity = new PayloadDto();
            while (rs.next()) {
                // Open the large object for reading
                log.info("Read resultset...");
                oid = rs.getLong("STREAMER_INFO");
                entity.setHash(rs.getString("HASH"));
                entity.setObjectType(rs.getString("OBJECT_TYPE"));
                entity.setVersion(rs.getString("VERSION"));
                entity.setInsertionTime(rs.getDate("INSERTION_TIME"));
                entity.setSize(rs.getInt("DATA_SIZE"));
                log.info("Create Dto {}", entity);
            }
            // Only one row is returned....
            buf = getlargeObj(oid, conn);
            entity.setStreamerInfo(buf);
            rs.close();
            conn.commit();
            return entity;
        }
        catch (final SQLException e) {
            log.error("SQL exception occurred in retrieving payload data for {}: {}", id, e.getMessage());
        }
        finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            }
            catch (final SQLException e) {
                log.error("Error in closing result set : {}", e);
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * hep.crest.data.repositories.PayloadDataBaseCustom#findData(java.lang.String)
     */
    @Override
    public InputStream findData(String id) {
        log.info("Find payload data only for {} using JDBCTEMPLATE", id);
        final String tablename = this.tablename();
        final String sql = PayloadRequests.getFindDataQuery(tablename);
        return readBlobAsStream(id, sql);
    }

    /**
     * @param id
     *            the String
     * @param sql
     *            the String with the sql query
     * @return LargeObject
     */
    protected InputStream readBlobAsStream(String id, String sql) {
        log.info("Read Payload data with hash {} using JDBCTEMPLATE", id);
        byte[] buf = null;
        Long oid = null;
        ResultSet rs = null;
        try (Connection conn = super.getDs().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            conn.setAutoCommit(false);
            ps.setString(1, id);
            rs = ps.executeQuery();
            while (rs.next()) {
                // Open the large object for reading
                oid = rs.getLong(1);
            }
            // Only one row is returned....
            buf = getlargeObj(oid, conn);
            rs.close();
            conn.commit();
            return new ByteArrayInputStream(buf);
        }
        catch (final SQLException e) {
            log.error("SQL exception occurred in retrieving payload data for {}: {}", id, e.getMessage());
        }
        finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            }
            catch (final SQLException e) {
                log.error("Error in closing result set : {}", e);
            }
        }
        return null;
    }

    /**
     * @param oid
     *            the Long
     * @param conn
     *            the Connection
     * @throws SQLException
     *             If an Exception occurred
     * @return byte[]
     */
    protected byte[] getlargeObj(long oid, Connection conn) throws SQLException {
        final LargeObjectManager lobj = conn.unwrap(org.postgresql.PGConnection.class)
                .getLargeObjectAPI();
        LargeObject obj = null;
        byte[] buf = null;
        try {
            obj = lobj.open(oid, LargeObjectManager.READ);
            buf = new byte[obj.size()];
            obj.read(buf, 0, obj.size());
            obj.close();
        }
        catch (final SQLException e) {
            log.error("cannot read large object in postgres {} : {}", oid, e);
        }
        finally {
            if (obj != null) {
                obj.close();
                // conn.commit(); no need to call commit here, it will be done in the calling func.
            }
            //This may not work ? lobj.unlink(oid);
            // Be Careful : unlink could be used to DELETE the BLOB.
        }
        return buf;
    }

    /**
     * This method is inspired to the postgres documentation on the JDBC driver. For
     * reasons which are still not clear the select methods are working as they are.
     *
     * @param conn
     *            the Connection
     * @param is
     *            the InputStream
     * @param entity
     *            the PayloadDto
     * @return long
     */
    protected long getLargeObjectId(Connection conn, InputStream is, PayloadDto entity) {
        // Open the large object for writing
        LargeObjectManager lobj = null;
        LargeObject obj = null;
        long oid;
        try {
            lobj = conn.unwrap(org.postgresql.PGConnection.class).getLargeObjectAPI();
            oid = lobj.createLO();
            obj = lobj.open(oid, LargeObjectManager.WRITE);

            // Copy the data from the file to the large object
            final byte[] buf = new byte[2048];
            int s = 0;
            int tl = 0;
            while ((s = is.read(buf, 0, 2048)) > 0) {
                log.trace("Write into LargeObject ID {} nbytes {} ", oid, s);
                obj.write(buf, 0, s);
                tl += s;
            }
            if (entity != null) {
                log.trace("Written size {} ", tl);
                entity.setSize(tl);
            }
            // Close the large object
            obj.close();
            // This seems to be not needed or harmful: lobj . unlink( oid )
            // unlink seems to be used to DELETE the BLOB.
            return oid;
        }
        catch (SQLException | IOException e) {
            log.error("Exception in getting large object id: {}", e);
        }
        finally {
            try {
                if (obj != null) {
                    obj.close();
                }
                if (lobj != null) {
                    lobj = null;
                }
            }
            catch (final SQLException e) {
                log.error("Error in closing result set : {}", e);
            }
        }
        return LONGNULL;
    }

    @Override
    protected PayloadDto saveBlobAsBytes(PayloadDto entity) throws CdbServiceException {

        final String tablename = this.tablename();

        final String sql = PayloadRequests.getInsertAllQuery(tablename);

        log.info("Insert Payload {} using JDBCTEMPLATE ", entity.getHash());

        final InputStream is = new ByteArrayInputStream(entity.getData());
        final InputStream sis = new ByteArrayInputStream(entity.getStreamerInfo());

        execute(is, sis, sql, entity);
        log.debug("Search for stored payload as a verification, use hash {}", entity.getHash());
        return findMetaInfo(entity.getHash());
    }

    @Override
    protected PayloadDto saveBlobAsStream(PayloadDto entity, InputStream is)
            throws CdbServiceException {
        final String tablename = this.tablename();

        final String sql = PayloadRequests.getInsertAllQuery(tablename);

        log.info("Insert Payload {} using JDBCTEMPLATE", entity.getHash());
        log.debug("Streamer info {} ", entity.getStreamerInfo());
        final InputStream sis = new ByteArrayInputStream(entity.getStreamerInfo());

        execute(is, sis, sql, entity);
        return findMetaInfo(entity.getHash());
    }

    /**
     * @param is
     *            the InputStream
     * @param sis
     *            the InputStream
     * @param sql
     *            the String
     * @param entity
     *            the PayloadDto
     * @throws CdbServiceException
     *             If an Exception occurred
     * @return
     */
    protected void execute(InputStream is, InputStream sis, String sql, PayloadDto entity)
            throws CdbServiceException {
        final Calendar calendar = Calendar.getInstance();
        final java.sql.Date inserttime = new java.sql.Date(calendar.getTime().getTime());
        entity.setInsertionTime(calendar.getTime());

        try (Connection conn = super.getDs().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            conn.setAutoCommit(false);
            final long oid = getLargeObjectId(conn, is, entity);
            final long sioid = getLargeObjectId(conn, sis, null);

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
                log.error("Error in closing streams...potential leak: {}", e);
            }
        }
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

}
