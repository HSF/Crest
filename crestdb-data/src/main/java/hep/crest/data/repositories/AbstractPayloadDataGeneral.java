package hep.crest.data.repositories;

import hep.crest.data.config.DatabasePropertyConfigurator;
import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.handlers.PayloadHandler;
import hep.crest.data.pojo.Payload;
import hep.crest.data.repositories.externals.PayloadRequests;
import hep.crest.swagger.model.PayloadDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
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
 *
 */
public abstract class AbstractPayloadDataGeneral implements PayloadDataBaseCustom {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(AbstractPayloadDataGeneral.class);
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
    public AbstractPayloadDataGeneral(DataSource ds) {
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
     * @param id the String
     * @return String
     */
    @Override
    public String exists(String id) {
        log.info("Find payload {} using JDBCTEMPLATE", id);
        try {
            final JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
            final String tablename = this.tablename();

            final String sql = PayloadRequests.getExistsHashQuery(tablename);

            return jdbcTemplate.queryForObject(sql, new Object[] {id}, (rs, num) -> {
                final String dbhash = rs.getString("HASH");
                return dbhash;
            });
        }
        catch (final DataAccessException e) {
            log.warn("Hash {} does not exists: {}", id, e);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * hep.phycdb.svc.repositories.PayloadDataBaseCustom#save(hep.phycdb.data.pojo.
     * Payload)
     */
    @Override
    public PayloadDto save(PayloadDto entity) throws CdbServiceException {
        PayloadDto savedentity = null;
        try {
            savedentity = this.saveBlobAsBytes(entity);
        }
        catch (final CdbServiceException e) {
            log.error("Error in save paylod dto : {}", e);
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
    public PayloadDto save(PayloadDto entity, InputStream is) throws CdbServiceException {
        PayloadDto savedentity = null;
        try {
            savedentity = this.saveBlobAsStream(entity, is);
        }
        catch (final CdbServiceException e) {
            log.error("Exception during payload dto insertion: {}", e);
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
    public PayloadDto find(String id) {
        log.info("Find payload {} using JDBCTEMPLATE", id);
        try {
            final JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
            final String tablename = this.tablename();
    
            final String sql = PayloadRequests.getFindQuery(tablename);
    
            // Be careful, this seems not to work with Postgres: probably getBlob loads an
            // OID and not the byte[]
            // Temporarely, try to create a postgresql implementation of this class.
    
            return jdbcTemplate.queryForObject(sql, new Object[] {id}, (rs, num) -> {
                final PayloadDto entity = new PayloadDto();
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
        catch (final DataAccessException e) {
            log.warn("Could not find entry for hash {}: {}", id, e);
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
    public PayloadDto findMetaInfo(String id) {
        log.info("Find payload meta info {} using JDBCTEMPLATE", id);
        try {
            final JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
            final String tablename = this.tablename();
            final String sql = PayloadRequests.getFindMetaQuery(tablename);

            return jdbcTemplate.queryForObject(sql, new Object[] {id}, (rs, num) -> {
                final PayloadDto entity = new PayloadDto();
                entity.setHash(rs.getString("HASH"));
                entity.setObjectType(rs.getString("OBJECT_TYPE"));
                entity.setVersion(rs.getString("VERSION"));
                entity.setInsertionTime(rs.getDate("INSERTION_TIME"));
                entity.setStreamerInfo(getBlob(rs, "STREAMER_INFO"));
                entity.setSize(rs.getInt("DATA_SIZE"));

                return entity;
            });
        }
        catch (final DataAccessException e) {
            log.warn("Could not find meta info entry for hash {}: {}", id, e);
        }
        return null;
    }
    
    /**
     * @param is
     *            the InputStream
     * @param sql
     *            the String
     * @param entity
     *            the PayloadDto
     * @throws CdbServiceException
     *             If an Exception occurred
     * @return
     */
    protected void execute(InputStream is, String sql, PayloadDto entity) {

        final Calendar calendar = Calendar.getInstance();
        final java.sql.Date inserttime = new java.sql.Date(calendar.getTime().getTime());
        entity.setInsertionTime(calendar.getTime());

        if (is != null) {
            final byte[] blob = PayloadHandler.getBytesFromInputStream(is);
            if (blob != null) {
                entity.setSize(blob.length);
                entity.setData(blob);
                log.debug("Read data blob of length {} and streamer info {}", blob.length,
                        entity.getStreamerInfo().length);
            }
        }

        try (Connection conn = ds.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, entity.getHash());
            ps.setString(2, entity.getObjectType());
            ps.setString(3, entity.getVersion());
            ps.setBytes(4, entity.getData());
            ps.setBytes(5, entity.getStreamerInfo());
            ps.setDate(6, inserttime);
            ps.setInt(7, entity.getSize());
            log.debug("Dump preparedstatement {} using sql {} and arguments : {} {} {} {}", ps, sql,
                    entity.getHash(), entity.getObjectType(), entity.getVersion(),
                    entity.getInsertionTime());
            ps.execute();
            log.debug("Search for stored payload as a verification, use hash {} ",
                    entity.getHash());
        }
        catch (final SQLException e) {
            log.error("Sql exception when storing payload with sql {} : {}", sql, e);
        }
        finally {
            try {
                if (is != null) {
                    is.close();
                }
            }
            catch (final IOException e) {
                log.error("Error in closing streams...potential leak: {}", e);
            }
        }
    }

    
    /**
     * @param rs the ResultSet
     * @param key the String
     * @throws SQLException If an Exception occurred
     * @return byte[]
     */
    protected abstract byte[] getBlob(ResultSet rs, String key) throws SQLException;
    
    /**
     * @param entity
     *            the PaloadDto
     * @param is
     *            the InputStream
     * @throws CdbServiceException
     *             If an Exception occurred
     * @return PayloadDto
     */
    protected PayloadDto saveBlobAsStream(PayloadDto entity, InputStream is) throws CdbServiceException {
        final String tablename = this.tablename();

        final String sql = PayloadRequests.getInsertAllQuery(tablename);

        log.info("Insert Payload with hash {} using saveBlobAsStream", entity.getHash());
        execute(is, sql, entity);
        return findMetaInfo(entity.getHash());
    }

    /**
     * @param entity
     *            the PayloadDto
     * @throws CdbServiceException
     *             If an Exception occurred
     * @return PayloadDto
     */
    protected PayloadDto saveBlobAsBytes(PayloadDto entity) throws CdbServiceException {

        final String tablename = this.tablename();
        final String sql = PayloadRequests.getInsertAllQuery(tablename);

        log.info("Insert Payload with hash {} using saveBlobAsBytes", entity.getHash());
        execute(null, sql, entity);
        return findMetaInfo(entity.getHash());
    }

}
