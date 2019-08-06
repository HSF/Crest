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

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;

import javax.persistence.Table;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import hep.crest.data.config.DatabasePropertyConfigurator;
import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.pojo.TagMeta;
import hep.crest.swagger.model.TagMetaDto;

/**
 * @author formica
 *
 */
public class TagMetaDBImpl implements TagMetaDataBaseCustom {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	protected DataSource ds;

	private String defaultTablename = null;

	public TagMetaDBImpl(DataSource ds) {
		super();
		this.ds = ds;
	}

	public void setDefaultTablename(String defaultTablename) {
		if (this.defaultTablename == null)
			this.defaultTablename = defaultTablename;
	}

	/**
	 * @return
	 */
	protected String tablename() {
		Table ann = TagMeta.class.getAnnotation(Table.class);
		String tablename = ann.name();
		if (!DatabasePropertyConfigurator.SCHEMA_NAME.isEmpty()) {
			tablename = DatabasePropertyConfigurator.SCHEMA_NAME + "." + tablename;
		} else if (this.defaultTablename != null) {
			tablename = this.defaultTablename + "." + tablename;
		}
		return tablename;
	}

	/**
	 * @param entity
	 * @return
	 * @throws IOException
	 */
	protected void saveBlobAsBytes(TagMetaDto entity) throws CdbServiceException {
		String tablename = this.tablename();

		String sql = "INSERT INTO " + tablename
				+ "(TAG_NAME, DESCRIPTION, CHANNEL_SIZE, COLUMN_SIZE, CHANNEL_INFO, PAYLOAD_INFO, INSERTION_TIME) VALUES (?,?,?,?,?,?,?)";

		log.info("Insert TagMeta {} using JDBCTEMPLATE", entity.getTagName());
		log.debug("Channel info {}", entity.getChannelInfo());
		log.debug("Read data blob of length {} and streamer info {}", entity.getChannelInfo().length,
				entity.getPayloadInfo().length);
		Calendar calendar = Calendar.getInstance();
		java.sql.Date inserttime = new java.sql.Date(calendar.getTime().getTime());
		entity.setInsertionTime(calendar.getTime());
		try (Connection conn = ds.getConnection(); PreparedStatement ps = conn.prepareStatement(sql);) {
			ps.setString(1, entity.getTagName());
			ps.setString(2, entity.getDescription());
			ps.setInt(3, entity.getChansize());
			ps.setInt(4, entity.getColsize());
			ps.setBytes(5, entity.getChannelInfo());
			ps.setBytes(6, entity.getPayloadInfo());
			ps.setDate(7, inserttime);
			log.debug("Dump preparedstatement {}", ps);
			ps.execute();
			conn.commit();
		} catch (SQLException e) {
			log.error("Exception from SQL during insertion: {}", e.getMessage());
			throw new CdbServiceException(e.getMessage());
		} catch (Exception e) {
			log.error("Exception during tagmeta dto insertion: {}", e.getMessage());
			throw new CdbServiceException("Exception occurred during tagmeta insertion.." + e.getMessage());
		} finally {
			log.debug("Nothing to do here ?");
		}
	}

	protected void updateEntityAsBytes(TagMetaDto entity) throws CdbServiceException {
		String tablename = this.tablename();

		String sql = "UPDATE " + tablename
				+ " SET DESCRIPTION=?, CHANNEL_SIZE=?, COLUMN_SIZE=?, CHANNEL_INFO=?, PAYLOAD_INFO=? WHERE TAG_NAME=?";

		log.info("Insert TagMeta {} using JDBCTEMPLATE", entity.getTagName());
		log.debug("Channel info {}", entity.getChannelInfo());
		log.debug("Read data blob of length {} and streamer info {}", entity.getChannelInfo().length,
				entity.getPayloadInfo().length);
		try (Connection conn = ds.getConnection(); PreparedStatement ps = conn.prepareStatement(sql);) {
			ps.setString(1, entity.getDescription());
			ps.setInt(2, entity.getChansize());
			ps.setInt(3, entity.getColsize());
			ps.setBytes(4, entity.getChannelInfo());
			ps.setBytes(5, entity.getPayloadInfo());
			ps.setString(6, entity.getTagName());
			log.debug("Dump preparedstatement {}", ps);
			ps.execute();
			conn.commit();
		} catch (SQLException e) {
			log.error("Exception from SQL during update: {}", e.getMessage());
			throw new CdbServiceException(e.getMessage());
		} catch (Exception e) {
			log.error("Exception during tagmeta dto update: {}", e.getMessage());
			throw new CdbServiceException("Exception occurred during tagmeta update.." + e.getMessage());
		} finally {
			log.debug("Nothing to do here ?");
		}
	}

	@Override
	@Transactional
	public void delete(String id) {
		String tablename = this.tablename();
		String sql = "DELETE FROM " + tablename + " WHERE TAG_NAME=(?)";
		log.info("Remove tag meta with name {} using JDBCTEMPLATE", id);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
		jdbcTemplate.update(sql, new Object[] { id });
		log.debug("Entity removal done...");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hep.crest.data.repositories.TagMetaDataBaseCustom#find(java.lang.String)
	 */
	@Override
	public TagMeta find(String id) {
		log.info("Find tag meta {} using JDBCTEMPLATE", id);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
		String tablename = this.tablename();

		String sql = "select TAG_NAME,DESCRIPTION, CHANNEL_SIZE, COLUMN_SIZE,INSERTION_TIME,CHANNEL_INFO,PAYLOAD_INFO,CHANNEL_SIZE from "
				+ tablename + " where TAG_NAME=?";

		try {
			return jdbcTemplate.queryForObject(sql, new Object[] { id }, (rs, num) -> {
				final TagMeta entity = new TagMeta();
				entity.setTagName(rs.getString("TAG_NAME"));
				entity.setDescription(rs.getString("DESCRIPTION"));
				entity.setChansize(rs.getInt("CHANNEL_SIZE"));
				entity.setColsize(rs.getInt("COLUMN_SIZE"));
				entity.setInsertionTime(rs.getDate("INSERTION_TIME"));
				entity.setChannelInfo(rs.getBlob("CHANNEL_INFO"));
				entity.setPayloadInfo(rs.getBlob("PAYLOAD_INFO"));

				return entity;
			});
		} catch (Exception e) {
			log.error("Error in finding tag meta info for {}", id);
			return null;
		}

	}

	@Override
	public TagMeta findMetaInfo(String id) {
		log.info("Find tag meta {} using JDBCTEMPLATE", id);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
		String tablename = this.tablename();

		String sql = "select TAG_NAME,DESCRIPTION,INSERTION_TIME,CHANNEL_SIZE,COLUMN_SIZE from " + tablename
				+ " where TAG_NAME=?";

		// Be careful, this seems not to work with Postgres: probably getBlob loads an
		// OID and not the byte[]
		// Temporarely, try to create a postgresql implementation of this class.
		try {
			return jdbcTemplate.queryForObject(sql, new Object[] { id }, (rs, num) -> {
				final TagMeta entity = new TagMeta();
				entity.setTagName(rs.getString("TAG_NAME"));
				entity.setDescription(rs.getString("DESCRIPTION"));
				entity.setChansize(rs.getInt("CHANNEL_SIZE"));
				entity.setColsize(rs.getInt("COLUMN_SIZE"));
				entity.setInsertionTime(rs.getDate("INSERTION_TIME"));

				return entity;
			});
		} catch (Exception e) {
			log.error("Error in finding tag meta info for {}", id);
			return null;
		}
	}

	@Override
	public TagMeta save(TagMetaDto entity) throws CdbServiceException {
		TagMeta savedentity = null;
		try {
			log.info("Store tag meta dto {}", entity);
			this.saveBlobAsBytes(entity);
			log.info("Stored dto for tag meta...");
			savedentity = find(entity.getTagName());
		} catch (CdbServiceException e) {
			log.error("Exception in tag meta save() : {}", e.getMessage());
		}
		return savedentity;
	}

	/* (non-Javadoc)
	 * @see hep.crest.data.repositories.TagMetaDataBaseCustom#update(hep.crest.swagger.model.TagMetaDto)
	 */
	@Override
	public TagMeta update(TagMetaDto entity) throws CdbServiceException {
		TagMeta savedentity = null;
		try {
			log.info("Update tag meta dto {}", entity);
			this.updateEntityAsBytes(entity);
			log.info("Updated dto for tag meta...");
			savedentity = find(entity.getTagName());
		} catch (CdbServiceException e) {
			log.error("Exception in tag meta save() : {}", e.getMessage());
		}
		return savedentity;
	}
	
	
}
