/**
 * 
 */
package hep.crest.data.handlers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.hibernate.engine.jdbc.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Utility class to get binary data and transform them into Blob.
 * It need a connection to a database to operate correctly.
 *
 * @author formica
 *
 */
public class CrestLobHandler {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(CrestLobHandler.class);

    /**
     * Datasource.
     */
    @Autowired
    @Qualifier("dataSource")
    private DataSource ds;

    /**
     * @param ds
     *            the DataSource
     */
    public CrestLobHandler(DataSource ds) {
        super();
        this.ds = ds;
    }

    /**
     * @param ds
     *            the ds to set
     */
    public void setDs(DataSource ds) {
        this.ds = ds;
    }

    /**
     * Wrapper around the createBlobFromStream method to read data and create Blob
     * from a file.
     *
     * @param filelocation
     *            the String
     * @return Blob
     * @throws IOException
     *             If an Exception occurred.
     */
    public Blob createBlobFromFile(String filelocation) throws IOException {
        final File f = new File(filelocation);
        try (FileInputStream fstream = new FileInputStream(f);) {
            return createBlobFromStream(fstream);
        }
        catch (final IOException e) {
            log.error("Cannot find file {}", filelocation);
            throw e;
        }
    }

    /**
     * Create a Blob from an InputStream.
     *
     * @param is
     *            the InputStream
     * @return Blob
     */
    public Blob createBlobFromStream(InputStream is) {
        Blob blob = null;
        BufferedOutputStream bstream = null;
        try (Connection conn = ds.getConnection();
                BufferedInputStream fstream = new BufferedInputStream(is);) {
            // Use the connection to the DB to create a Blob.
            // This allow to use the Lob implementation of the underlying database backend.
            blob = conn.createBlob();
            bstream = new BufferedOutputStream(blob.setBinaryStream(1));
            // stream copy runs a high-speed upload across the network
            StreamUtils.copy(fstream, bstream);
        }
        catch (IOException | SQLException e) {
            log.error("Exception in createBlobFromStream: {}", e.getMessage());
        }
        finally {
            // Close the streams which are not inside the try-with-resources block.
            try {
                if (bstream != null) {
                    bstream.close();
                }
                if (is != null) {
                    is.close();
                }
            }
            catch (final IOException e) {
                log.error("Exception in createBlobFromStream when closing : {}", e.getMessage());
            }
        }
        return blob;
    }

    /**
     * Create a Blob from a byte array.
     *
     * @param data
     *            the byte[]
     * @return Blob
     */
    public Blob createBlobFromByteArr(byte[] data) {
        Blob blob = null;
        BufferedOutputStream bstream = null;
        try (Connection conn = ds.getConnection();
                InputStream is = new ByteArrayInputStream(data);
                BufferedInputStream fstream = new BufferedInputStream(is);) {
            // Use the connection to the DB to create a Blob.
            // This allow to use the Lob implementation of the underlying database backend.
            blob = conn.createBlob();
            bstream = new BufferedOutputStream(blob.setBinaryStream(1));
            // stream copy runs a high-speed upload across the network
            StreamUtils.copy(fstream, bstream);
        }
        catch (final IOException e) {
            log.error("IO Error creating blob from bytes : {}", e.getMessage());
        }
        catch (final SQLException e) {
            log.error("SQL Error creating blob from bytes : {}", e.getMessage());
        }
        finally {
            // Close the streams which are not inside the try-with-resources block.
            try {
                if (bstream != null) {
                    bstream.close();
                }
            }
            catch (final IOException e) {
                log.error("Error closing stream...{}", e.getMessage());
            }
        }
        return blob;
    }
}
