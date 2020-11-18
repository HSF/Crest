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
import hep.crest.data.repositories.externals.TagMetaRequests;
import hep.crest.swagger.model.TagMetaDto;
import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

/**
 * @author formica
 *
 */
public class TagMetaPostgresImpl extends TagMetaGeneral implements TagMetaDataBaseCustom {

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
    public TagMetaPostgresImpl(DataSource ds) {
        super(ds);
    }

    
    /* (non-Javadoc)
     * @see hep.crest.data.repositories.TagMetaGeneral#getBlob(java.sql.ResultSet, java.lang.String)
     */
    @Override
    protected String getBlob(ResultSet rs, String key) throws SQLException {
        return new String(rs.getBytes(key));
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
    protected long getLargeObjectId(Connection conn, InputStream is, TagMetaDto entity) {
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
            while ((s = is.read(buf, 0, 2048)) > 0) {
                obj.write(buf, 0, s);
            }
            // Close the large object
            obj.close();
            // lobj.unlink(oid);
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

    /**
     * @param tis
     *            the InputStream
     * @param sql
     *            the String
     * @param entity
     *            the TagMetaDto
     * @throws CdbServiceException
     *             If an Exception occurred
     * @return
     */
    protected void execute(InputStream tis, String sql, TagMetaDto entity)
            throws CdbServiceException {
        final Calendar calendar = Calendar.getInstance();
        final java.sql.Date inserttime = new java.sql.Date(calendar.getTime().getTime());
        entity.setInsertionTime(calendar.getTime());

        try (Connection conn = super.getDs().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            conn.setAutoCommit(false);
            final long tid = getLargeObjectId(conn, tis, entity);

            ps.setString(1, entity.getTagName());
            ps.setString(2, entity.getDescription());
            ps.setInt(3, entity.getChansize());
            ps.setInt(4, entity.getColsize());
            ps.setLong(5, tid);
            ps.setDate(6, inserttime);
            log.debug("Dump preparedstatement {} ", ps);
            ps.executeUpdate();
            conn.commit();
        }
        catch (final SQLException e) {
            log.error("Sql exception when storing payload with sql {} : {}", sql, e.getMessage());
        }
        finally {
            try {
                tis.close();
            }
            catch (final IOException e) {
                log.error("Error in closing streams...potential leak");
            }
        }
    }

    /**
     * @param tis
     *            the InputStream
     * @param sql
     *            the String
     * @param entity
     *            the TagMetaDto
     * @throws CdbServiceException
     *             If an Exception occurred
     * @return
     */
    protected void update(InputStream tis, String sql, TagMetaDto entity) {

        final Calendar calendar = Calendar.getInstance();
        final java.sql.Date inserttime = new java.sql.Date(calendar.getTime().getTime());
        entity.setInsertionTime(calendar.getTime());

        try (Connection conn = super.getDs().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            conn.setAutoCommit(false);
            final long tid = getLargeObjectId(conn, tis, entity);
            ps.setString(1, entity.getDescription());
            ps.setInt(2, entity.getChansize());
            ps.setInt(3, entity.getColsize());
            ps.setLong(4, tid);
            ps.setDate(5, inserttime);
            // Now we set the update where condition.
            ps.setString(6, entity.getTagName());
            log.debug("Dump preparedstatement {}", ps);
            ps.execute();
            log.debug("Search for stored tag meta as a verification, use tag name {} ",
                    entity.getTagName());
        }
        catch (final SQLException e) {
            log.error("Sql exception when storing payload with sql {} : {}", sql, e.getMessage());
        }
        finally {
            try {
                tis.close();
            }
            catch (final IOException e) {
                log.error("Error in closing streams...potential leak");
            }
        }
    }

    @Override
    protected TagMetaDto saveBlobAsBytes(TagMetaDto entity) throws CdbServiceException {

        final String tablename = this.tablename();

        final String sql = TagMetaRequests.getInsertAllQuery(tablename);

        log.debug("Insert Tag meta {} using JDBCTEMPLATE ", entity.getTagName());

        final InputStream is = new ByteArrayInputStream(entity.getTagInfo().getBytes(StandardCharsets.UTF_8));

        execute(is, sql, entity);
        log.debug("Search for stored tag meta as a verification, use tag {}", entity.getTagName());
        return findMetaInfo(entity.getTagName());
    }


    @Override
    protected TagMetaDto updateAsBytes(TagMetaDto entity) throws CdbServiceException {
        final String tablename = this.tablename();
        final String sql = TagMetaRequests.getUpdateQuery(tablename);

        log.debug("Update Tag meta {} using JDBCTEMPLATE ", entity.getTagName());
        final InputStream is = new ByteArrayInputStream(entity.getTagInfo().getBytes(StandardCharsets.UTF_8));

        update(is, sql, entity);
        log.debug("Search for stored tag meta as a verification, use tag {}", entity.getTagName());
        return findMetaInfo(entity.getTagName());
    }
}
