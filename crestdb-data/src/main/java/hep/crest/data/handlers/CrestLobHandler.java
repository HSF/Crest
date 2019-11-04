/**
 * 
 */
package hep.crest.data.handlers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

import hep.crest.data.pojo.Payload;
import hep.crest.swagger.model.PayloadDto;

/**
 * @author formica
 *
 */
public class CrestLobHandler {

    /**
     * Logger.
     */
    private final Logger log = LoggerFactory.getLogger(this.getClass());

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
     * @param ds the ds to set
     */
    public void setDs(DataSource ds) {
        this.ds = ds;
    }

    /**
     * @param filelocation
     *            the String
     * @return Blob
     */
    public Blob createBlobFromFile(String filelocation) {
        Blob blob = null;
        BufferedOutputStream bstream = null;
        final File f = new File(filelocation);
        try (Connection conn = ds.getConnection();
                BufferedInputStream fstream = new BufferedInputStream(new FileInputStream(f));) {

            blob = conn.createBlob();
            bstream = new BufferedOutputStream(blob.setBinaryStream(1));
            // stream copy runs a high-speed upload across the network
            StreamUtils.copy(fstream, bstream);
            return blob;
        }
        catch (IOException | SQLException e) {
            log.error("Exception in createBlobFromFile: {}", e.getMessage());
        }
        finally {
            try {
                if (bstream != null) {
                    bstream.close();
                }
            }
            catch (final IOException e) {
                log.error("Exception in createBlobFromFile when closing : {}", e.getMessage());
            }
        }
        return blob;
    }

    /**
     * @param is
     *            the InputStream
     * @return Blob
     */
    public Blob createBlobFromStream(InputStream is) {
        Blob blob = null;
        BufferedOutputStream bstream = null;
        try (Connection conn = ds.getConnection();
                BufferedInputStream fstream = new BufferedInputStream(is);) {
            blob = conn.createBlob();
            bstream = new BufferedOutputStream(blob.setBinaryStream(1));
            // stream copy runs a high-speed upload across the network
            StreamUtils.copy(fstream, bstream);
            return blob;
        }
        catch (IOException | SQLException e) {
            log.error("Exception in createBlobFromStream: {}", e.getMessage());
        }
        finally {
            try {
                if (bstream != null) {
                    bstream.close();
                }
            }
            catch (final IOException e) {
                log.error("Exception in createBlobFromStream when closing : {}", e.getMessage());
            }
        }
        return blob;
    }

    /**
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
            blob = conn.createBlob();
            bstream = new BufferedOutputStream(blob.setBinaryStream(1));
            // stream copy runs a high-speed upload across the network
            StreamUtils.copy(fstream, bstream);
            return blob;
        }
        catch (final IOException e) {
            log.error("IO Error creating blob from bytes : {}", e.getMessage());
        }
        catch (final SQLException e) {
            log.error("SQL Error creating blob from bytes : {}", e.getMessage());
        }
        finally {
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

    /**
     * @param in
     *            the InputStream
     * @return byte[]
     */
    protected byte[] readLobs(InputStream in) {
        byte[] databarr = null;
        try (ByteArrayOutputStream fos = new ByteArrayOutputStream()) {
            int read = 0;
            final byte[] bytes = new byte[2048];

            while ((read = in.read(bytes)) != -1) {
                fos.write(bytes, 0, read);
                log.trace("Copying {} bytes into the output...", read);
            }
            fos.flush();
            databarr = fos.toByteArray();
            return databarr;
        }
        catch (final IOException e) {
            log.error("Exception in readLobs: {}", e.getMessage());
        }
        return new byte[0];
    }

    /**
     * @param dataentity
     *            the Payload
     * @return byte[]
     */
    public byte[] convertToByteArray(Payload dataentity) {
        try {
            log.debug("Retrieving binary stream from payload entity with the DATA blob alone");
            byte[] databarr = null;
            final InputStream in = dataentity.getData().getBinaryStream();
            databarr = readLobs(in);
            dataentity.getData().free();
            return databarr;
        }
        catch (final SQLException e) {
            log.error("Exception : {}", e.getMessage());
        }
        return new byte[0];
    }

    /**
     * @param dataentity
     *            the Payload
     * @return PayloadDto
     */
    public PayloadDto convertToDto(Payload dataentity) {
        try {
            log.debug("Retrieving binary stream from payload entity including the DATA blob");
            byte[] databarr = null;
            byte[] strinfobarr = null;
            // Get the input stream for the data.
            final InputStream in = dataentity.getData().getBinaryStream();
            databarr = readLobs(in);
            dataentity.getData().free();

            // Get the input stream for streamerinfo.
            final InputStream insi = dataentity.getStreamerInfo().getBinaryStream();
            strinfobarr = readLobs(insi);
            dataentity.getStreamerInfo().free();

            return new PayloadDto().hash(dataentity.getHash()).version(dataentity.getVersion())
                    .objectType(dataentity.getObjectType()).size(dataentity.getSize())
                    .data(databarr).streamerInfo(strinfobarr)
                    .insertionTime(dataentity.getInsertionTime());
        }
        catch (final SQLException e) {
            log.error("Exception in convertToDto: {}", e.getMessage());
        }
        return null;
    }

    /**
     * @param dataentity
     *            the Payload
     * @return PayloadDto
     */
    public PayloadDto convertToDtoNoData(Payload dataentity) {
        try {
            log.debug("Retrieving binary stream from payload entity without the DATA blob");
            byte[] strinfobarr = null;

            // Get the input stream for streamerinfo.
            final InputStream insi = dataentity.getStreamerInfo().getBinaryStream();
            strinfobarr = readLobs(insi);
            dataentity.getStreamerInfo().free();

            log.info("Retrieved payload: {} {} {} ", dataentity.getHash(),
                    dataentity.getObjectType(), dataentity.getVersion());
            return new PayloadDto().hash(dataentity.getHash()).version(dataentity.getVersion())
                    .objectType(dataentity.getObjectType()).size(dataentity.getSize())
                    .streamerInfo(strinfobarr);
        }
        catch (final SQLException e) {
            log.error("Exception in convertToDtoNoData: {} ", e.getMessage());
        }
        return null;
    }
}
