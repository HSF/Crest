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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.handlers.PayloadHandler;
import hep.crest.data.repositories.externals.TagMetaRequests;
import hep.crest.swagger.model.TagMetaDto;

/**
 * @author formica
 *
 */
public class TagMetaDBImpl extends TagMetaGeneral implements TagMetaDataBaseCustom {

    /**
     * The logger.
     */
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Default ctor.
     *
     * @param ds
     *            the DataSource
     */
    public TagMetaDBImpl(DataSource ds) {
        super(ds);
    }

    /*
     * (non-Javadoc)
     *
     * @see hep.crest.data.repositories.TagMetaGeneral#getBlob(java.sql.ResultSet,
     * java.lang.String)
     */
    @Override
    protected String getBlob(ResultSet rs, String key) throws SQLException {
        return new String(rs.getBytes(key));
    }

    @Override
    protected TagMetaDto saveBlobAsBytes(TagMetaDto entity) throws CdbServiceException {

        final String tablename = this.tablename();
        final String sql = TagMetaRequests.getInsertAllQuery(tablename);

        log.info("Insert Tag meta {} using JDBCTEMPLATE ", entity.getTagName());
        execute(null, sql, entity);
        return findMetaInfo(entity.getTagName());
    }

    @Override
    protected TagMetaDto updateAsBytes(TagMetaDto entity) throws CdbServiceException {

        final String tablename = this.tablename();
        final String sql = TagMetaRequests.getUpdateQuery(tablename);

        log.info("Update Tag meta {} using JDBCTEMPLATE ", entity.getTagName());
        update(null, sql, entity);
        return findMetaInfo(entity.getTagName());
    }

    /**
     * @param is
     *            the InputStream
     * @param sql
     *            the String
     * @param entity
     *            the TagMetaDto
     * @throws CdbServiceException
     *             If an Exception occurred
     * @return
     */
    protected void execute(InputStream is, String sql, TagMetaDto entity) {

        final Calendar calendar = Calendar.getInstance();
        final java.sql.Date inserttime = new java.sql.Date(calendar.getTime().getTime());
        entity.setInsertionTime(calendar.getTime());

        if (is != null) {
            final byte[] blob = PayloadHandler.getBytesFromInputStream(is);
            if (blob != null) {
                entity.setTagInfo(new String(blob));
                log.debug("Read channel info blob of length {} ", blob.length);
            }
        }

        try (Connection conn = super.getDs().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, entity.getTagName());
            ps.setString(2, entity.getDescription());
            ps.setInt(3, entity.getChansize());
            ps.setInt(4, entity.getColsize());
            ps.setBytes(5, entity.getTagInfo().getBytes());
            ps.setDate(6, inserttime);
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
                if (is != null) {
                    is.close();
                }
            }
            catch (final IOException e) {
                log.error("Error in closing streams...potential leak");
            }
        }
    }
    
    /**
     * @param is
     *            the InputStream
     * @param sql
     *            the String
     * @param entity
     *            the TagMetaDto
     * @throws CdbServiceException
     *             If an Exception occurred
     * @return
     */
    protected void update(InputStream is, String sql, TagMetaDto entity) {

        final Calendar calendar = Calendar.getInstance();
        final java.sql.Date inserttime = new java.sql.Date(calendar.getTime().getTime());
        entity.setInsertionTime(calendar.getTime());

        if (is != null) {
            final byte[] blob = PayloadHandler.getBytesFromInputStream(is);
            if (blob != null) {
                entity.setTagInfo(new String(blob));
                log.debug("Read channel info blob of length {} ", blob.length);
            }
        }

        try (Connection conn = super.getDs().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, entity.getDescription());
            ps.setInt(2, entity.getChansize());
            ps.setInt(3, entity.getColsize());
            ps.setBytes(4, entity.getTagInfo().getBytes());
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
                if (is != null) {
                    is.close();
                }
            }
            catch (final IOException e) {
                log.error("Error in closing streams...potential leak");
            }
        }
    }
    
}
