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

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.Table;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;

import hep.crest.data.config.DatabasePropertyConfigurator;
import hep.crest.data.pojo.Iov;
import hep.crest.swagger.model.TagSummaryDto;

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
    private final Logger log = LoggerFactory.getLogger(this.getClass());

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
        Long groupfreq = 1000L;
        if (groupsize != null && groupsize > 0) {
            groupfreq = groupsize;
        }
        final String sql = "select MIN(SINCE) from " + tablename + " where TAG_NAME=? "
                + " group by cast(SINCE/? as int)*?" + " order by min(SINCE)";

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
        Long groupfreq = 1000L;
        if (groupsize != null && groupsize > 0) {
            groupfreq = groupsize;
        }
        final String sql = "select MIN(SINCE) from " + tablename
                + " where TAG_NAME=? and INSERTION_TIME<=?" + " group by cast(SINCE/? as int)*?"
                + " order by min(SINCE)";

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

        final String sql = "select TAG_NAME, COUNT(TAG_NAME) as NIOVS from " + tablename
                + " where TAG_NAME like ? GROUP BY TAG_NAME";
        return jdbcTemplate.query(sql, new Object[] {tagname}, (rs, num) -> {
            final TagSummaryDto entity = new TagSummaryDto();
            entity.setTagname(rs.getString("TAG_NAME"));
            entity.setNiovs(rs.getLong("NIOVS"));
            return entity;
        });
    }
}
