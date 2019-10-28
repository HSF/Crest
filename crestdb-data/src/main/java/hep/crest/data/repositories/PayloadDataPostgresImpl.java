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

import javax.persistence.Table;
import javax.sql.DataSource;

import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import hep.crest.data.config.DatabasePropertyConfigurator;
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
public class PayloadDataPostgresImpl implements PayloadDataBaseCustom {

    /**
     * Logger.
     */
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Datasource.
     */
    private final DataSource ds;

    /**
     * A null long.
     */
    private static Long long1 = null;

    /**
     * The upload directory for files.
     */
    @Value("${crest.upload.dir:/tmp}")
    private String serverUploadLocationFolder;

    /**
     * Default table name.
     */
    private String defaultTablename = null;

    /**
     * @param ds
     *            the DataSource
     */
    public PayloadDataPostgresImpl(DataSource ds) {
        super();
        this.ds = ds;
    }

    /**
     * @param defaultTablename
     *            the String
     * @return
     */
    public void setDefaultTablename(String defaultTablename) {
        if (this.defaultTablename == null) {
            this.defaultTablename = defaultTablename;
        }
    }

    /**
     * @return String
     */
    protected String tablename() {
        final Table ann = Payload.class.getAnnotation(Table.class);
        String tablename = ann.name();
        if (!DatabasePropertyConfigurator.SCHEMA_NAME.isEmpty()) {
            tablename = DatabasePropertyConfigurator.SCHEMA_NAME + "." + tablename;
        }
        else if (this.defaultTablename != null) {
            tablename = this.defaultTablename + "." + tablename;
        }
        return tablename;
    }

    /*
     * (non-Javadoc)
     *
     * @see hep.crest.data.repositories.PayloadDataBaseCustom#find(java.lang.String)
     */
    @Override
    public Payload find(String id) {
        log.info("Find payload {} using JDBCTEMPLATE", id);
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
        final String tablename = this.tablename();

        final String sql = PayloadRequests.getFindQuery(tablename);

        // Be careful, this seems not to work with Postgres: probably getBlob loads an
        // OID and not the byte[] .
        // Temporarily, try to create a postgresql implementation of this class.

        return jdbcTemplate.queryForObject(sql, new Object[] {id}, (rs, num) -> {
            final Payload entity = new Payload();
            entity.setHash(rs.getString("HASH"));
            entity.setObjectType(rs.getString("OBJECT_TYPE"));
            entity.setVersion(rs.getString("VERSION"));
            entity.setInsertionTime(rs.getDate("INSERTION_TIME"));
            entity.setData(rs.getBlob("DATA"));
            entity.setStreamerInfo(rs.getBlob("STREAMER_INFO"));
            entity.setSize(rs.getInt("DATA_SIZE"));
            return entity;
        });
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * hep.crest.data.repositories.PayloadDataBaseCustom#findMetaInfo(java.lang.
     * String)
     */
    @Override
    @Transactional
    public Payload findMetaInfo(String id) {
        log.info("Find payload meta data only for {} using JDBCTEMPLATE", id);
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
        final String tablename = this.tablename();

        final String sql = PayloadRequests.getFindMetaQuery(tablename);

        return jdbcTemplate.queryForObject(sql, new Object[] {id}, (rs, num) -> {
            final Payload entity = new Payload();
            entity.setHash(rs.getString("HASH"));
            entity.setObjectType(rs.getString("OBJECT_TYPE"));
            entity.setVersion(rs.getString("VERSION"));
            entity.setInsertionTime(rs.getDate("INSERTION_TIME"));
            entity.setStreamerInfo(rs.getBlob("STREAMER_INFO"));
            entity.setSize(rs.getInt("DATA_SIZE"));
            return entity;
        });

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * hep.crest.data.repositories.PayloadDataBaseCustom#findData(java.lang.String)
     */
    @Override
    public Payload findData(String id) {
        log.info("Find payload data only for {} using JDBCTEMPLATE", id);
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
        final String tablename = this.tablename();

        final String sql = PayloadRequests.getFindDataHashQuery(tablename);

        return jdbcTemplate.queryForObject(sql, new Object[] {id}, (rs, num) -> {
            final Payload entity = new Payload();
            entity.setHash(rs.getString("HASH"));
            entity.setData(rs.getBlob("DATA"));
            return entity;
        });
    }

    /**
     * @param id
     *            the String
     * @return LargeObject
     */
    protected LargeObject readBlobAsStream(String id) {
        final String tablename = this.tablename();

        final String sql = PayloadRequests.getFindDataQuery(tablename);

        log.info("Read Payload data with hash {} using JDBCTEMPLATE", id);
        ResultSet rs = null;
        LargeObject obj = null;
        try (Connection conn = ds.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            conn.setAutoCommit(false);
            final LargeObjectManager lobj = conn.unwrap(org.postgresql.PGConnection.class)
                    .getLargeObjectAPI();
            ps.setString(1, id);
            rs = ps.executeQuery();
            while (rs.next()) {
                // Open the large object for reading
                final long oid = rs.getLong(1);
                obj = lobj.open(oid, LargeObjectManager.READ);
            }
        }
        catch (final SQLException e) {
            log.error("SQL exception occurred in retrieving payload data for {}", id);
        }
        finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (obj != null) {
                    obj.close();
                }
            }
            catch (SQLException | NullPointerException e) {
                log.error("Error in closing result set : {}", e.getMessage());
            }
        }
        return obj;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * hep.crest.data.repositories.PayloadDataBaseCustom#save(hep.crest.swagger.
     * model.PayloadDto)
     */
    @Override
    public Payload save(PayloadDto entity) throws CdbServiceException {
        Payload savedentity = null;
        try {
            log.info("Saving blob as bytes array....");
            savedentity = this.saveBlobAsBytes(entity);
        }
        catch (final IOException e) {
            log.error("Exception in saving payload dto: {}", e.getMessage());
        }
        return savedentity;
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
        LargeObjectManager lobj;
        long oid;
        try {
            lobj = conn.unwrap(org.postgresql.PGConnection.class).getLargeObjectAPI();
            oid = lobj.createLO();
            final LargeObject obj = lobj.open(oid, LargeObjectManager.WRITE);

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
            return oid;
        }
        catch (SQLException | IOException e) {
            log.error("Exception in getting large object id: {}", e.getMessage());
        }
        return long1;
    }

    /**
     * @param entity
     *            the PayloadDto
     * @throws IOException
     *             If an Exception occurred
     * @return Payload
     */
    protected Payload saveBlobAsBytes(PayloadDto entity) throws IOException {

        final String tablename = this.tablename();

        final String sql = PayloadRequests.getInsertAllQuery(tablename);

        log.info("Insert Payload {} using JDBCTEMPLATE ", entity.getHash());
        final Calendar calendar = Calendar.getInstance();
        final java.sql.Date inserttime = new java.sql.Date(calendar.getTime().getTime());
        entity.setInsertionTime(calendar.getTime());

        final InputStream is = new ByteArrayInputStream(entity.getData());
        final InputStream sis = new ByteArrayInputStream(entity.getStreamerInfo());

        try (Connection conn = ds.getConnection();
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
            log.info("Dump preparedstatement {} using sql {} and args {} {} {} {}", ps, sql,
                    entity.getHash(), entity.getObjectType(), entity.getVersion(),
                    entity.getInsertionTime());
            ps.execute();
            conn.commit();
            log.debug("Search for stored payload as a verification, use hash {}", entity.getHash());
            return find(entity.getHash());
        }
        catch (final SQLException e) {
            log.error("Exception : {}", e.getMessage());
        }
        finally {
            is.close();
            sis.close();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * hep.crest.data.repositories.PayloadDataBaseCustom#save(hep.crest.swagger.
     * model.PayloadDto, java.io.InputStream)
     */
    @Override
    @Transactional
    public Payload save(PayloadDto entity, InputStream is) throws CdbServiceException {
        Payload savedentity = null;
        try {
            log.info("Saving blob as stream....");
            this.saveBlobAsStream(entity, is);
            savedentity = findMetaInfo(entity.getHash());
        }
        catch (final IOException e) {
            log.error("IOException during payload insertion: {}", e.getMessage());
        }
        catch (final Exception e) {
            log.error("Exception during payload insertion: {}", e.getMessage());
        }
        return savedentity;
    }

    /**
     * @param entity
     *            the PayloadDto
     * @param is
     *            the InputStream
     * @throws IOException
     *             If an Exception occurred
     */
    protected void saveBlobAsStream(PayloadDto entity, InputStream is) throws IOException {
        final String tablename = this.tablename();

        final String sql = PayloadRequests.getInsertAllQuery(tablename);

        log.info("Insert Payload {} using JDBCTEMPLATE", entity.getHash());

        log.debug("Streamer info {} ", entity.getStreamerInfo());

        final Calendar calendar = Calendar.getInstance();
        final java.sql.Date inserttime = new java.sql.Date(calendar.getTime().getTime());
        entity.setInsertionTime(calendar.getTime());

        final InputStream sis = new ByteArrayInputStream(entity.getStreamerInfo());
        try (Connection conn = ds.getConnection();
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
            log.debug("Dump preparedstatement {} ", ps);
            ps.executeUpdate();
            conn.commit();
        }
        catch (final SQLException e) {
            log.error("Exception from SQL during insertion: {}", e.getMessage());
        }
        finally {
            is.close();
            sis.close();
        }
    }

    /**
     * @param metainfoentity
     *            the PayloadDto
     * @return Payload
     */
    protected Payload saveMetaInfo(PayloadDto metainfoentity) {

        final String tablename = this.tablename();

        final String sql = PayloadRequests.getInsertMetaQuery(tablename);

        log.info("Insert Payload Meta Info {} using JDBCTEMPLATE", metainfoentity.getHash());
        try (Connection conn = ds.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, metainfoentity.getHash());
            ps.setString(2, metainfoentity.getObjectType());
            ps.setString(3, metainfoentity.getVersion());
            ps.setBytes(4, metainfoentity.getStreamerInfo());
            // FIXME: be careful to the insertion time...is the one provided correct ?
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

    /*
     * (non-Javadoc)
     *
     * @see
     * hep.crest.data.repositories.PayloadDataBaseCustom#delete(java.lang.String)
     */
    @Override
    @Transactional
    public void delete(String id) {
        final String tablename = this.tablename();
        final String sql = PayloadRequests.getDeleteQuery(tablename);
        log.info("Remove payload with hash {} using JDBCTEMPLATE", id);
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
        jdbcTemplate.update(sql, id);
        log.debug("Entity removal done...");
    }
}
