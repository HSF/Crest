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
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;

import javax.persistence.Table;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import hep.crest.data.config.DatabasePropertyConfigurator;
import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.exceptions.PayloadEncodingException;
import hep.crest.data.handlers.PayloadHandler;
import hep.crest.data.pojo.Payload;
import hep.crest.swagger.model.PayloadDto;

/**
 * @author formica
 *
 */
public class PayloadDataDBImpl implements PayloadDataBaseCustom {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private DataSource ds;

	@Value("${physconddb.upload.dir:/tmp}")
	private String serverUploadLocationFolder;

	@Autowired
	private PayloadHandler payloadHandler;

	private String defaultTablename = null;

	public PayloadDataDBImpl(DataSource ds) {
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
		Table ann = Payload.class.getAnnotation(Table.class);
		String tablename = ann.name();
		if (!DatabasePropertyConfigurator.SCHEMA_NAME.isEmpty()) {
			tablename = DatabasePropertyConfigurator.SCHEMA_NAME + "." + tablename;
		} else if (this.defaultTablename != null) {
			tablename = this.defaultTablename + "." + tablename;
		}
		return tablename;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hep.crest.data.repositories.PayloadDataBaseCustom#find(java.lang.String)
	 */
	@Transactional
	public Payload find(String id) {
		log.info("Find payload {} using JDBCTEMPLATE", id);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
		String tablename = this.tablename();

		String sql = "select HASH,OBJECT_TYPE,VERSION,INSERTION_TIME,DATA,STREAMER_INFO,DATA_SIZE from " + tablename
				+ " where HASH=?";

		// Be careful, this seems not to work with Postgres: probably getBlob loads an
		// OID and not the byte[]
		// Temporarely, try to create a postgresql implementation of this class.

		return jdbcTemplate.queryForObject(sql, new Object[] { id }, (rs, num) -> {
			final Payload entity = new Payload();
			entity.setHash(rs.getString("HASH"));
			entity.setObjectType(rs.getString("OBJECT_TYPE"));
			entity.setVersion(rs.getString("VERSION"));
			entity.setInsertionTime(rs.getDate("INSERTION_TIME"));
			entity.setData(rs.getBlob("DATA"));
			entity.setStreamerInfo(rs.getBlob("STREAMER_INFO"));
			entity.setSize(rs.getInt("DATA_SIZE"));

			return entity;
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * hep.crest.data.repositories.PayloadDataBaseCustom#findMetaInfo(java.lang.
	 * String)
	 */
	@Transactional
	public Payload findMetaInfo(String id) {
		log.info("Find payload meta info {} using JDBCTEMPLATE", id);
		try {
			JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
			String tablename = this.tablename();

			String sql = "select HASH,OBJECT_TYPE,VERSION,INSERTION_TIME,STREAMER_INFO,DATA_SIZE from " + tablename
					+ " where HASH=?";

			return jdbcTemplate.queryForObject(sql, new Object[] { id }, (rs, num) -> {
				final Payload entity = new Payload();
				entity.setHash(rs.getString("HASH"));
				entity.setObjectType(rs.getString("OBJECT_TYPE"));
				entity.setVersion(rs.getString("VERSION"));
				entity.setInsertionTime(rs.getDate("INSERTION_TIME"));
				entity.setStreamerInfo(rs.getBlob("STREAMER_INFO"));
				entity.setSize(rs.getInt("DATA_SIZE"));

				return entity;
			});
		} catch (Exception e) {
			log.error("Cannot find payload metadata with hash {}", id);
			return null;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * hep.crest.data.repositories.PayloadDataBaseCustom#findData(java.lang.String)
	 */
	@Transactional
	public Payload findData(String id) {
		log.info("Find payload data {} using JDBCTEMPLATE", id);
		try {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
		String tablename = this.tablename();

		String sql = "select DATA from " + tablename + " where HASH=?";
		return jdbcTemplate.queryForObject(sql, new Object[] { id }, (rs, num) -> {
			final Payload entity = new Payload();
			entity.setData(rs.getBlob("DATA"));
			return entity;
		});
		} catch (Exception e) {
			log.error("Cannot find payload with data for hash {}",id);
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * hep.crest.data.repositories.PayloadDataBaseCustom#save(hep.crest.swagger.
	 * model.PayloadDto)
	 */
	@Override
	@Transactional
	public Payload save(PayloadDto entity) throws CdbServiceException {
		Payload savedentity = null;
		try {
			savedentity = this.saveBlobAsBytes(entity);
		} catch (CdbServiceException e) {
			log.error("Exception in save() : {}", e.getMessage());
		}
		return savedentity;
	}

	protected Payload saveBlobAsBytes(PayloadDto entity) throws CdbServiceException {

		String tablename = this.tablename();

		String sql = "INSERT INTO " + tablename
				+ "(HASH, OBJECT_TYPE, VERSION, DATA, STREAMER_INFO, INSERTION_TIME,DATA_SIZE) VALUES (?,?,?,?,?,?,?)";

		log.info("Insert Payload {} using JDBCTEMPLATE ", entity.getHash());
		Calendar calendar = Calendar.getInstance();
		java.sql.Date inserttime = new java.sql.Date(calendar.getTime().getTime());
		entity.setInsertionTime(calendar.getTime());
		try (Connection conn = ds.getConnection(); PreparedStatement ps = conn.prepareStatement(sql);) {
			ps.setString(1, entity.getHash());
			ps.setString(2, entity.getObjectType());
			ps.setString(3, entity.getVersion());
			ps.setBytes(4, entity.getData());
			ps.setBytes(5, entity.getStreamerInfo());
			ps.setDate(6, inserttime);
			ps.setInt(7, entity.getSize());
			log.info("Dump preparedstatement {} using sql {} and arguments : {} {} {} {}", ps, sql, entity.getHash(),
					entity.getObjectType(), entity.getVersion(), entity.getInsertionTime());
			ps.execute();
			log.debug("Search for stored payload as a verification, use hash {} ", entity.getHash());
			return find(entity.getHash());
		} catch (SQLException e) {
			log.error("SQL Exception when saving blob as bytes: {} ", e.getMessage());
			throw new CdbServiceException("SQL error "+e.getMessage());
		} catch (Exception e) {
			log.error("Generic Exception when savinf payload as bytes: {} ", e.getMessage());
			throw new CdbServiceException("Error "+e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * hep.crest.data.repositories.PayloadDataBaseCustom#save(hep.crest.swagger.
	 * model.PayloadDto, java.io.InputStream)
	 */
	@Override
	@Transactional
	public Payload save(PayloadDto entity, InputStream is) throws CdbServiceException {
		Payload savedentity = null;
		try {
			if ((savedentity = findMetaInfo(entity.getHash())) != null) {
				log.warn("The hash {} already exists...return the existing entity...", entity.getHash());
				return savedentity;
			}
			this.saveBlobAsStream(entity, is);
			savedentity = findMetaInfo(entity.getHash());
		} catch (CdbServiceException e) {
			log.error("Exception during payload dto insertion: {}", e.getMessage());
		}
		return savedentity;
	}

	/**
	 * @param entity
	 * @param is
	 * @throws IOException
	 */
	protected void saveBlobAsStream(PayloadDto entity, InputStream is) throws CdbServiceException {
		String tablename = this.tablename();

		String sql = "INSERT INTO " + tablename
				+ "(HASH, OBJECT_TYPE, VERSION, DATA, STREAMER_INFO, INSERTION_TIME, DATA_SIZE) VALUES (?,?,?,?,?,?,?)";

		log.info("Insert Payload {} using JDBCTEMPLATE", entity.getHash());
		byte[] blob = payloadHandler.getBytesFromInputStream(is);
		entity.setSize(blob.length);
		log.debug("Streamer info {}", entity.getStreamerInfo());
		log.debug("Read data blob of length {} and streamer info {}", blob.length, entity.getStreamerInfo().length);
		Calendar calendar = Calendar.getInstance();
		java.sql.Date inserttime = new java.sql.Date(calendar.getTime().getTime());
		entity.setInsertionTime(calendar.getTime());
		try (Connection conn = ds.getConnection(); PreparedStatement ps = conn.prepareStatement(sql);) {
			ps.setString(1, entity.getHash());
			ps.setString(2, entity.getObjectType());
			ps.setString(3, entity.getVersion());
			ps.setBytes(4, blob);
			ps.setBytes(5, entity.getStreamerInfo());
			ps.setDate(6, inserttime);
			ps.setInt(7, entity.getSize());
			log.debug("Dump preparedstatement {}", ps);
			ps.execute();
			conn.commit();
		} catch (SQLException e) {
			log.error("Exception from SQL during insertion: {}", e.getMessage());
			throw new CdbServiceException(e.getMessage());
		} catch (Exception e) {
			log.error("Exception during payload dto insertion: {}", e.getMessage());
			throw new CdbServiceException("Exception occurred during payload insertion from stream.."+e.getMessage());
		} finally {
			log.debug("Nothing to do here ?");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hep.phycdb.svc.repositories.PayloadDataBaseCustom#saveNull()
	 */
	@Override
	public Payload saveNull() throws IOException, PayloadEncodingException {
		log.warn("Method not implemented");
		return null;
	}

	// FIXME: THIS METHOD is FOR OLD SCHEMA....Should be updated...
	@Override
	@Transactional
	public void delete(String id) {
		String tablename = this.tablename();
		String sql = "DELETE FROM " + tablename + " WHERE HASH=(?)";
		log.info("Remove payload with hash {} using JDBCTEMPLATE", id);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
		jdbcTemplate.update(sql, new Object[] { id });
		log.debug("Entity removal done...");
	}
}
