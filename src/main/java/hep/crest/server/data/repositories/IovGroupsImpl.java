/**
 * This file is part of Crest.
 * <p>
 * PhysCondDB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Crest is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Crest.  If not, see <http://www.gnu.org/licenses/>.
 **/
package hep.crest.server.data.repositories;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An implementation for groups queries.
 *
 * @author formica
 */
@Slf4j
public class IovGroupsImpl extends DataGeneral implements IovGroupsCustom {
    /**
     * The upload directory for files.
     */
    @Value("${crest.upload.dir:/tmp}")
    private String serverUploadLocationFolder;

    /**
     * @param ds the DataSource
     */
    public IovGroupsImpl(DataSource ds) {
        super(ds);
    }


    @Override
    public List<BigInteger> selectSnapshotGroups(String tagname, Date snap, Long groupsize) {
        log.info(
                "Select Iov Snapshot Groups for tag {} with group size {} and snapshot time {} using JDBCTEMPLATE",
                tagname, groupsize, snap);
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(getDs());
        final String tablename = getCrestTableNames().getIovTableName();
        // Set the default group frequency at 100. This can be changed via groupsize argument.
        Long groupfreq = 100L;
        if (groupsize != null && groupsize > 10) {
            groupfreq = groupsize;
        }
        String snaptime = " and iv.INSERTION_TIME<=? ";
        if (snap == null) {
            snaptime = " and ? is null ";
        }
        // I use mod(row_id, group) = 1 because I want to select the first element of each group.
        // When using mod(row_id, group) = 0 I may get 0 rows for a number of iovs less than
        // groupsize.
        final String sql = "with tag_iov as ("
                                 + " select iv.SINCE, rownum as rid from " + tablename + " iv "
                                 + "   where iv.TAG_NAME=? " + snaptime + " ) "
                                 + "select ti.SINCE from tag_iov ti where mod(rid, ?) = 1 "
                                 + "ORDER BY SINCE";
        log.debug("Execute selectSnapshotGroups query {}", sql);

        List<BigDecimal> sinceList = jdbcTemplate.queryForList(sql, BigDecimal.class, tagname, snap, groupfreq);
        BigDecimal a = sinceList.get(0);
        log.info("Return elements like {} ", a);
        return sinceList.stream().map(BigDecimal::toBigInteger)
                .collect(Collectors.toList());
    }

    @Override
    public Long getSize(String tagname) {
        log.info("Select count(TAG_NAME) Iov for tag {} using JDBCTEMPLATE", tagname);
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(getDs());
        final String tablename = getCrestTableNames().getIovTableName();

        final String sql = "select COUNT(TAG_NAME) from " + tablename + " where TAG_NAME=?";
        log.info("Execute query {}", sql);
        return jdbcTemplate.queryForObject(sql, Long.class, tagname);
    }

    @Override
    public Long getSizeBySnapshot(String tagname, Date snap) {
        log.info("Select count(TAG_NAME) Iov for tag {} and snapshot time {} using JDBCTEMPLATE",
                tagname, snap);
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(getDs());
        final String tablename = getCrestTableNames().getIovTableName();

        final String sql = "select COUNT(TAG_NAME) from " + tablename
                           + " where TAG_NAME=? and INSERTION_TIME<=?";
        return jdbcTemplate.queryForObject(sql, Long.class, tagname, snap);
    }

}
