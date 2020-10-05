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

import hep.crest.swagger.model.IovPayloadDto;
import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

/**
 * An implementation for groups queries.
 *
 * @author formica
 *
 */
public class IovGroupsPostgresImpl extends IovGroupsImpl implements IovGroupsCustom {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(IovGroupsPostgresImpl.class);

    /**
     * The decoder.
     */
    private CharsetDecoder decoder = StandardCharsets.US_ASCII.newDecoder();


    /**
     * Default table name.
     */
    private String defaultTablename = null;

    /**
     * @param ds
     *            the DataSource
     */
    public IovGroupsPostgresImpl(DataSource ds) {
        super(ds);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * hep.crest.data.repositories.IovGroupsCustom#getRangeIovPayloadInfo(java.lang
     * .String, java.math.BigDecimal, java.math.BigDecimal, java.util.Date)
     */
    @Override
    public List<IovPayloadDto> getRangeIovPayloadInfo(String name, BigDecimal since,
            BigDecimal until, Date snapshot) {
        log.debug("Select Iov and Payload meta info for tag  {} using standard JDBC for Postgres", name);
        ResultSet rs = null;
        final String tablename = this.tablename();

        // sql : select only metadata from payload table and link with the IOV table
        // select iv.*, pyld.VERSION, pyld.OBJECT_TYPE, pyld.DATA_SIZE from IOV iv
        // left join PAYLOAD pyld ON pyld.HASH=iv.PAYLOAD_HASH
        // where iv.TAG_NAME=? AND iv.SINCE>=COALESCE((SELECT max(iov2.SINCE) FROM
        // IOV iov2 WHERE iov2.TAG_NAME=? AND iov2.SINCE<=? AND iov2.INSERTION_TIME<=? ),0)
        // AND iv.SINCE<=? AND iv.INSERTION_TIME<=?
        // order by iv.SINCE ASC, iv.INSERTION_TIME DESC

        final String sql = "select iv.TAG_NAME, iv.SINCE, iv.INSERTION_TIME, iv.PAYLOAD_HASH, pyld.STREAMER_INFO, "
                + " pyld.VERSION, pyld.OBJECT_TYPE, " + " pyld.DATA_SIZE from " + tablename + " iv "
                + " LEFT JOIN " + payloadTablename() + " pyld " + " ON iv.PAYLOAD_HASH=pyld.HASH "
                + " where iv.TAG_NAME=? AND iv.SINCE>=COALESCE(" + "  (SELECT max(iov2.SINCE) FROM "
                + tablename + " iov2 "
                + "  WHERE iov2.TAG_NAME=? AND iov2.SINCE<=? AND iov2.INSERTION_TIME<=? ),0)"
                + " AND iv.SINCE<=? AND iv.INSERTION_TIME<=? "
                + " ORDER BY iv.SINCE ASC, iv.INSERTION_TIME DESC";
        List<IovPayloadDto> entitylist = new ArrayList<>();
        try (Connection conn = super.getDs().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);) {
            conn.setAutoCommit(false);
            log.debug("Executing query {}", sql);
            ps.setString(1, name);
            ps.setString(2, name);
            ps.setBigDecimal(3, since);
            ps.setTimestamp(4, new java.sql.Timestamp(snapshot.getTime()));
            ps.setBigDecimal(5, until);
            ps.setTimestamp(6, new java.sql.Timestamp(snapshot.getTime()));

            rs = ps.executeQuery();
            byte[] buf = null;
            Long oid = null;
            while (rs.next()) {
                final IovPayloadDto entity = new IovPayloadDto();
                // Open the large object for reading
                log.info("Read resultset...");
                entity.setSince(rs.getBigDecimal("SINCE"));
                entity.setInsertionTime(rs.getTimestamp("INSERTION_TIME"));
                entity.setPayloadHash(rs.getString("PAYLOAD_HASH"));
                entity.setVersion(rs.getString("VERSION"));
                entity.setObjectType(rs.getString("OBJECT_TYPE"));
                entity.setSize(rs.getInt("DATA_SIZE"));
                oid = rs.getLong("STREAMER_INFO");
                buf = this.getlargeObj(oid, conn);
                entity.setStreamerInfo(this.getStringFromBuf(buf));
                log.debug("create entity {}", entity);
                entitylist.add(entity);
            }
            rs.close();
            conn.commit();
        }
        catch (final SQLException e) {
            log.error("SQL exception occurred in retrieving iovpayload data for {}: {}", name, e.getMessage());
        }
        finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            }
            catch (final SQLException e) {
                log.error("Error in closing result set : {}", e);
            }
        }
        return entitylist;
    }

    /**
     * @param oid
     *            the Long
     * @param conn
     *            the Connection
     * @throws SQLException
     *             If an Exception occurred
     * @return byte[]
     */
    protected byte[] getlargeObj(long oid, Connection conn) throws SQLException {
        final LargeObjectManager lobj = conn.unwrap(org.postgresql.PGConnection.class)
                .getLargeObjectAPI();
        LargeObject obj = null;
        byte[] buf = null;
        try {
            obj = lobj.open(oid, LargeObjectManager.READ);
            buf = new byte[obj.size()];
            obj.read(buf, 0, obj.size());
            obj.close();
        }
        catch (final SQLException e) {
            log.error("cannot read large object in postgres {} : {}", oid, e);
        }
        finally {
            if (obj != null) {
                obj.close();
                // conn.commit(); no need to call commit here, it will be done in the calling func.
            }
            //This may not work ? lobj.unlink(oid);
            // Be Careful : unlink could be used to DELETE the BLOB.
        }
        return buf;
    }

    /**
     *
     * @param buf
     * @return the String.
     */
    private String getStringFromBuf(byte[] buf) {
        decoder.onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);

        byte[] streaminfoByteArr = buf;
        try {
            return decoder.decode(ByteBuffer.wrap(streaminfoByteArr))
                    .toString();
        }
        catch (CharacterCodingException e) {
            log.warn("Cannot decode as String with charset US_ASCII, use base64: {}", e);
            return Base64.getEncoder().encodeToString(streaminfoByteArr);
        }
    }
}
