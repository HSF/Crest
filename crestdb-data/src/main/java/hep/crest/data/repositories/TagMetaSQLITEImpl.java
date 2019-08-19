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

import javax.sql.DataSource;
import javax.sql.rowset.serial.SerialBlob;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import hep.crest.data.pojo.TagMeta;

/**
 * @author formica
 *
 */
public class TagMetaSQLITEImpl extends TagMetaDBImpl implements TagMetaDataBaseCustom {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	public TagMetaSQLITEImpl(DataSource ds) {
		super(ds);
	}

	/* (non-Javadoc)
	 * @see hep.crest.data.repositories.TagMetaDataBaseCustom#find(java.lang.String)
	 */
	@Override
	public TagMeta find(String id) {
		log.info("Find tag meta {} using JDBCTEMPLATE", id);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
		String tablename = this.tablename();

		String sql = "select TAG_NAME,DESCRIPTION,CHANNEL_SIZE,COLUMN_SIZE,INSERTION_TIME,CHANNEL_INFO,PAYLOAD_INFO from " + tablename
				+ " where TAG_NAME=?";

		// Be careful, this seems not to work with Postgres: probably getBlob loads an
		// OID and not the byte[]
		// Temporarely, try to create a postgresql implementation of this class.

		return jdbcTemplate.queryForObject(sql, new Object[] { id }, (rs, num) -> {
			final TagMeta entity = new TagMeta();
			entity.setTagName(rs.getString("TAG_NAME"));
			entity.setDescription(rs.getString("DESCRIPTION"));
			entity.setInsertionTime(rs.getDate("INSERTION_TIME"));
			SerialBlob blob = new SerialBlob(rs.getBytes("CHANNEL_INFO"));
			entity.setChannelInfo(blob);
			blob = new SerialBlob(rs.getBytes("PAYLOAD_INFO"));
			entity.setPayloadInfo(blob);
			entity.setChansize(rs.getInt("CHANNEL_SIZE"));
			entity.setColsize(rs.getInt("COLUMN_SIZE"));

			return entity;
		});
	}

}
