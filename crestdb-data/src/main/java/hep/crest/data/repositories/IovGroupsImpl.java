/**
 * 
 * This file is part of Crest.
 *
 *   PhysCondDB is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Crest is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Crest.  If not, see <http://www.gnu.org/licenses/>.
 **/
package hep.crest.data.repositories;

import hep.crest.data.config.DatabasePropertyConfigurator;
import hep.crest.data.pojo.Iov;
import hep.crest.data.pojo.Payload;
import hep.crest.swagger.model.IovPayloadDto;
import hep.crest.swagger.model.TagSummaryDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.persistence.Table;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Date;
import java.util.List;

/**
 * An implementation for groups queries.
 *
 * @author formica
 *
 */
public class IovGroupsImpl implements IovGroupsCustom {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(IovGroupsImpl.class);

    /**
     * Datasource.
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
    public IovGroupsImpl(DataSource ds) {
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
     * @return DataSource
     */
    protected DataSource getDs() {
        return ds;
    }

    /**
     * Use annotations to generate the table name in SQL requests. In general it
     * adds the schema name.
     *
     * @return String
     */
    protected String tablename() {
        final Table ann = Iov.class.getAnnotation(Table.class);
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
     * @return String
     */
    protected String payloadTablename() {
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
     * hep.phycdb.svc.repositories.IovGroupsCustom#selectGroups(java.lang.String,
     * java.lang.Long)
     */
    @Override
    public List<BigDecimal> selectGroups(String tagname, Long groupsize) {
        log.info("Select Iov Groups for tag {} with group size {} using JDBCTEMPLATE", tagname,
                groupsize);
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
        final String tablename = this.tablename();
        // Set the default group frequency at 1000. This can be changed via groupsize argument.
        Long groupfreq = 1000L;
        if (groupsize != null && groupsize > 0) {
            groupfreq = groupsize;
        }
        final String sql = "select MIN(SINCE) from " + tablename + " where TAG_NAME=? "
                + " group by cast(SINCE/? as int)*?" + " order by min(SINCE)";
        log.debug("Execute selectGroups query {}", sql);
        return jdbcTemplate.queryForList(sql, BigDecimal.class, tagname, groupfreq, groupfreq);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * hep.phycdb.svc.repositories.IovGroupsCustom#selectSnapshotGroups(java.lang.
     * String, java.util.Date, java.lang.Integer)
     */
    @Override
    public List<BigDecimal> selectSnapshotGroups(String tagname, Date snap, Long groupsize) {
        log.info(
                "Select Iov Snapshot Groups for tag {} with group size {} and snapshot time {} using JDBCTEMPLATE",
                tagname, groupsize, snap);
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
        final String tablename = this.tablename();
        // Set the default group frequency at 1000. This can be changed via groupsize argument.
        Long groupfreq = 1000L;
        if (groupsize != null && groupsize > 0) {
            groupfreq = groupsize;
        }
        final String sql = "select MIN(SINCE) from " + tablename
                + " where TAG_NAME=? and INSERTION_TIME<=?" + " group by cast(SINCE/? as int)*?"
                + " order by min(SINCE)";
        log.debug("Execute selectSnapshotGroups query {}", sql);

        return jdbcTemplate.queryForList(sql, BigDecimal.class, tagname, snap, groupfreq,
                groupfreq);
    }

    /*
     * (non-Javadoc)
     *
     * @see hep.crest.data.repositories.IovGroupsCustom#getSize(java.lang.String)
     */
    @Override
    public Long getSize(String tagname) {
        log.info("Select count(TAG_NAME) Iov for tag {} using JDBCTEMPLATE", tagname);
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
        final String tablename = this.tablename();

        final String sql = "select COUNT(TAG_NAME) from " + tablename + " where TAG_NAME=?";
        log.info("Execute query {}", sql);
        return jdbcTemplate.queryForObject(sql, Long.class, tagname);
    }

    /*
     * (non-Javadoc)
     *
     * @see hep.crest.data.repositories.IovGroupsCustom#getSizeBySnapshot(java.lang.
     * String, java.util.Date)
     */
    @Override
    public Long getSizeBySnapshot(String tagname, Date snap) {
        log.info("Select count(TAG_NAME) Iov for tag {} and snapshot time {} using JDBCTEMPLATE",
                tagname, snap);
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
        final String tablename = this.tablename();

        final String sql = "select COUNT(TAG_NAME) from " + tablename
                + " where TAG_NAME=? and INSERTION_TIME<=?";
        return jdbcTemplate.queryForObject(sql, Long.class, tagname, snap);
    }

    /*
     * (non-Javadoc)
     *
     * @see hep.phycdb.svc.repositories.IovGroupsCustom#getTagSummaryInfo(java.lang.
     * String)
     */
    @Override
    public List<TagSummaryDto> getTagSummaryInfo(String tagname) {
        log.info("Select count(TAG_NAME) Iov for tag matching pattern {} using JDBCTEMPLATE",
                tagname);
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
        final String tablename = this.tablename();
        // sql : count iovs in a tag
        // select TAG_NAME, COUNT(TAG_NAME) as NIOVS from IOV
        // where TAG_NAME like ? GROUP BY TAG_NAME
        final String sql = "select TAG_NAME, COUNT(TAG_NAME) as NIOVS from " + tablename
                + " where TAG_NAME like ? GROUP BY TAG_NAME";
        return jdbcTemplate.query(sql, new Object[] {tagname}, (rs, num) -> {
            final TagSummaryDto entity = new TagSummaryDto();
            entity.setTagname(rs.getString("TAG_NAME"));
            entity.setNiovs(rs.getLong("NIOVS"));
            return entity;
        });
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * hep.crest.data.repositories.IovGroupsCustom#getRangeIovPayloadInfo(java.lang
     * .String, java.math.BigDecimal, java.math.BigDecimal, java.util.Date)
     */
    @Override
    public List<IovPayloadDto> getRangeIovPayloadInfo(String name, BigDecimal since,
            BigDecimal until, Date snapshot) {
        log.debug("Select Iov and Payload meta info for tag  {} using JDBCTEMPLATE", name);
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
        final String tablename = this.tablename();

        // sql : select only metadata from payload table and link with the IOV table
        // select iv.*, pyld.VERSION, pyld.OBJECT_TYPE, pyld.DATA_SIZE from IOV iv
        // left join PAYLOAD pyld ON pyld.HASH=iv.PAYLOAD_HASH
        // where iv.TAG_NAME=? AND iv.SINCE>=COALESCE((SELECT max(iov2.SINCE) FROM
        // IOV iov2 WHERE iov2.TAG_NAME=? AND iov2.SINCE<=? AND iov2.INSERTION_TIME<=? ),0)
        // AND iv.SINCE<=? AND iv.INSERTION_TIME<=?
        // order by iv.SINCE ASC, iv.INSERTION_TIME DESC

        final String sql = "select iv.TAG_NAME, iv.SINCE, iv.INSERTION_TIME, iv.PAYLOAD_HASH, pyld.STREAMER_INFO, "
                + " pyld.VERSION, pyld.OBJECT_TYPE, " + " pyld.DATA_SIZE from " + tablename + " iv "
                + " LEFT JOIN " + payloadTablename() + " pyld " + " ON iv.PAYLOAD_HASH=pyld.HASH "
                + " where iv.TAG_NAME=? AND iv.SINCE>=COALESCE(" + "  (SELECT max(iov2.SINCE) FROM "
                + tablename + " iov2 "
                + "  WHERE iov2.TAG_NAME=? AND iov2.SINCE<=? AND iov2.INSERTION_TIME<=? ),0)"
                + " AND iv.SINCE<=? AND iv.INSERTION_TIME<=? "
                + " ORDER BY iv.SINCE ASC, iv.INSERTION_TIME DESC";
        return jdbcTemplate.query(sql,
                new Object[] {name, name, since, snapshot, until, snapshot}, (rs, num) -> {
                    final IovPayloadDto entity = new IovPayloadDto();
                    entity.setSince(rs.getBigDecimal("SINCE"));
                    entity.setInsertionTime(rs.getTimestamp("INSERTION_TIME"));
                    entity.setPayloadHash(rs.getString("PAYLOAD_HASH"));
                    entity.setVersion(rs.getString("VERSION"));
                    entity.setObjectType(rs.getString("OBJECT_TYPE"));
                    entity.setSize(rs.getInt("DATA_SIZE"));
                    entity.setStreamerInfo(getBlob(rs, "STREAMER_INFO"));
                    log.debug("create entity {}", entity);
                    return entity;
                });
    }

    /**
     * Transform streamerInfo bytes into String.
     * This method should deal with different types of binary content. We want a normal string if possible, or base64
     * encoded if the content is really binary.
     *
     * @param rs the ResulSet
     * @param key the column String
     * @return String
     * @throws SQLException
     */
    protected String getBlob(ResultSet rs, String key) throws SQLException {
        CharsetDecoder decoder = StandardCharsets.US_ASCII.newDecoder();

        decoder.onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);

        byte[] streaminfoByteArr = rs.getBytes(key);
        try {
            return decoder.decode(ByteBuffer.wrap(streaminfoByteArr))
                    .toString();
        }
        catch (CharacterCodingException e) {
            log.warn("Cannot decode as String with charset US_ASCII, use base64: {}", e);
            return Base64.getEncoder().encodeToString(streaminfoByteArr);
        }
    }
}
