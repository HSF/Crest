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
import java.sql.Blob;
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
    protected Blob getBlob(ResultSet rs, String key) throws SQLException {
        return new SerialBlob(rs.getBytes(key));
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
     * @throws CdbServiceException
     *             If an Exception occurred
     * @return Payload
     */
    @Override
    protected Payload saveBlobAsBytes(PayloadDto entity) throws CdbServiceException {

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
    @Override
    protected void saveBlobAsStream(PayloadDto entity, InputStream is) throws CdbServiceException {
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
    protected void execute(InputStream is, String sql, PayloadDto entity) {

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
