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
	private String SERVER_UPLOAD_LOCATION_FOLDER;

	private String default_tablename=null;

	public PayloadDataPostgresImpl(DataSource ds) {
		super();
		this.ds = ds;
	}
	
	public void setDefault_tablename(String default_tablename) {
		if (this.default_tablename == null)
			this.default_tablename = default_tablename;
	}
	
	protected String tablename() {
		Table ann = Payload.class.getAnnotation(Table.class);
		String tablename = ann.name();
		if (!DatabasePropertyConfigurator.SCHEMA_NAME.isEmpty()) {
			tablename = DatabasePropertyConfigurator.SCHEMA_NAME+"."+tablename;
		} else if (this.default_tablename != null) {
			tablename = this.default_tablename + "." + tablename;
		}
		return tablename;
	}


	@Transactional
	public Payload find(String id) {
		log.info("Find payload " + id + " using JDBCTEMPLATE");
		JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
		String tablename = this.tablename();

		String sql = "select HASH,OBJECT_TYPE,VERSION,INSERTION_TIME,DATA,STREAMER_INFO from " + tablename
				+ " where HASH=?";
		
		// Be careful, this seems not to work with Postgres: probably getBlob loads an OID and not the byte[] 
		// Temporarily, try to create a postgresql implementation of this class.
		
		Payload dataentity = jdbcTemplate.queryForObject(sql, new Object[] { id }, (rs, num) -> {
			final Payload entity = new Payload();
			entity.setHash(rs.getString("HASH"));
			entity.setObjectType(rs.getString("OBJECT_TYPE"));
			entity.setVersion(rs.getString("VERSION"));
			entity.setInsertionTime(rs.getDate("INSERTION_TIME"));
			entity.setData(rs.getBlob("DATA"));
			entity.setStreamerInfo(rs.getBlob("STREAMER_INFO"));

			return entity;
		});

		return dataentity;
	}

	@Transactional
	public Payload findMetaInfo(String id) throws Exception {
		log.info("Find payload " + id + " using JDBCTEMPLATE");
		JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
		String tablename = this.tablename();

		String sql = "select HASH,OBJECT_TYPE,VERSION,INSERTION_TIME,STREAMER_INFO from " + tablename
				+ " where HASH=?";
		Payload dataentity = jdbcTemplate.queryForObject(sql, new Object[] { id }, (rs, num) -> {
			final Payload entity = new Payload();
			entity.setHash(rs.getString("HASH"));
			entity.setObjectType(rs.getString("OBJECT_TYPE"));
			entity.setVersion(rs.getString("VERSION"));
			entity.setInsertionTime(rs.getDate("INSERTION_TIME"));
			// entity.setData(rs.getBlob("DATA"));
			entity.setStreamerInfo(rs.getBlob("STREAMER_INFO"));

			return entity;
		});

		return dataentity;
	}

	@Transactional
	public Payload findData(String id) {
		log.info("Find payload data only for " + id + " using JDBCTEMPLATE");
		JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
		String tablename = this.tablename();

		String sql = "select DATA from " + tablename + " where HASH=?";
		Payload dataentity = jdbcTemplate.queryForObject(sql, new Object[] { id }, (rs, num) -> {
			final Payload entity = new Payload();
			entity.setData(rs.getBlob("DATA"));
			return entity;
		});
		return dataentity;
	}

	@Override
	public Payload save(PayloadDto entity) throws CdbServiceException {
		Payload savedentity = null;
		try {
			log.info("Saving blob as bytes array....");
			savedentity = this.saveBlobAsBytes(entity);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return savedentity;
	}
	
	/**
	 * This method is inspired to the postgres documentation on the JDBC driver. For reasons which are still not clear the select
	 * methods are working as they are.
	 * 
	 * @param conn
	 * @param is
	 * @return
	 */
	protected long getLargeObjectId(Connection conn, InputStream is) {
		// Open the large object for writing
		LargeObjectManager lobj;
		long oid;
		try {
			lobj = conn.unwrap(org.postgresql.PGConnection.class).getLargeObjectAPI();
			oid = lobj.createLO();
			LargeObject obj = lobj.open(oid, LargeObjectManager.WRITE);

			// Copy the data from the file to the large object
			byte buf[] = new byte[2048];
			int s = 0; 
			int tl = 0;
			while ((s = is.read(buf, 0, 2048)) > 0) {
				obj.write(buf, 0, s);
			    tl += s;
			}
			// Close the large object
			obj.close();
			return oid;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return (Long) null;
	}

	@Transactional
	protected Payload saveBlobAsBytes(PayloadDto entity) throws IOException {

		String tablename = this.tablename();

		String sql = "INSERT INTO " + tablename
				+ "(HASH, OBJECT_TYPE, VERSION, DATA, STREAMER_INFO, INSERTION_TIME) VALUES (?,?,?,?,?,?)";

		log.info("Insert Payload " + entity.getHash() + " using JDBCTEMPLATE ");
		Connection conn = null;
		Calendar calendar = Calendar.getInstance();
		java.sql.Date inserttime = new java.sql.Date(calendar.getTime().getTime());
		entity.setInsertionTime(calendar.getTime());

		InputStream is = new ByteArrayInputStream(entity.getData());
		InputStream sis = new ByteArrayInputStream(entity.getStreamerInfo());
		
		try {
			conn = ds.getConnection();
			conn.setAutoCommit(false);
			long oid = getLargeObjectId(conn, is);
			long sioid = getLargeObjectId(conn, sis);
						
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, entity.getHash());
			ps.setString(2, entity.getObjectType());
			ps.setString(3, entity.getVersion());
			ps.setLong(4, oid);
			ps.setLong(5, sioid);
			ps.setDate(6, inserttime);
			log.info("Dump preparedstatement " + ps.toString() + " using sql "+sql+" and arguments "+
					entity.getHash()+" "+entity.getObjectType()+" "+entity.getVersion()+" "+entity.getInsertionTime());
			ps.execute();
			ps.close();
			conn.commit();
			log.debug("Search for stored payload as a verification, use hash "+entity.getHash());
			Payload saved = find(entity.getHash());
			return saved;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (is != null) {
					is.close();
				}
				if (sis != null) {
					sis.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	@Transactional
	public Payload save(PayloadDto entity, InputStream is) throws CdbServiceException {
		Payload savedentity = null;
		try {
			// savedentity = this.saveMetaInfo(entity);
			// Blob blob = payloadHandler.createBlobFromStream(is);
			log.info("Saving blob as stream....");
			this.saveBlobAsStream(entity, is);
			savedentity = findMetaInfo(entity.getHash());
		} catch (IOException e) {
			log.error("IOException during payload insertion: "+e.getMessage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("Exception during payload insertion: "+e.getMessage());
		}
		return savedentity;
	}

	protected void saveBlobAsStream(PayloadDto entity, InputStream is) throws IOException {
		String tablename = this.tablename();

		String sql = "INSERT INTO " + tablename
				+ "(HASH, OBJECT_TYPE, VERSION, DATA, STREAMER_INFO, INSERTION_TIME) VALUES (?,?,?,?,?,?)";

		log.info("Insert Payload " + entity.getHash() + " using JDBCTEMPLATE");
		
		log.debug("Streamer info " + entity.getStreamerInfo());
		//log.debug("Read data blob of length " + blob.length + " and streamer info " + entity.getStreamerInfo().length);
		Connection conn = null;
		Calendar calendar = Calendar.getInstance();
		java.sql.Date inserttime = new java.sql.Date(calendar.getTime().getTime());
		entity.setInsertionTime(calendar.getTime());
		InputStream sis = new ByteArrayInputStream(entity.getStreamerInfo());
		try {
			conn = ds.getConnection();
			conn.setAutoCommit(false);
			long oid = getLargeObjectId(conn, is);
			long sioid = getLargeObjectId(conn, sis);
			
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, entity.getHash());
			ps.setString(2, entity.getObjectType());
			ps.setString(3, entity.getVersion());
			ps.setLong(4, oid);
			ps.setLong(5, sioid);
			ps.setDate(6, inserttime);
			log.debug("Dump preparedstatement " + ps.toString());
			ps.executeUpdate();
			ps.close();
			conn.commit();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			log.error("Exception from SQL during insertion: "+e.getMessage());
		} finally {
			try {
				if (is != null) {
					is.close();
				}
				if (sis != null) {
					sis.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				log.error("Exception from SQL attempting to close connection: "+e.getMessage());
			}
		}
		return;
	}

	@Transactional
	protected Payload saveMetaInfo(PayloadDto metainfoentity) throws IOException {

		String tablename = this.tablename();

		String sql = "INSERT INTO " + tablename
				+ "(HASH, OBJECT_TYPE, VERSION, STREAMER_INFO, INSERTION_TIME) VALUES (?,?,?,?,?)";

		log.info("Insert Payload Meta Info " + metainfoentity.getHash() + " using JDBCTEMPLATE");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, metainfoentity.getHash());
			ps.setString(2, metainfoentity.getObjectType());
			ps.setString(3, metainfoentity.getVersion());
			ps.setBytes(4, metainfoentity.getStreamerInfo());
			// FIXME: be careful to the insertion time...is the one provided correct ?
			ps.setDate(5, new java.sql.Date(metainfoentity.getInsertionTime().getTime()));
			log.debug("Dump preparedstatement " + ps.toString());
			ps.execute();
			ps.close();
			Payload saved = find(metainfoentity.getHash());
			return saved;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Transactional
	//FIXME: THIS METHOD is FOR OLD SCHEMA....Should be updated...
	public void delete(String id) {
		String sql = "DELETE FROM PAYLOAD_DATA " + " WHERE HASH=(?)";
		log.info("Remove payload with hash " + id + " using JDBCTEMPLATE");
		JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
		jdbcTemplate.update(sql, new Object[] { id });
		log.debug("Entity removal done...");
	}
}
