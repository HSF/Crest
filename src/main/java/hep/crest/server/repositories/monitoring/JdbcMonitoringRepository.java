/**
 *
 */
package hep.crest.server.repositories.monitoring;

import hep.crest.server.data.repositories.DataGeneral;
import hep.crest.server.data.repositories.externals.SqlRequests;
import hep.crest.server.swagger.model.IovPayloadDto;
import hep.crest.server.swagger.model.PayloadTagInfoDto;
import hep.crest.server.swagger.model.TagSummaryDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.math.BigInteger;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author formica
 */
@Slf4j
public class JdbcMonitoringRepository extends DataGeneral implements IMonitoringRepository {

    /**
     * @param ds the DataSource
     */
    public JdbcMonitoringRepository(DataSource ds) {
        super(ds);
    }

    /*
     * (non-Javadoc)
     * @see
     * hep.crest.data.monitoring.repositories.IMonitoringRepository#selectTagInfo(
     *java.lang.String)
     */
    @Override
    public List<PayloadTagInfoDto> selectTagInfo(String tagpattern) {
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(getDs());
        String sql;
        try {
            // Create the sql string using a query defined in a package.
            // This should word on any DB.
            sql = "select iv.tag_name, count(iv.tag_name) as niovs, sum(pl.data_size) as tot_volume, avg(pl"
                  + ".data_size) as avg_volume FROM " + getCrestTableNames().getPayloadTableName() + " pl, "
                  + getCrestTableNames().getIovTableName() + " iv "
                  + " WHERE iv.TAG_NAME like ? AND iv.PAYLOAD_HASH=pl.HASH group by iv.TAG_NAME order by iv.TAG_NAME";
            log.debug("Execute query {} using {}", sql, tagpattern);
            return jdbcTemplate.query(sql, new PayloadInfoMapper(), tagpattern);
        }
        catch (final EmptyResultDataAccessException e) {
            // No result, log the error.
            log.error("Cannot find tag information for pattern {}: {}", tagpattern, e);
        }
        return new ArrayList<>();
    }

    @Override
    public List<TagSummaryDto> getTagSummaryInfo(String tagname) {
        log.info("Select count(TAG_NAME) Iov for tag matching pattern {} using JDBCTEMPLATE",
                tagname);
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(getDs());
        final String tablename = getCrestTableNames().getIovTableName();
        // sql : count iovs in a tag
        // select TAG_NAME, COUNT(TAG_NAME) as NIOVS from IOV
        // where TAG_NAME like ? GROUP BY TAG_NAME
        final String sql = "select TAG_NAME, COUNT(TAG_NAME) as NIOVS from " + tablename
                           + " where TAG_NAME like ? GROUP BY TAG_NAME";
        return jdbcTemplate.query(sql, (rs, num) -> {
            final TagSummaryDto entity = new TagSummaryDto();
            entity.setTagname(rs.getString("TAG_NAME"));
            entity.setNiovs(rs.getLong("NIOVS"));
            return entity;
        }, tagname);
    }

    @Override
    public List<IovPayloadDto> getRangeIovPayloadInfo(String name, BigInteger since,
                                                      BigInteger until, Date snapshot) {
        log.debug("Select Iov and Payload meta info for tag  {} using JDBCTEMPLATE", name);
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(getDs());
        // Get sql query.
        final String sql = SqlRequests.getRangeIovPayloadQuery(getCrestTableNames().getIovTableName(),
                getCrestTableNames().getPayloadTableName());
        // Execute request.
        return jdbcTemplate.query(sql, (rs, num) -> {
            final IovPayloadDto entity = new IovPayloadDto();
            Instant inst = Instant.ofEpochMilli(rs.getTimestamp("INSERTION_TIME").getTime());
            entity.setSince(rs.getBigDecimal("SINCE"));
            entity.setInsertionTime(inst.atOffset(ZoneOffset.UTC));
            entity.setPayloadHash(rs.getString("PAYLOAD_HASH"));
            entity.setVersion(rs.getString("VERSION"));
            entity.setObjectType(rs.getString("OBJECT_TYPE"));
            entity.setObjectName(rs.getString("OBJECT_NAME"));
            entity.setSize(rs.getInt("DATA_SIZE"));
            log.debug("create entity {}", entity);
            return entity;
        }, name, name, since, snapshot, until, snapshot);
    }

    /**
     * Procname. Use stored procedures.
     *
     * @return the string
     */
    protected String procname() {
        // Get the package name.
        if (getCrestTableNames().getDefaultTablename() == null
            || getCrestTableNames().getDefaultTablename().isEmpty()) {
            return "CREST_TOOLS";
        }
        return getCrestTableNames().getDefaultTablename() + ".CREST_TOOLS";
    }
}
