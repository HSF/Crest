package hep.crest.data.repositories;

import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.handlers.PayloadHandler;
import hep.crest.data.pojo.TagMeta;
import hep.crest.data.repositories.externals.TagMetaRequests;
import hep.crest.swagger.model.TagMetaDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Table;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

/**
 * General base class for repository implementations.
 *
 * @author formica
 */
public abstract class TagMetaGeneral extends DataGeneral implements TagMetaDataBaseCustom {

    /**
     * Logger.
     */
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * @param ds the DataSource
     */
    protected TagMetaGeneral(DataSource ds) {
        super(ds);
        ann = TagMeta.class.getAnnotation(Table.class);
    }

    @Override
    public TagMetaDto save(TagMetaDto entity) {
        TagMetaDto savedentity = null;
        try {
            savedentity = this.saveBlobAsBytes(entity);
        }
        catch (final Exception e) {
            log.error("Error in save paylod dto : {}", e.getMessage());
        }
        return savedentity;
    }

    @Override
    public TagMetaDto update(TagMetaDto entity) {
        TagMetaDto savedentity = null;
        try {
            savedentity = this.updateAsBytes(entity);
        }
        catch (final Exception e) {
            log.error("Error in save paylod dto : {}", e.getMessage());
        }
        return savedentity;
    }

    @Override
    @Transactional
    public void delete(String id) {
        final String tablename = this.tablename();
        final String sql = TagMetaRequests.getDeleteQuery(tablename);
        log.debug("Remove tag meta with tag name {} using JDBCTEMPLATE", id);
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
        jdbcTemplate.update(sql, id);
        log.debug("Entity removal done...");
    }

    @Override
    @Transactional
    public TagMetaDto find(String id) {
        log.debug("Find tag meta {} using JDBCTEMPLATE", id);
        try {
            final JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
            final String tablename = this.tablename();

            final String sql = TagMetaRequests.getFindQuery(tablename);
            log.debug("Use sql request {}", sql);
            // Be careful, this seems not to work with Postgres: probably getBlob loads an
            // OID and not the byte[]
            // Temporarely, try to create a postgresql implementation of this class.

            return jdbcTemplate.queryForObject(sql, new Object[]{id}, (rs, num) -> {
                final TagMetaDto entity = new TagMetaDto();
                entity.setTagName(rs.getString("TAG_NAME"));
                entity.setDescription(rs.getString("DESCRIPTION"));
                entity.setChansize(rs.getInt("CHANNEL_SIZE"));
                entity.setColsize(rs.getInt("COLUMN_SIZE"));
                entity.setInsertionTime(rs.getDate("INSERTION_TIME"));
                entity.setTagInfo(getBlob(rs, "TAG_INFO"));
                return entity;
            });
        }
        catch (final Exception e) {
            log.warn("Could not find entry for tag name {}", id);
        }
        return null;
    }

    @Override
    @Transactional
    public TagMetaDto findMetaInfo(String id) {
        log.debug("Find tag meta info {} using JDBCTEMPLATE", id);
        try {
            final JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
            final String tablename = this.tablename();
            final String sql = TagMetaRequests.getFindMetaQuery(tablename);

            return jdbcTemplate.queryForObject(sql, new Object[]{id}, (rs, num) -> {
                final TagMetaDto entity = new TagMetaDto();
                entity.setTagName(rs.getString("TAG_NAME"));
                entity.setDescription(rs.getString("DESCRIPTION"));
                entity.setChansize(rs.getInt("CHANNEL_SIZE"));
                entity.setColsize(rs.getInt("COLUMN_SIZE"));
                entity.setInsertionTime(rs.getDate("INSERTION_TIME"));
                return entity;
            });
        }
        catch (final Exception e) {
            log.warn("Could not find entry for tag {}", id);
        }
        return null;
    }

    /**
     * @param is     the InputStream
     * @param sql    the String
     * @param entity the TagMetaDto
     * @return
     * @throws CdbServiceException If an Exception occurred
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

        try (Connection conn = getDs().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, entity.getDescription());
            ps.setInt(2, entity.getChansize());
            ps.setInt(3, entity.getColsize());
            ps.setBytes(4, entity.getTagInfo().getBytes());
            ps.setDate(5, inserttime);
            // Now we set the update where condition, or tagname in insertion
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

    /**
     * @param rs  the ResultSet
     * @param key the String
     * @return byte[]
     * @throws SQLException If an Exception occurred
     */
    protected abstract String getBlob(ResultSet rs, String key) throws SQLException;

    /**
     * @param entity
     * @return TagMetaDto
     * @throws CdbServiceException
     */
    protected TagMetaDto saveBlobAsBytes(TagMetaDto entity) {

        final String tablename = this.tablename();
        final String sql = TagMetaRequests.getInsertAllQuery(tablename);
        log.info("Insert Tag meta {} using JDBCTEMPLATE ", entity.getTagName());
        execute(null, sql, entity);
        return findMetaInfo(entity.getTagName());
    }

    /**
     * @param entity
     * @return TagMetaDto
     * @throws CdbServiceException
     */
    protected TagMetaDto updateAsBytes(TagMetaDto entity) {

        final String tablename = this.tablename();
        final String sql = TagMetaRequests.getUpdateQuery(tablename);
        log.info("Update Tag meta {} using JDBCTEMPLATE ", entity.getTagName());
        execute(null, sql, entity);
        return findMetaInfo(entity.getTagName());
    }
}
