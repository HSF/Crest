package hep.crest.data.repositories;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.Table;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import hep.crest.data.config.DatabasePropertyConfigurator;
import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.pojo.TagMeta;
import hep.crest.data.repositories.externals.TagMetaRequests;
import hep.crest.swagger.model.TagMetaDto;

/**
 * General base class for repository implementations.
 * 
 * @author formica
 *
 */
public abstract class TagMetaGeneral implements TagMetaDataBaseCustom {

    /**
     * Logger.
     */
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    /**
     * The Data Source.
     */
    private final DataSource ds;
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
    public TagMetaGeneral(DataSource ds) {
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
        final Table ann = TagMeta.class.getAnnotation(Table.class);
        String tablename = ann.name();
        if (!DatabasePropertyConfigurator.SCHEMA_NAME.isEmpty()) {
            tablename = DatabasePropertyConfigurator.SCHEMA_NAME + "." + tablename;
        }
        else if (this.defaultTablename != null) {
            tablename = this.defaultTablename + "." + tablename;
        }
        return tablename;
    }

    /**
     * @return DataSource
     */
    protected DataSource getDs() {
        return ds;
    }

    /**
     * @return the serverUploadLocationFolder
     */
    protected String getServerUploadLocationFolder() {
        return serverUploadLocationFolder;
    }

    /**
     * @param serverUploadLocationFolder
     *            the serverUploadLocationFolder to set
     */
    protected void setServerUploadLocationFolder(String serverUploadLocationFolder) {
        this.serverUploadLocationFolder = serverUploadLocationFolder;
    }

    @Override
    public TagMetaDto save(TagMetaDto entity) throws CdbServiceException {
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
    public TagMetaDto update(TagMetaDto entity) throws CdbServiceException {
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

            return jdbcTemplate.queryForObject(sql, new Object[] {id}, (rs, num) -> {
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

            return jdbcTemplate.queryForObject(sql, new Object[] {id}, (rs, num) -> {
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
     * @param rs
     *            the ResultSet
     * @param key
     *            the String
     * @throws SQLException
     *             If an Exception occurred
     * @return byte[]
     */
    protected abstract String getBlob(ResultSet rs, String key) throws SQLException;

    /**
     * @param entity
     *            the TagMetaDto
     * @throws CdbServiceException
     *             If an Exception occurred
     * @return TagMetaDto
     */
    protected abstract TagMetaDto saveBlobAsBytes(TagMetaDto entity) throws CdbServiceException;

    /**
     * @param entity
     *            the TagMetaDto
     * @throws CdbServiceException
     *             If an Exception occurred
     * @return TagMetaDto
     */
    protected abstract TagMetaDto updateAsBytes(TagMetaDto entity) throws CdbServiceException;
}
