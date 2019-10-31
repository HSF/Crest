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
import java.util.Calendar;

import javax.sql.DataSource;
import javax.sql.rowset.serial.SerialBlob;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.exceptions.PayloadEncodingException;
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

    /*
     * (non-Javadoc)
     *
     * @see hep.crest.data.repositories.PayloadDataBaseCustom#find(java.lang.String)
     */
    @Override
    @Transactional
    public Payload find(String id) {
        log.info("Find payload {} using JDBCTEMPLATE", id);
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(super.getDs());
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
        execute(null, sql, entity);
        return find(entity.getHash());

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
        execute(is, sql, entity);
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
    protected void execute(InputStream is, String sql, PayloadDto entity) throws IOException {

        final Calendar calendar = Calendar.getInstance();
        final java.sql.Date inserttime = new java.sql.Date(calendar.getTime().getTime());
        entity.setInsertionTime(calendar.getTime());

        if (is != null) {
            final byte[] blob = super.getPayloadHandler().getBytesFromInputStream(is);
            if (blob != null) {
                entity.setSize(blob.length);
                entity.setData(blob);
                log.debug("Read data blob of length {} and streamer info {}", blob.length,
                        entity.getStreamerInfo().length);
            }
        }
        try (PreparedStatement ps = super.getDs().getConnection().prepareStatement(sql);) {

            ps.setString(1, entity.getHash());
            ps.setString(2, entity.getObjectType());
            ps.setString(3, entity.getVersion());
            ps.setBytes(4, entity.getData());
            ps.setBytes(5, entity.getStreamerInfo());
            ps.setDate(6, inserttime);
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
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(super.getDs());
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
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(super.getDs());
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
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(super.getDs());
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
