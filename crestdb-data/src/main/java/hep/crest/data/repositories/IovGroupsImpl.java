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
import org.springframework.jdbc.core.JdbcTemplate;

import hep.crest.data.config.DatabasePropertyConfigurator;
import hep.crest.data.pojo.Iov;
import hep.crest.data.pojo.Tag;
import hep.crest.swagger.model.TagSummaryDto;

/**
 * @author formica
 *
 */
public class IovGroupsImpl implements IovGroupsCustom {

	private Logger log = LoggerFactory.getLogger(this.getClass());


	private DataSource ds;
	
	private String defaultTablename=null;
	
	public IovGroupsImpl(DataSource ds) {
		super();
		this.ds = ds;
	}

	public void setDefaultTablename(String defaultTablename) {
		if (this.defaultTablename == null)
			this.defaultTablename = defaultTablename;
	}

	protected String tablename() {
		Table ann = Iov.class.getAnnotation(Table.class);
		String tablename = ann.name();
		if (!DatabasePropertyConfigurator.SCHEMA_NAME.isEmpty()) {
			tablename = DatabasePropertyConfigurator.SCHEMA_NAME+"."+tablename;
		} else if (this.defaultTablename != null) {
			tablename = this.defaultTablename + "." + tablename;
		}
		return tablename;
	}

	protected String tagtablename() {
		Table ann = Tag.class.getAnnotation(Table.class);
		String tablename = ann.name();
		if (!DatabasePropertyConfigurator.SCHEMA_NAME.isEmpty()) {
			tablename = DatabasePropertyConfigurator.SCHEMA_NAME+"."+tablename;
		} else if (this.defaultTablename != null) {
			tablename = this.defaultTablename + "." + tablename;
		}
		return tablename;
	}

	/* (non-Javadoc)
	 * @see hep.phycdb.svc.repositories.IovGroupsCustom#selectGroups(java.lang.Long, java.lang.Long)
	 */
	@Override
	public List<BigDecimal> selectGroups(Long tagid, Long groupsize) {
		log.info("Select Iov Groups for tag {} with group size {} using JDBCTEMPLATE",tagid,groupsize);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
		String tablename = this.tablename();
		Long groupfreq = 1000L;
		if (groupsize != null && groupsize > 0) {
			groupfreq = groupsize;
		}
		String sql = "select MIN(SINCE) from "+tablename 
				+" where TAG_ID=? "
				+" group by cast(SINCE/? as int)*?"
				+" order by min(SINCE)";
		
		return jdbcTemplate.queryForList(sql, BigDecimal.class, new Object[] { tagid, groupfreq, groupfreq });
	}


	/* (non-Javadoc)
	 * @see hep.phycdb.svc.repositories.IovGroupsCustom#selectSnapshotGroups(java.lang.Long, java.util.Date, java.lang.Integer)
	 */
	@Override
	public List<BigDecimal> selectSnapshotGroups(Long tagid, Date snap, Long groupsize) {
		log.info("Select Iov Snapshot Groups for tag {} with group size {} and snapshot time {} using JDBCTEMPLATE", tagid, groupsize, snap);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
		String tablename = this.tablename();
		Long groupfreq = 1000L;
		if (groupsize != null && groupsize > 0) {
			groupfreq = groupsize;
		}
		String sql = "select MIN(SINCE) from "+tablename 
				+" where TAG_ID=? and INSERTION_TIME<=?"
				+" group by cast(SINCE/? as int)*?"
				+" order by min(SINCE)";
		
		return jdbcTemplate.queryForList(sql, BigDecimal.class, new Object[] { tagid, snap, groupfreq, groupfreq });
	}


	/* (non-Javadoc)
	 * @see hep.crest.data.repositories.IovGroupsCustom#getSize(java.lang.Long)
	 */
	@Override
	public Long getSize(Long tagid) {
		log.info("Select count(TAG_ID) Iov for tag {} using JDBCTEMPLATE",tagid);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
		String tablename = this.tablename();

		String sql = "select COUNT(TAG_ID) from "+ tablename +" where TAG_ID=?";
		
		return jdbcTemplate.queryForObject(sql, Long.class, new Object[] { tagid } );
	}
	
	/* (non-Javadoc)
	 * @see hep.crest.data.repositories.IovGroupsCustom#getSizeBySnapshot(java.lang.Long, java.util.Date)
	 */
	@Override
	public Long getSizeBySnapshot(Long tagid, Date snap) {
		log.info("Select count(TAG_ID) Iov for tag {} and snapshot time {} using JDBCTEMPLATE",tagid,snap);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
		String tablename = this.tablename();

		String sql = "select COUNT(TAG_ID) from "+ tablename +" where TAG_ID=? and INSERTION_TIME<=?";
		return jdbcTemplate.queryForObject(sql, Long.class, new Object[] { tagid, snap} );
	}


	/* (non-Javadoc)
	 * @see hep.phycdb.svc.repositories.IovGroupsCustom#getTagSummaryInfo(java.lang.String)
	 */
	@Override
	public List<TagSummaryDto> getTagSummaryInfo(String tagname) {
		log.info("Select count(TAG_ID) Iov for tag matching pattern {} using JDBCTEMPLATE",tagname);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
		String tablename = this.tablename();
		String tagtablename = this.tagtablename();
		String sql = "select tags.TAG_NAME, COUNT(iovs.TAG_ID) as NIOVS from "
				+ tablename +" iovs left join "+tagtablename+" tags on tags.tag_id=iovs.tag_id where tags.TAG_NAME like ? GROUP BY tags.TAG_NAME";
		return jdbcTemplate.query(sql, new Object[] { tagname }, (rs, num) -> {
			final TagSummaryDto entity = new TagSummaryDto();
			entity.setTagname(rs.getString("TAG_NAME"));
			entity.setNiovs(rs.getLong("NIOVS"));
			return entity;
		});
	}
	
}
