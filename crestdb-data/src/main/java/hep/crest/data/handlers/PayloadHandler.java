package hep.crest.data.handlers;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

import org.hibernate.engine.jdbc.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.exceptions.PayloadEncodingException;

/**
 * A helper service to handle payload.
 * 
 * @author formica
 *
 */
public final class PayloadHandler {

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(PayloadHandler.class);

    /**
     * Max length for reading.
     */
    private static final Integer MAX_LENGTH = 1024;

    /**
     * Hidden ctor.
     */
    private PayloadHandler() {
    }

    /**
     * @param is
     *            the InputStream
     * @return byte[]
     */
    public static byte[] getBytesFromInputStream(InputStream is) {
        byte[] data = null;
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            int nRead;
            data = new byte[16384];

            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
                LOG.debug("Reading data from stream {} ", nRead);
            }
            buffer.flush();
            data = buffer.toByteArray();
        }
        catch (final IOException e) {
            LOG.error("Exception getting bytes from stream : {}", e.getMessage());
            data = new byte[0];
        }
        return data;
    }

    /**
     * @param uploadedInputStream
     *            the InputStream
     * @param uploadedFileLocation
     *            the String
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    public static void saveToFile(InputStream uploadedInputStream, String uploadedFileLocation)
            throws CdbServiceException {

        try (OutputStream out = new FileOutputStream(new File(uploadedFileLocation))) {
            int read = 0;
            final byte[] bytes = new byte[MAX_LENGTH];

            while ((read = uploadedInputStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
        }
        catch (final IOException e) {
            LOG.error("Exception saving stream to file: {}", e.getMessage());
            throw new CdbServiceException("Cannot save stream to file " + uploadedFileLocation);
        }
    }

    /**
     * Save the inputStream to the outputStream.
     *
     * @param uploadedInputStream
     *            the InputStream
     * @param out
     *            the OutputStream
     */
    public static void saveToOutStream(InputStream uploadedInputStream, OutputStream out) {

        try {
            int read = 0;
            final byte[] bytes = new byte[MAX_LENGTH];
            while ((read = uploadedInputStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
        }
        catch (final IOException e) {
            LOG.error("Exception in saveToOutStream : {}", e.getMessage());
        }
        finally {
            try {
                uploadedInputStream.close();
                out.close();
            }
            catch (final IOException e) {
                LOG.error("Exception in saveToOutStream when closing : {}", e.getMessage());
            }
        }
    }

    /**
     * Get hash while reading the stream and saving it to a file. The internal
     * method will close the output and input stream but we also do it here just in
     * case.
     *
     * @param uploadedInputStream
     *            the InputStream
     * @param uploadedFileLocation
     *            the String
     * @return String
     * @throws PayloadEncodingException
     *             If an Exception occurred
     */
    public static String saveToFileGetHash(InputStream uploadedInputStream,
            String uploadedFileLocation) throws PayloadEncodingException {

        try (OutputStream out = new FileOutputStream(new File(uploadedFileLocation))) {
            return HashGenerator.hashoutstream(uploadedInputStream, out);
        }
        catch (NoSuchAlgorithmException | IOException e) {
            LOG.error("Cannot generate hash : {}", e.getMessage());
            throw new PayloadEncodingException(e.getMessage());
        }
        finally {
            // Close the stream outside the try-with-resource block.
            if (uploadedInputStream != null) {
                try {
                    uploadedInputStream.close();
                }
                catch (final IOException e) {
                    LOG.error("error closing input stream in saveToFileGetHash: {}",
                            e.getMessage());
                }
            }
        }
    }

    /**
     * @param uploadedInputStream
     *            the BufferedInputStream
     * @return String
     * @throws PayloadEncodingException
     *             If an Exception occurred
     */
    public static String getHashFromStream(BufferedInputStream uploadedInputStream)
            throws PayloadEncodingException {
        try {
            return HashGenerator.hash(uploadedInputStream);
        }
        catch (NoSuchAlgorithmException | IOException e) {
            LOG.error("Error in hashing stream : {}", e.getMessage());
            throw new PayloadEncodingException("Error in hashing stream : " + e.getMessage());
        }
    }

    /**
     * @param uploadedInputStream
     *            the InputStream
     * @param uploadedFileLocation
     *            the String
     */
    public static void saveStreamToFile(InputStream uploadedInputStream,
            String uploadedFileLocation) {

        try (OutputStream out = new FileOutputStream(new File(uploadedFileLocation))) {
            StreamUtils.copy(uploadedInputStream, out);
        }
        catch (final IOException e) {
            LOG.error("Exception in saveStreamToFile: {}", e.getMessage());
        }
    }

    /**
     * @param uploadedFileLocation
     *            the String
     * @return byte[]
     */
    public static byte[] readFromFile(String uploadedFileLocation) {
        byte[] databarr = null;
        try {
            final java.nio.file.Path path = Paths.get(uploadedFileLocation);
            databarr = Files.readAllBytes(path);
        }
        catch (final IOException e) {
            LOG.error("Exception in readFromFile: {}", e.getMessage());
            databarr = new byte[0];
        }
        return databarr;

    }

    /**
     * @param uploadedFileLocation
     *            the String
     * @return long
     */
    public static long lengthOfFile(String uploadedFileLocation) {
        long flength = 0;
        try {
            final java.nio.file.Path path = Paths.get(uploadedFileLocation);
            Files.size(path);
            flength = Files.size(path);
        }
        catch (final IOException e) {
            LOG.error("Exception in lengthOfFile: {}", e.getMessage());
        }
        return flength;
    }
}
