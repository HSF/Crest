/**
 *
 */
package hep.crest.data.monitoring.repositories;

import hep.crest.data.config.CrestProperties;
import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.pojo.Iov;
import hep.crest.data.pojo.Payload;
import hep.crest.swagger.model.PayloadTagInfoDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.persistence.Table;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author formica
 */
@Component
public class JdbcMonitoringRepository implements IMonitoringRepository {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(JdbcMonitoringRepository.class);

    /**
     * The datasource.
     */
    @Autowired
    private DataSource ds;

    /** The props. */
    @Autowired
    private CrestProperties props;

    /*
     * (non-Javadoc)
     * @see
     * hep.crest.data.monitoring.repositories.IMonitoringRepository#selectTagInfo(
     *java.lang.String)
     */
    @Override
    public List<PayloadTagInfoDto> selectTagInfo(String tagpattern) throws CdbServiceException {
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
        String sql;
        try {
            // Create the sql string using a query defined in a package.
            // This should word on any DB.
            sql = "select iv.tag_name, count(iv.tag_name) as niovs, sum(pl.data_size) as tot_volume, avg(pl"
                    + ".data_size) as avg_volume FROM " + this.payloadTable() + " pl, " + this.iovTable() + " iv "
                    + " WHERE iv.TAG_NAME like ? AND iv.PAYLOAD_HASH=pl.HASH group by iv.TAG_NAME order by iv.TAG_NAME";
            log.debug("Execute query {} using {}", sql, tagpattern);
            return jdbcTemplate.query(sql, new Object[]{tagpattern}, new PayloadInfoMapper());
        }
        catch (final EmptyResultDataAccessException e) {
            log.error("Cannot find tag information for pattern {}: {}", tagpattern, e);
        }
        return new ArrayList<>();
    }

    /**
     * Provide the correct table names taking into account the schema name.
     *
     * @return String
     */
    protected String payloadTable() {
        String defaultTablename = ("none".equals(props.getSchemaname())) ? null : props.getSchemaname();
        final Table ann = Payload.class.getAnnotation(Table.class);
        String tablename = ann.name();
        if (defaultTablename != null) {
            tablename = defaultTablename + "." + tablename;
        }
        log.info("Generating payload table name as {}", tablename);
        return tablename;
    }

    /**
     * Provide the correct table names taking into account the schema name.
     *
     * @return String
     */
    protected String iovTable() {
        String defaultTablename = ("none".equals(props.getSchemaname())) ? null : props.getSchemaname();
        final Table ann = Iov.class.getAnnotation(Table.class);
        String tablename = ann.name();
        if (defaultTablename != null) {
            tablename = defaultTablename + "." + tablename;
        }
        log.info("Generating iov table name as {}", tablename);
        return tablename;
    }

    /**
     * Procname.
     *
     * @return the string
     */
    protected String procname() {
        if (props.getSchemaname().isEmpty()) {
            return "CREST_TOOLS";
        }
        return props.getSchemaname() + ".CREST_TOOLS";
    }
}
