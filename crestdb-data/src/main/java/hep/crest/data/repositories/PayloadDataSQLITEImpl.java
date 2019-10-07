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
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.persistence.Table;
import javax.sql.DataSource;
import javax.sql.rowset.serial.SerialBlob;

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
public class PayloadDataSQLITEImpl implements PayloadDataBaseCustom {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private DataSource ds;

	@Value("${physconddb.upload.dir:/tmp}")
	private String serverUploadLocationFolder;

	@Autowired
	private PayloadHandler payloadHandler;

	private String defaultTablename=null;

	public PayloadDataSQLITEImpl(DataSource ds) {
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
			tablename = DatabasePropertyConfigurator.SCHEMA_NAME+"."+tablename;
		} else if (this.defaultTablename != null) {
			tablename = this.defaultTablename + "." + tablename;
		}
		return tablename;
	}

	/* (non-Javadoc)
	 * @see hep.crest.data.repositories.PayloadDataBaseCustom#find(java.lang.String)
	 */
	@Transactional
	public Payload find(String id) {
		log.info("Find payload {} using JDBCTEMPLATE",id);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
		String tablename = this.tablename();

		String sql = "select HASH,OBJECT_TYPE,VERSION,INSERTION_TIME,DATA,STREAMER_INFO,DATA_SIZEIZE from "+tablename+" where PAYLOAD.HASH=?";
		return jdbcTemplate.queryForObject(sql, new Object[] { id }, (rs, num) -> {
			final Payload entity = new Payload();
			entity.setHash(rs.getString("HASH"));
			entity.setObjectType(rs.getString("OBJECT_TYPE"));
			entity.setVersion(rs.getString("VERSION"));
			entity.setInsertionTime(rs.getDate("INSERTION_TIME"));
			SerialBlob blob = new SerialBlob(rs.getBytes("DATA"));
			entity.setData(blob);
			blob = new SerialBlob(rs.getBytes("STREAMER_INFO"));
			entity.setStreamerInfo(blob);
			entity.setSize(rs.getInt("DATA_SIZE"));

			return entity;
		});
		
	}

	/* (non-Javadoc)
	 * @see hep.phycdb.svc.repositories.PayloadDataBaseCustom#save(hep.phycdb.data.pojo.Payload)
	 */
	@Override
	public Payload save(PayloadDto entity) throws CdbServiceException {
		Payload savedentity = null;
		try {
			savedentity = this.saveBlobAsBytes(entity);
		} catch (IOException e) {
			log.error("Error in save paylod dto : {}",e.getMessage());
		}
		return savedentity;
	}
	
	/* (non-Javadoc)
	 * @see hep.crest.data.repositories.PayloadDataBaseCustom#save(hep.crest.swagger.model.PayloadDto, java.io.InputStream)
	 */
	@Override
	@Transactional
	public Payload save(PayloadDto entity, InputStream is) throws CdbServiceException {
		Payload savedentity = null;
		try {
			this.saveBlobAsStream(entity,is);
			savedentity = findMetaInfo(entity.getHash());
		} catch (IOException e) {
			log.error("Error in save paylod dto : {}",e.getMessage());
		}
		return savedentity;
	}
	


	/* (non-Javadoc)
	 * @see hep.phycdb.svc.repositories.PayloadDataBaseCustom#saveNull()
	 */
	@Override
	public Payload saveNull() throws IOException, PayloadEncodingException {
		return null;
	}

	protected Payload saveBlobAsBytes(PayloadDto entity) throws IOException {
		
		String tablename = this.tablename();

		String sql = "INSERT INTO "+tablename+ "(HASH, OBJECT_TYPE, VERSION, DATA, STREAMER_INFO, INSERTION_TIME, DATA_SIZE) VALUES (?,?,?,?,?,?,?)";

		log.info("Insert Payload {} using JDBCTEMPLATE",entity.getHash());
		
		try (PreparedStatement ps = ds.getConnection().prepareStatement(sql);) {
            ps.setString(1,entity.getHash());  
            ps.setString(2,entity.getObjectType());  
            ps.setString(3,entity.getVersion());  
            ps.setBytes(4, entity.getData());  
            ps.setBytes(5, entity.getStreamerInfo());
            ps.setDate(6, new java.sql.Date(entity.getInsertionTime().getTime()));
            ps.setInt(7, entity.getSize());

            log.info("Dump preparedstatement {}",ps);
            ps.execute();
            return find(entity.getHash());
		} catch (SQLException e) {
			log.error("Sql exception in saving bytes for payload : {} ",e.getMessage());
		}
		return null;
	}

	/**
	 * @param entity
	 * @param is
	 * @throws IOException
	 */
	protected void saveBlobAsStream(PayloadDto entity, InputStream is) throws IOException {
		String tablename = this.tablename();

		String sql = "INSERT INTO "+tablename+ "(HASH, OBJECT_TYPE, VERSION, DATA, STREAMER_INFO, INSERTION_TIME, DATA_SIZE) VALUES (?,?,?,?,?,?,?)";

		log.info("Insert Payload {} using JDBCTEMPLATE",entity.getHash());
		byte[] blob = payloadHandler.getBytesFromInputStream(is);
		if (blob != null) {
			entity.setSize(blob.length);
			log.debug("Read data blob of length {} and streamer info {}",blob.length,entity.getStreamerInfo().length);
		}
		try (PreparedStatement ps = ds.getConnection().prepareStatement(sql);){
			
            ps.setString(1,entity.getHash());  
            ps.setString(2,entity.getObjectType());  
            ps.setString(3,entity.getVersion());  
            ps.setBytes(4, blob);  
            ps.setBytes(5, entity.getStreamerInfo());
            ps.setDate(6, new java.sql.Date(entity.getInsertionTime().getTime()));
            ps.setInt(7, entity.getSize());
            log.debug("Dump preparedstatement {}",ps);
            ps.execute();
		} catch (SQLException e) {
			log.error("Sql exception when storing payload {}",e.getMessage());
		}
	}


	/* (non-Javadoc)
	 * @see hep.crest.data.repositories.PayloadDataBaseCustom#delete(java.lang.String)
	 */
	@Override
	@Transactional
	public void delete(String id) {
		String tablename = this.tablename();

		String sql = "DELETE FROM  "+tablename+ " WHERE HASH=(?)";
		log.info("Remove payload with hash {} using JDBCTEMPLATE",id);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
		jdbcTemplate.update(sql, new Object[] { id });
		log.info("Entity removal done...");
	}

	@Transactional
	public Payload findMetaInfo(String id) {
		log.info("Find payload {} using JDBCTEMPLATE",id);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
		String tablename = this.tablename();

		String sql = "select HASH,OBJECT_TYPE,VERSION,INSERTION_TIME,STREAMER_INFO,DATA_SIZE from "+tablename+" where PAYLOAD.HASH=?";
		Payload dataentity = jdbcTemplate.queryForObject(sql, new Object[] { id }, (rs, num) -> {
			final Payload entity = new Payload();
			entity.setHash(rs.getString("HASH"));
			entity.setObjectType(rs.getString("OBJECT_TYPE"));
			entity.setVersion(rs.getString("VERSION"));
			entity.setInsertionTime(rs.getDate("INSERTION_TIME"));
			SerialBlob blob = new SerialBlob(rs.getBytes("STREAMER_INFO"));
			entity.setStreamerInfo(blob);
			entity.setSize(rs.getInt("DATA_SIZE"));
			return entity;
		});

		return dataentity;
	}
	
	@Override
	public Payload findData(String id) {
		log.info("Find payload data only for {} using JDBCTEMPLATE",id);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
		String tablename = this.tablename();

		String sql = "select DATA from "+tablename+" where PAYLOAD.HASH=?";
		Payload dataentity = jdbcTemplate.queryForObject(sql, new Object[] { id }, (rs, num) -> {
			final Payload entity = new Payload();
			SerialBlob blob = new SerialBlob(rs.getBytes("DATA"));
			entity.setData(blob);
			return entity;
		});
		return dataentity;
	}

}
