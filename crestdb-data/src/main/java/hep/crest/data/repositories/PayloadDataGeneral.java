package hep.crest.data.repositories;

import java.io.InputStream;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.Table;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import hep.crest.data.config.DatabasePropertyConfigurator;
import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.handlers.PayloadHandler;
import hep.crest.data.pojo.Payload;
import hep.crest.data.repositories.externals.PayloadRequests;
import hep.crest.swagger.model.PayloadDto;

/**
 * General base class for repository implementations.
 * 
 * @author formica
 *
 */
public abstract class PayloadDataGeneral implements PayloadDataBaseCustom {

    /**
     * Logger.
     */
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    /**
     * The Data Source.
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
    public PayloadDataGeneral(DataSource ds) {
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

    /**
     * @param payloadHandler
     *            the PayloadHandler
     * @return
     */
    public void setPayloadHandler(PayloadHandler payloadHandler) {
        this.payloadHandler = payloadHandler;
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

    /**
     * @return the payloadHandler
     */
    protected PayloadHandler getPayloadHandler() {
        return payloadHandler;
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
        catch (final Exception e) {
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
            log.info("Look if hash exists...{}", entity.getHash());
            savedentity = findMetaInfo(entity.getHash());
            if (savedentity != null) {
                log.warn("The hash {} already exists...return the existing entity...",
                        entity.getHash());
                return savedentity;
            }
            log.info("Hash was not found, we can insert the new one...{}", entity.getHash());
            this.saveBlobAsStream(entity, is);
            savedentity = findMetaInfo(entity.getHash());
        }
        catch (final CdbServiceException e) {
            log.error("Exception during payload dto insertion: {}", e.getMessage());
        }
        return savedentity;
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

    /* (non-Javadoc)
     * @see hep.crest.data.repositories.PayloadDataBaseCustom#find(java.lang.String)
     */
    @Override
    @Transactional
    public Payload find(String id) {
        log.info("Find payload {} using JDBCTEMPLATE", id);
        try {
            final JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
            final String tablename = this.tablename();
    
            final String sql = PayloadRequests.getFindQuery(tablename);
    
            // Be careful, this seems not to work with Postgres: probably getBlob loads an
            // OID and not the byte[]
            // Temporarely, try to create a postgresql implementation of this class.
    
            return jdbcTemplate.queryForObject(sql, new Object[] {id}, (rs, num) -> {
                final Payload entity = new Payload();
                entity.setHash(rs.getString("HASH"));
                entity.setObjectType(rs.getString("OBJECT_TYPE"));
                entity.setVersion(rs.getString("VERSION"));
                entity.setInsertionTime(rs.getDate("INSERTION_TIME"));
                entity.setData(getBlob(rs, "DATA"));
                entity.setStreamerInfo(getBlob(rs, "STREAMER_INFO"));
                entity.setSize(rs.getInt("DATA_SIZE"));
    
                return entity;
            });
        }
        catch (final Exception e) {
            log.warn("Could not find entry for hash {}", id);
        }
        return null;
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
        log.info("Find payload meta info {} using JDBCTEMPLATE", id);
        try {
            final JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
            final String tablename = this.tablename();
            final String sql = PayloadRequests.getFindMetaQuery(tablename);

            return jdbcTemplate.queryForObject(sql, new Object[] {id}, (rs, num) -> {
                final Payload entity = new Payload();
                entity.setHash(rs.getString("HASH"));
                entity.setObjectType(rs.getString("OBJECT_TYPE"));
                entity.setVersion(rs.getString("VERSION"));
                entity.setInsertionTime(rs.getDate("INSERTION_TIME"));
                entity.setStreamerInfo(getBlob(rs, "STREAMER_INFO"));
                entity.setSize(rs.getInt("DATA_SIZE"));

                return entity;
            });
        }
        catch (final Exception e) {
            log.warn("Could not find entry for hash {}", id);
        }
        return null;

    }
    
    /**
     * @param rs the ResultSet
     * @param key the String
     * @throws SQLException If an Exception occurred
     * @return Blob
     */
    protected abstract Blob getBlob(ResultSet rs, String key) throws SQLException;
    
    /**
     * @param entity
     *            the PaloadDto
     * @param is
     *            the InputStream
     * @throws CdbServiceException
     *             If an Exception occurred
     * @return
     */
    protected abstract void saveBlobAsStream(PayloadDto entity, InputStream is)
            throws CdbServiceException;

    /**
     * @param entity
     *            the PayloadDto
     * @throws CdbServiceException
     *             If an Exception occurred
     * @return Payload
     */
    protected abstract Payload saveBlobAsBytes(PayloadDto entity) throws CdbServiceException;
}
