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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

import javax.persistence.Table;
import javax.sql.DataSource;

import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import hep.crest.data.config.DatabasePropertyConfigurator;
import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.exceptions.PayloadEncodingException;
import hep.crest.data.pojo.Payload;
import hep.crest.swagger.model.PayloadDto;

/**
 * @author formica
 *
 */
public class PayloadDataPostgresImpl implements PayloadDataBaseCustom {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private DataSource ds;

	@Value("${crest.upload.dir:/tmp}")
	private String serverUploadLocationFolder;

	private String defaultTablename = null;

	public PayloadDataPostgresImpl(DataSource ds) {
		super();
		this.ds = ds;
	}

	public void setDefaultTablename(String defaultTablename) {
		if (this.defaultTablename == null)
			this.defaultTablename = defaultTablename;
	}

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

	/* (non-Javadoc)
	 * @see hep.crest.data.repositories.PayloadDataBaseCustom#find(java.lang.String)
	 */
	public Payload find(String id) {
		log.info("Find payload {} using JDBCTEMPLATE", id);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
		String tablename = this.tablename();

		String sql = "select HASH,OBJECT_TYPE,VERSION,INSERTION_TIME,DATA,STREAMER_INFO,DATA_SIZE from " + tablename
				+ " where HASH=?";

		// Be careful, this seems not to work with Postgres: probably getBlob loads an
		// OID and not the byte[]
		// Temporarily, try to create a postgresql implementation of this class.

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

	/* (non-Javadoc)
	 * @see hep.crest.data.repositories.PayloadDataBaseCustom#findMetaInfo(java.lang.String)
	 */
	@Transactional
	public Payload findMetaInfo(String id) {
		log.info("Find payload meta data only for {} using JDBCTEMPLATE", id);
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

	}

	/* (non-Javadoc)
	 * @see hep.crest.data.repositories.PayloadDataBaseCustom#findData(java.lang.String)
	 */
	public Payload findData(String id) {
		log.info("Find payload data only for {} using JDBCTEMPLATE", id);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
		String tablename = this.tablename();

		String sql = "select HASH,DATA from " + tablename + " where HASH=?";
		return jdbcTemplate.queryForObject(sql, new Object[] { id }, (rs, num) -> {
			final Payload entity = new Payload();
			entity.setHash(rs.getString("HASH"));
			entity.setData(rs.getBlob("DATA"));
			return entity;
		});
	}

	/**
	 * @param id
	 * @return
	 */
	protected LargeObject readBlobAsStream(String id) {
		String tablename = this.tablename();

		String sql = "select DATA from " + tablename + " where HASH=?";

		log.info("Read Payload data with hash {} using JDBCTEMPLATE", id);
		ResultSet rs = null;
		LargeObject obj = null;
		try (Connection conn = ds.getConnection(); PreparedStatement ps = conn.prepareStatement(sql);) {
			conn.setAutoCommit(false);
			LargeObjectManager lobj = conn.unwrap(org.postgresql.PGConnection.class).getLargeObjectAPI();
			ps.setString(1, id);
			rs = ps.executeQuery();
			while (rs.next()) {
				// Open the large object for reading
				long oid = rs.getLong(1);
				obj = lobj.open(oid, LargeObjectManager.READ);
			}
		} catch (SQLException e) {
			log.error("SQL exception occurred in retrieving payload data for {}", id);
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (obj != null) 
					obj.close();
			} catch (SQLException | NullPointerException e) {
				log.error("Error in closing result set : {}",e.getMessage());
			}
		}
		return obj;
	}

	@Override
	public Payload save(PayloadDto entity) throws CdbServiceException {
		Payload savedentity = null;
		try {
			log.info("Saving blob as bytes array....");
			savedentity = this.saveBlobAsBytes(entity);
		} catch (IOException e) {
			log.error("Exception in saving payload dto: {}", e.getMessage());
		}
		return savedentity;
	}

	/**
	 * This method is inspired to the postgres documentation on the JDBC driver. For
	 * reasons which are still not clear the select methods are working as they are.
	 * 
	 * @param conn
	 * @param is
	 * @return
	 */
	protected long getLargeObjectId(Connection conn, InputStream is, PayloadDto entity) {
		// Open the large object for writing
		LargeObjectManager lobj;
		LargeObject obj = null;
		long oid;
		try {
			lobj = conn.unwrap(org.postgresql.PGConnection.class).getLargeObjectAPI();
			oid = lobj.createLO();
			obj = lobj.open(oid, LargeObjectManager.WRITE);

			// Copy the data from the file to the large object
			byte[] buf = new byte[2048];
			int s = 0;
			int tl = 0;
			while ((s = is.read(buf, 0, 2048)) > 0) {
				obj.write(buf, 0, s);
				tl += s;
			}
			if (entity != null) {
				entity.setSize(tl);
			}
			// Close the large object
			obj.close();
			return oid;
		} catch (SQLException | IOException e) {
			log.error("Exception in getting large object id: {}", e.getMessage());
		} finally {
			if (obj!= null) {
				try {
					obj.close();
				} catch (SQLException e) {
					log.error("Error in closing LargeObject");
				}
			}
		}
		return (Long) null;
	}

	protected Payload saveBlobAsBytes(PayloadDto entity) throws IOException {

		String tablename = this.tablename();

		String sql = "INSERT INTO " + tablename
				+ "(HASH, OBJECT_TYPE, VERSION, DATA, STREAMER_INFO, INSERTION_TIME, DATA_SIZE) VALUES (?,?,?,?,?,?,?)";

		log.info("Insert Payload {} using JDBCTEMPLATE ", entity.getHash());
		Calendar calendar = Calendar.getInstance();
		java.sql.Date inserttime = new java.sql.Date(calendar.getTime().getTime());
		entity.setInsertionTime(calendar.getTime());

		InputStream is = new ByteArrayInputStream(entity.getData());
		InputStream sis = new ByteArrayInputStream(entity.getStreamerInfo());

		try (Connection conn = ds.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);) {
			conn.setAutoCommit(false);
			long oid = getLargeObjectId(conn, is, entity);
			long sioid = getLargeObjectId(conn, sis, null);

			ps.setString(1, entity.getHash());
			ps.setString(2, entity.getObjectType());
			ps.setString(3, entity.getVersion());
			ps.setLong(4, oid);
			ps.setLong(5, sioid);
			ps.setDate(6, inserttime);
			ps.setInt(7, entity.getSize());
			log.info("Dump preparedstatement {} using sql {} and args {} {} {} {}", ps, sql,
					entity.getHash(), entity.getObjectType(), entity.getVersion(), entity.getInsertionTime());
			ps.execute();
			//conn.commit();
			log.debug("Search for stored payload as a verification, use hash {}", entity.getHash());
			return find(entity.getHash());
		} catch (SQLException e) {
			log.error("Exception : {}", e.getMessage());
		} finally {
			is.close();
			sis.close();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see hep.crest.data.repositories.PayloadDataBaseCustom#save(hep.crest.swagger.model.PayloadDto, java.io.InputStream)
	 */
	@Override
	@Transactional
	public Payload save(PayloadDto entity, InputStream is) throws CdbServiceException {
		Payload savedentity = null;
		try {
			log.info("Saving blob as stream....");
			this.saveBlobAsStream(entity, is);
			savedentity = findMetaInfo(entity.getHash());
		} catch (IOException e) {
			log.error("IOException during payload insertion: {}", e.getMessage());
		} catch (Exception e) {
			log.error("Exception during payload insertion: {}", e.getMessage());
		}
		return savedentity;
	}

	/**
	 * @param entity
	 * @param is
	 * @throws IOException
	 */
	protected void saveBlobAsStream(PayloadDto entity, InputStream is) throws IOException {
		String tablename = this.tablename();

		String sql = "INSERT INTO " + tablename
				+ "(HASH, OBJECT_TYPE, VERSION, DATA, STREAMER_INFO, INSERTION_TIME, DATA_SIZE) VALUES (?,?,?,?,?,?,?)";

		log.info("Insert Payload {} using JDBCTEMPLATE", entity.getHash());

		log.debug("Streamer info {} ", entity.getStreamerInfo());

		Calendar calendar = Calendar.getInstance();
		java.sql.Date inserttime = new java.sql.Date(calendar.getTime().getTime());
		entity.setInsertionTime(calendar.getTime());

		InputStream sis = new ByteArrayInputStream(entity.getStreamerInfo());
		try (Connection conn = ds.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);) {
			conn.setAutoCommit(false);
			long oid = getLargeObjectId(conn, is, entity);
			long sioid = getLargeObjectId(conn, sis, null);

			ps.setString(1, entity.getHash());
			ps.setString(2, entity.getObjectType());
			ps.setString(3, entity.getVersion());
			ps.setLong(4, oid);
			ps.setLong(5, sioid);
			ps.setDate(6, inserttime);
			ps.setInt(7, entity.getSize());
			log.debug("Dump preparedstatement {} ", ps);
			ps.executeUpdate();
			//conn.commit();
		} catch (SQLException e) {
			log.error("Exception from SQL during insertion: {}", e.getMessage());
		} finally {
			is.close();
			sis.close();
		}
	}

	/**
	 * @param metainfoentity
	 * @return
	 */
	protected Payload saveMetaInfo(PayloadDto metainfoentity) {

		String tablename = this.tablename();

		String sql = "INSERT INTO " + tablename
				+ "(HASH, OBJECT_TYPE, VERSION, STREAMER_INFO, INSERTION_TIME,DATA_SIZE) VALUES (?,?,?,?,?,?)";

		log.info("Insert Payload Meta Info {} using JDBCTEMPLATE", metainfoentity.getHash());
		try (Connection conn = ds.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);) {
			ps.setString(1, metainfoentity.getHash());
			ps.setString(2, metainfoentity.getObjectType());
			ps.setString(3, metainfoentity.getVersion());
			ps.setBytes(4, metainfoentity.getStreamerInfo());
			// FIXME: be careful to the insertion time...is the one provided correct ?
			ps.setDate(5, new java.sql.Date(metainfoentity.getInsertionTime().getTime()));
			ps.setInt(6, metainfoentity.getSize());
			log.debug("Dump preparedstatement {}", ps);
			ps.execute();
			return find(metainfoentity.getHash());
		} catch (SQLException e) {
			log.error("Sql Exception when saving meta info : {}",e.getMessage());
		} 
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hep.phycdb.svc.repositories.PayloadDataBaseCustom#saveNull()
	 */
	@Override
	public Payload saveNull() throws IOException, PayloadEncodingException {
		return null;
	}

	// FIXME: THIS METHOD is FOR OLD SCHEMA....Should be updated...
	@Override
	@Transactional
	public void delete(String id) {
		String tablename = this.tablename();
		String sql = "DELETE FROM "+ tablename + " WHERE HASH=(?)";
		log.info("Remove payload with hash {} using JDBCTEMPLATE", id);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
		jdbcTemplate.update(sql, new Object[] { id });
		log.debug("Entity removal done...");
	}
}
