package hep.crest.data.handlers;

import hep.crest.swagger.model.PayloadDto;
import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Handler for Postgres LOB.
 *
 * @author formica
 */
public class PostgresBlobHandler {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(PostgresBlobHandler.class);

    /**
     * The null long.
     */
    private static final Long LONGNULL = null;

    /**
     * This method is inspired to the postgres documentation on the JDBC driver. For
     * reasons which are still not clear the select methods are working as they are.
     *
     * @param conn   the Connection
     * @param is     the InputStream
     * @param entity the PayloadDto
     * @return long
     */
    public long getLargeObjectId(Connection conn, InputStream is, PayloadDto entity) {
        // Open the large object for writing
        LargeObjectManager lobj = null;
        LargeObject obj = null;
        long oid;
        try {
            lobj = conn.unwrap(org.postgresql.PGConnection.class).getLargeObjectAPI();
            oid = lobj.createLO();
            obj = lobj.open(oid, LargeObjectManager.WRITE);

            // Copy the data from the file to the large object
            final byte[] buf = new byte[2048];
            int s = 0;
            int tl = 0;
            while ((s = is.read(buf, 0, 2048)) > 0) {
                log.trace("Write into LargeObject ID {} nbytes {} ", oid, s);
                obj.write(buf, 0, s);
                tl += s;
            }
            if (entity != null) {
                log.trace("Written size {} ", tl);
                entity.setSize(tl);
            }
            // Close the large object
            obj.close();
            // unlink is used to DELETE the BLOB. Do not unlink here.
            return oid;
        }
        catch (SQLException | IOException e) {
            log.error("Exception in getting large object id: {}", e.getMessage());
        }
        finally {
            try {
                if (obj != null) {
                    obj.close();
                }
                if (lobj != null) {
                    lobj = null;
                }
            }
            catch (final SQLException e) {
                log.error("Error in closing result set : {}", e.getMessage());
            }
        }
        return LONGNULL;
    }

    /**
     * @param oid  the Long
     * @param conn the Connection
     * @return byte[]
     * @throws SQLException If an Exception occurred
     */
    public byte[] getlargeObj(long oid, Connection conn) throws SQLException {
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
            log.error("cannot read large object in postgres {} : {}", oid, e.getMessage());
        }
        finally {
            if (obj != null) {
                obj.close();
                // Not needed here: conn.commit(); it will be done in the calling func.
            }
            // This does not work => lobj unlink (oid) , because it is used for deleting the LOB.
        }
        return buf;
    }
}
