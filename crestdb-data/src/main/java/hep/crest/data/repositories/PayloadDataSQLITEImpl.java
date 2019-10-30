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
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.persistence.Table;
import javax.sql.DataSource;
import javax.sql.rowset.serial.SerialBlob;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import hep.crest.data.config.DatabasePropertyConfigurator;
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
public class PayloadDataSQLITEImpl implements PayloadDataBaseCustom {

    /**
     * Logger.
     */
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Datasource.
     */
    private final DataSource ds;

    /**
     * Handler for payload.
     */
    @Autowired
    private PayloadHandler payloadHandler;

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
    public PayloadDataSQLITEImpl(DataSource ds) {
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
     * @see
     * hep.crest.data.repositories.PayloadDataBaseCustom#setPayloadHandler(hep.crest
     * .data.handlers.PayloadHandler)
     */
    @Override
    public void setPayloadHandler(PayloadHandler payloadHandler) {
        this.payloadHandler = payloadHandler;
    }

    /*
     * (non-Javadoc)
     *
     * @see hep.crest.data.repositories.PayloadDataBaseCustom#find(java.lang.String)
     */
    @Override
    @Transactional
    public Payload find(String id) {
        log.info("Find payload {} using JDBCTEMPLATE", id);
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
        final String tablename = this.tablename();

        final String sql = PayloadRequests.getFindQuery(tablename);

        return jdbcTemplate.queryForObject(sql, new Object[] {id}, (rs, num) -> {
            final Payload entity = new Payload();
            entity.setHash(rs.getString("HASH"));
            entity.setObjectType(rs.getString("OBJECT_TYPE"));
            entity.setVersion(rs.getString("VERSION"));
            entity.setInsertionTime(rs.getDate("INSERTION_TIME"));
            SerialBlob blob = new SerialBlob(rs.getBytes("DATA"));
            entity.setData(blob);
            blob = new SerialBlob(rs.getBytes("STREAMER_INFO"));
            entity.setStreamerInfo(blob);
            entity.setSize(rs.getInt("DATA_SIZE"));

            return entity;
        });

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * hep.phycdb.svc.repositories.PayloadDataBaseCustom#save(hep.phycdb.data.pojo.
     * Payload)
     */
    @Override
    public Payload save(PayloadDto entity) throws CdbServiceException {
        Payload savedentity = null;
        try {
            savedentity = this.saveBlobAsBytes(entity);
        }
        catch (final IOException e) {
            log.error("Error in save paylod dto : {}", e.getMessage());
        }
        return savedentity;
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
            this.saveBlobAsStream(entity, is);
            savedentity = findMetaInfo(entity.getHash());
        }
        catch (final IOException e) {
            log.error("Error in save paylod dto : {}", e.getMessage());
        }
        return savedentity;
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

        log.info("Insert Payload {} using JDBCTEMPLATE", entity.getHash());

        try (PreparedStatement ps = ds.getConnection().prepareStatement(sql);) {
            ps.setString(1, entity.getHash());
            ps.setString(2, entity.getObjectType());
            ps.setString(3, entity.getVersion());
            ps.setBytes(4, entity.getData());
            ps.setBytes(5, entity.getStreamerInfo());
            ps.setDate(6, new java.sql.Date(entity.getInsertionTime().getTime()));
            ps.setInt(7, entity.getSize());

            log.info("Dump preparedstatement {}", ps);
            ps.execute();
            return find(entity.getHash());
        }
        catch (final SQLException e) {
            log.error("Sql exception in saving bytes for payload : {} ", e.getMessage());
        }
        return null;
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
        final byte[] blob = payloadHandler.getBytesFromInputStream(is);
        if (blob != null) {
            entity.setSize(blob.length);
            log.debug("Read data blob of length {} and streamer info {}", blob.length,
                    entity.getStreamerInfo().length);
        }
        try (PreparedStatement ps = ds.getConnection().prepareStatement(sql);) {

            ps.setString(1, entity.getHash());
            ps.setString(2, entity.getObjectType());
            ps.setString(3, entity.getVersion());
            ps.setBytes(4, blob);
            ps.setBytes(5, entity.getStreamerInfo());
            ps.setDate(6, new java.sql.Date(entity.getInsertionTime().getTime()));
            ps.setInt(7, entity.getSize());
            log.debug("Dump preparedstatement {}", ps);
            ps.execute();
        }
        catch (final SQLException e) {
            log.error("Sql exception when storing payload {}", e.getMessage());
        }
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
        log.info("Entity removal done...");
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
        log.info("Find payload {} using JDBCTEMPLATE", id);
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
        final String tablename = this.tablename();

        final String sql = PayloadRequests.getFindMetaQuery(tablename);
        return jdbcTemplate.queryForObject(sql, new Object[] {id}, (rs, num) -> {
            final Payload entity = new Payload();
            entity.setHash(rs.getString("HASH"));
            entity.setObjectType(rs.getString("OBJECT_TYPE"));
            entity.setVersion(rs.getString("VERSION"));
            entity.setInsertionTime(rs.getDate("INSERTION_TIME"));
            final SerialBlob blob = new SerialBlob(rs.getBytes("STREAMER_INFO"));
            entity.setStreamerInfo(blob);
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

        final String sql = PayloadRequests.getFindDataQuery(tablename);
        return jdbcTemplate.queryForObject(sql, new Object[] {id}, (rs, num) -> {
            final Payload entity = new Payload();
            final SerialBlob blob = new SerialBlob(rs.getBytes("DATA"));
            entity.setData(blob);
            return entity;
        });
    }

}
