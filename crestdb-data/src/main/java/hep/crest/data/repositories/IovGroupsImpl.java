/**
 * 
 * This file is part of PhysCondDB.
 *
 *   PhysCondDB is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   PhysCondDB is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with PhysCondDB.  If not, see <http://www.gnu.org/licenses/>.
 **/
package hep.crest.data.repositories;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.Table;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import hep.crest.data.config.DatabasePropertyConfigurator;
import hep.crest.data.pojo.Iov;
import hep.crest.swagger.model.TagSummaryDto;

/**
 * @author formica
 *
 */
public class IovGroupsImpl implements IovGroupsCustom {

	private Logger log = LoggerFactory.getLogger(this.getClass());


	private DataSource ds;
	
	
	public IovGroupsImpl(DataSource ds) {
		super();
		this.ds = ds;
	}


	/* (non-Javadoc)
	 * @see hep.phycdb.svc.repositories.IovGroupsCustom#selectGroups(java.lang.String, java.lang.Integer)
	 */
	@Override
	public List<BigDecimal> selectGroups(String tagname, Integer groupsize) {
		log.info("Select Iov Groups for tag " + tagname + " using JDBCTEMPLATE");
		JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
		Table ann = Iov.class.getAnnotation(Table.class);
		String tablename = ann.name();
		if (!DatabasePropertyConfigurator.SCHEMA_NAME.isEmpty()) {
			tablename = DatabasePropertyConfigurator.SCHEMA_NAME+"."+tablename;
		}
		Integer groupfreq = 1000;
		if (groupsize != null && groupsize > 0) {
			groupfreq = groupsize;
		}
		String sql = "select MIN(SINCE) from "+tablename 
				+" where TAG_NAME=? "
				+" group by cast(SINCE/? as int)*?"
				+" order by min(SINCE)";
		
		List<BigDecimal> sincelist = jdbcTemplate.queryForList(sql, BigDecimal.class, new Object[] { tagname, groupfreq, groupfreq });
		//jdbcTemplate.
		return sincelist;
	}


	/* (non-Javadoc)
	 * @see hep.phycdb.svc.repositories.IovGroupsCustom#selectSnapshotGroups(java.lang.String, java.util.Date, java.lang.Integer)
	 */
	@Override
	public List<BigDecimal> selectSnapshotGroups(String tagname, Date snap, Integer groupsize) {
		log.info("Select Iov Snapshot Groups for tag " + tagname + " and snapshot time "+snap+" using JDBCTEMPLATE");
		JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
		Table ann = Iov.class.getAnnotation(Table.class);
		String tablename = ann.name();
		if (!DatabasePropertyConfigurator.SCHEMA_NAME.isEmpty()) {
			tablename = DatabasePropertyConfigurator.SCHEMA_NAME+"."+tablename;
		}
		Integer groupfreq = 1000;
		if (groupsize != null && groupsize > 0) {
			groupfreq = groupsize;
		}
		String sql = "select MIN(SINCE) from "+tablename 
				+" where TAG_NAME=? and INSERTION_TIME<=?"
				+" group by cast(SINCE/? as int)*?"
				+" order by min(SINCE)";
		
		List<BigDecimal> sincelist = jdbcTemplate.queryForList(sql, BigDecimal.class, new Object[] { tagname, snap, groupfreq, groupfreq });
		return sincelist;
	}


	@Override
	public Long getSize(String tagname) {
		log.info("Select count(TAG_NAME) Iov for tag " + tagname + " using JDBCTEMPLATE");
		JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
		Table ann = Iov.class.getAnnotation(Table.class);
		String tablename = ann.name();
		if (!DatabasePropertyConfigurator.SCHEMA_NAME.isEmpty()) {
			tablename = DatabasePropertyConfigurator.SCHEMA_NAME+"."+tablename;
		}
		String sql = "select COUNT(TAG_NAME) from "+ tablename +" where TAG_NAME=?";
		
		//Long count = jdbcTemplate.queryForLong(sql, BigDecimal.class, new Object[] { tagname, snap, groupfreq, groupfreq });
		Long count = jdbcTemplate.queryForObject(sql, Long.class, new Object[] { tagname } );
		//jdbcTemplate.
		return count;
	}
	
	@Override
	public Long getSizeBySnapshot(String tagname, Date snap) {
		log.info("Select count(TAG_NAME) Iov for tag " + tagname + " using JDBCTEMPLATE");
		JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
		Table ann = Iov.class.getAnnotation(Table.class);
		String tablename = ann.name();
		if (!DatabasePropertyConfigurator.SCHEMA_NAME.isEmpty()) {
			tablename = DatabasePropertyConfigurator.SCHEMA_NAME+"."+tablename;
		}
		String sql = "select COUNT(TAG_NAME) from "+ tablename +" where TAG_NAME=? and INSERTION_TIME<=?";
		
		
		
		//Long count = jdbcTemplate.queryForLong(sql, BigDecimal.class, new Object[] { tagname, snap, groupfreq, groupfreq });
		//Long count = jdbcTemplate.queryForLong(sql, Long.class, new Object[] { tagname, snap });
		Long count = jdbcTemplate.queryForObject(sql, Long.class, new Object[] { tagname, snap} );
		//jdbcTemplate.
		return count;
	}


	/* (non-Javadoc)
	 * @see hep.phycdb.svc.repositories.IovGroupsCustom#getTagSummaryInfo(java.lang.String)
	 */
	@Override
	public List<TagSummaryDto> getTagSummaryInfo(String tagname) {
		log.info("Select count(TAG_NAME) Iov for tag matching pattern " + tagname + " using JDBCTEMPLATE");
		JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
		Table ann = Iov.class.getAnnotation(Table.class);
		String tablename = ann.name();
		if (!DatabasePropertyConfigurator.SCHEMA_NAME.isEmpty()) {
			tablename = DatabasePropertyConfigurator.SCHEMA_NAME+"."+tablename;
		}
		String sql = "select TAG_NAME, COUNT(TAG_NAME) as NIOVS from "+ tablename +" where TAG_NAME like ? GROUP BY TAG_NAME";
		List<TagSummaryDto> dtolist = jdbcTemplate.query(sql, new Object[] { tagname }, (rs, num) -> {
			final TagSummaryDto entity = new TagSummaryDto();
			entity.setTagname(rs.getString("TAG_NAME"));
			entity.setNiovs(rs.getLong("NIOVS"));
			return entity;
		});
		return dtolist;
	}
	
}
