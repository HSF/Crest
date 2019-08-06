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

import javax.sql.DataSource;

import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.pojo.TagMeta;
import hep.crest.swagger.model.TagMetaDto;

/**
 * @author formica
 *
 */
public class TagMetaPostgresImpl extends TagMetaDBImpl implements TagMetaDataBaseCustom {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	public TagMetaPostgresImpl(DataSource ds) {
		super(ds);
	}

	/**
	 * This method is inspired to the postgres documentation on the JDBC driver. For
	 * reasons which are still not clear the select methods are working as they are.
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
			byte[] buf = new byte[2048];
			int s = 0;
			int tl = 0;
			while ((s = is.read(buf, 0, 2048)) > 0) {
				obj.write(buf, 0, s);
				tl += s;
			}
			// Close the large object
			obj.close();
			return oid;
		} catch (SQLException | IOException e) {
			log.error("Exception in getting large object id: {}", e.getMessage());
		}
		return (Long) null;
	}

	/**
	 * @param entity
	 * @return 
	 * @throws IOException
	 */
	@Override
	protected void saveBlobAsBytes(TagMetaDto entity) throws CdbServiceException {
		String tablename = this.tablename();

		String sql = "INSERT INTO " + tablename
				+ "(TAG_NAME, DESCRIPTION, CHANNEL_SIZE,COLUMN_SIZE, INSERTION_TIME, CHANNEL_INFO, PAYLOAD_INFO) VALUES (?,?,?,?,?,?,?)";

		log.info("Insert TagMeta {} using JDBCTEMPLATE", entity.getTagName());
		log.debug("Channel info {}", entity.getChannelInfo());
		log.debug("Read data blob of length {} and streamer info {}", entity.getChannelInfo().length, entity.getPayloadInfo().length);
		Calendar calendar = Calendar.getInstance();
		java.sql.Date inserttime = new java.sql.Date(calendar.getTime().getTime());
		entity.setInsertionTime(calendar.getTime());
		
		InputStream cis = new ByteArrayInputStream(entity.getChannelInfo());
		InputStream pis = new ByteArrayInputStream(entity.getPayloadInfo());

		try (Connection conn = ds.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);) {
			conn.setAutoCommit(false);
			long cid = getLargeObjectId(conn, cis);
			long pid = getLargeObjectId(conn, pis);

			ps.setString(1, entity.getTagName());
			ps.setString(2, entity.getDescription());
			ps.setInt(3, entity.getChansize());
			ps.setInt(4, entity.getColsize());
			ps.setDate(5, inserttime);
			ps.setLong(6, cid);
			ps.setLong(7, pid);
			log.info("Dump preparedstatement {} using sql {} and args {} {} {}", ps, sql,
					entity.getTagName(), entity.getDescription(), entity.getInsertionTime());
			ps.execute();
			conn.commit();
		} catch (SQLException e) {
			log.error("Exception from SQL during insertion: {}", e.getMessage());
			throw new CdbServiceException(e.getMessage());
		} catch (Exception e) {
			log.error("Exception during tagmeta dto insertion: {}", e.getMessage());
			throw new CdbServiceException("Exception occurred during tagmeta insertion.."+e.getMessage());
		} finally {
			try {
				cis.close();
				pis.close();
			} catch (IOException e) {
				log.error("Error in closing input streams for blobs...");
			}
			log.debug("closed streams...");
		}
	}


	/* (non-Javadoc)
	 * @see hep.crest.data.repositories.TagMetaDBImpl#save(hep.crest.swagger.model.TagMetaDto)
	 */
	@Override
	public TagMeta save(TagMetaDto entity) throws CdbServiceException {
		TagMeta savedentity = null;
		try {
			this.saveBlobAsBytes(entity);
			savedentity = find(entity.getTagName());
		} catch (CdbServiceException e) {
			log.error("Exception in save() : {}", e.getMessage());
		}
		return savedentity;
	}
}
