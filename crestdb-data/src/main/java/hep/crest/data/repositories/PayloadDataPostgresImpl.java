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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

import javax.sql.DataSource;

import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.exceptions.PayloadEncodingException;
import hep.crest.data.pojo.Payload;
import hep.crest.data.repositories.externals.PayloadRequests;
import hep.crest.swagger.model.PayloadDto;

/**
 * An implementation for requests using Postgres database.
 *
 * @author formica
 *
 */
public class PayloadDataPostgresImpl extends PayloadDataGeneral implements PayloadDataBaseCustom {

    /**
     * Logger.
     */
    private final Logger log = LoggerFactory.getLogger(this.getClass());

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
        return rs.getBytes(key);
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
        return readBlobAsStream(id);
    }

    /**
     * @param id
     *            the String
     * @return LargeObject
     */
    protected InputStream readBlobAsStream(String id) {
        final String tablename = this.tablename();

        final String sql = PayloadRequests.getFindDataQuery(tablename);

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
            rs.close();
            buf = getlargeObj(oid, conn);
            //conn.commit();
            return new ByteArrayInputStream(buf);
        }
        catch (final SQLException e) {
            log.error("SQL exception occurred in retrieving payload data for {}", id);
        }
        finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            }
            catch (SQLException | NullPointerException e) {
                log.error("Error in closing result set : {}", e.getMessage());
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
            log.error("cannot read large object in postgres {} ", oid);
        }
        finally {
            if (obj != null) {
                obj.close();
            }
            lobj.unlink(oid);

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
                obj.write(buf, 0, s);
                tl += s;
            }
            if (entity != null) {
                entity.setSize(tl);
            }
            // Close the large object
            obj.close();
            lobj.unlink(oid);
            return oid;
        }
        catch (SQLException | IOException e) {
            log.error("Exception in getting large object id: {}", e.getMessage());
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
            catch (SQLException | NullPointerException e) {
                log.error("Error in closing result set : {}", e.getMessage());
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
                log.error("Error in closing streams...potential leak");
            }
        }
    }

    /**
     * @param metainfoentity
     *            the PayloadDto
     * @return Payload
     */
    protected PayloadDto saveMetaInfo(PayloadDto metainfoentity) {

        final String tablename = this.tablename();

        final String sql = PayloadRequests.getInsertMetaQuery(tablename);

        log.info("Insert Payload Meta Info {} using JDBCTEMPLATE", metainfoentity.getHash());
        try (Connection conn = super.getDs().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, metainfoentity.getHash());
            ps.setString(2, metainfoentity.getObjectType());
            ps.setString(3, metainfoentity.getVersion());
            ps.setBytes(4, metainfoentity.getStreamerInfo());
            ps.setDate(5, new java.sql.Date(metainfoentity.getInsertionTime().getTime()));
            ps.setInt(6, metainfoentity.getSize());
            log.debug("Dump preparedstatement {}", ps);
            ps.execute();
            conn.commit();
            return find(metainfoentity.getHash());
        }
        catch (final SQLException e) {
            log.error("Sql Exception when saving meta info : {}", e.getMessage());
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
        return null;
    }

}
