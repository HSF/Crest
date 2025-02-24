package hep.crest.server.repositories.triggerdb;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Blob;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class TriggerDb implements ITriggerDb {

    /**
     * Query map.
     */
    private static final Map<String, String> QUERY_MAP = Map.of(
            "L1PS", "select L1PS_DATA from %s.%s where l1ps_id=?",
            "HLTM", "select HMT.HTM_DATA from %s.SUPER_MASTER_TABLE SMT, %s.%s HMT "
                    + "where HMT.HTM_ID=SMT.SMT_HLT_MENU_ID and SMT.SMT_ID=?",
            "L1M", "select L1MT.L1TM_DATA from %s.SUPER_MASTER_TABLE SMT, %s.%s L1MT "
                   + "where L1MT.L1TM_ID=SMT.SMT_L1_MENU_ID and SMT.SMT_ID=?",
            "HLTPS", "select HPS_DATA from %s.%s where hps_id=?",
            "BGS", "select L1BGS_DATA from %s.%s where l1bgs_id=?",
            "MGS", "select HMG.HMG_DATA from %s.SUPER_MASTER_TABLE SMT, %s.%s HMG "
                  + " where HMG.HMG_IN_USE=1 and SMT.SMT_HLT_MENU_ID = HMG.HMG_HLT_MENU_ID "
                  + " and SMT.SMT_ID=?",
            "JO", "select JO.HJO_DATA from %s.SUPER_MASTER_TABLE SMT, %s.%s JO "
                  + " where JO.HJO_ID=SMT.SMT_HLT_JOBOPTIONS_ID "
                  + " and SMT.SMT_ID=?"
    );

    /**
     * Schema map.
     */
    private static final Map<String, String> SCHEMA_MAP = Map.of(
            "CONF_DATA_RUN3", "ATLAS_CONF_TRIGGER_RUN3",
            "CONF_MC_RUN3", "ATLAS_CONF_TRIGGER_MC_RUN3",
            "CONF_REPR_RUN3", "ATLAS_CONF_TRIGGER_REPR_RUN3"
    );

    /**
     * Table map.
     */
    private static final Map<String, String> TABLE_MAP = Map.of(
            "L1PS", "l1_prescale_set",
            "HLTM", "hlt_menu",
            "L1M", "l1_menu",
            "HLTPS", "hlt_prescale_set",
            "BGS", "l1_bunch_group_set",
            "MGS", "hlt_monitoring_groups",
            "JO", "hlt_joboptions"
    );

    /**
     * Column name.
     */
    private static final Map<String, String> COLUMN_MAP = Map.of(
            "L1PS", "L1PS_DATA",
            "HLTM", "HTM_DATA",
            "L1M", "L1TM_DATA",
            "HLTPS", "HPS_DATA",
            "BGS", "L1BGS_DATA",
            "MGS", "HMG_DATA",
            "JO", "HJO_DATA"
    );

    /**
     * The JdbcTemplate.
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * @param ds the DataSource
     */
    public TriggerDb(DataSource ds) {
        this.jdbcTemplate = new JdbcTemplate(ds);
    }

    /**
     * Get TriggerDB data using the provided hash-URL.
     *
     * @param components
     * @return InputStream
     */
    public InputStream getTriggerDBData(UrlComponents components) {
        String sql = buildSql(components);
        try {
            Long id = components.getId();
            String columnName = COLUMN_MAP.get(components.getTable());
            log.debug("Execute query {} using {}", sql, id);

            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                Blob blob = rs.getBlob(columnName);
                return blob.getBinaryStream();
            }, id);
        }
        catch (EmptyResultDataAccessException e) {
            // No result, log the error.
            log.error("Cannot find {} for ID {}: {}", components.getTable(),
                    components.getId(), e.getMessage());
            return null;
        }
    }

    /**
     * Get the SQL query.
     * @param components
     * @return String
     */
    protected String buildSql(UrlComponents components) {
        String schema = components.getSchema();
        if (SCHEMA_MAP.containsKey(schema)) {
            schema = SCHEMA_MAP.get(schema);
        }
        else {
            throw new IllegalArgumentException("Unknown schema: " + schema);
        }
        if (!QUERY_MAP.containsKey(components.getTable())) {
            throw new IllegalArgumentException("Unknown table: " + components.getTable());
        }
        String query = QUERY_MAP.get(components.getTable());
        switch (components.getTable()) {
            case "L1PS":
            case "HLTPS":
            case "BGS":
                return String.format(query, schema, TABLE_MAP.get(components.getTable()));
            default:
                return String.format(query, schema, schema, TABLE_MAP.get(components.getTable()));
        }
    }

    /**
     * Parse the URL for trigger://schema/pkt/id.
     *
     * @param url
     * @return UrlComponents
     */
    public UrlComponents parseUrl(String url) {
        String regex = "^triggerdb://([^/]+)/([^/]+)/([^/]+)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);

        if (matcher.matches()) {
            String schema = matcher.group(1);
            String table = matcher.group(2);
            String id = matcher.group(3);
            return new UrlComponents(schema, table, Long.valueOf(id));
        }
        else {
            throw new IllegalArgumentException("Invalid URL: " + url);
        }
    }
}
