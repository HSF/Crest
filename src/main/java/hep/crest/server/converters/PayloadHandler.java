package hep.crest.server.converters;

import hep.crest.server.exceptions.PayloadEncodingException;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;

/**
 * A helper service to handle payload.
 * The handler contains static methods to use inputstreams or byte arrays.
 *
 * @author formica
 */
@Slf4j
public final class PayloadHandler {

    /**
     * Hidden ctor.
     */
    private PayloadHandler() {
    }

    /**
     * @param is the InputStream
     * @return byte[]
     */
    public static byte[] getBytesFromInputStream(InputStream is) {
        byte[] data = null;
        // Test if stream is null.
        if (is == null) {
            return data;
        }
        // Open the output buffer.
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            int nRead;
            data = new byte[16384];
            // Loop over the stream.
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
                log.debug("Reading data from stream {} ", nRead);
            }
            // Flush.
            buffer.flush();
            data = buffer.toByteArray();
        }
        catch (final IOException e) {
            log.error("Exception getting bytes from stream : {}", e.getMessage());
            data = new byte[0];
        }
        return data;
    }

    /**
     * Get hash while reading the stream and saving it to a file. The internal
     * method will close the output and input stream but we also do it here just in
     * case.
     *
     * @param uploadedInputStream  the InputStream
     * @param uploadedFileLocation the String
     * @return String
     * @throws PayloadEncodingException If an Exception occurred
     */
    public static String saveToFileGetHash(InputStream uploadedInputStream,
                                           String uploadedFileLocation) throws PayloadEncodingException {
        // Generate hash.
        try (OutputStream out = new FileOutputStream(uploadedFileLocation)) {
            return HashGenerator.hashoutstream(uploadedInputStream, out);
        }
        catch (NoSuchAlgorithmException | IOException e) {
            throw new PayloadEncodingException("Cannot get hash from file " + uploadedFileLocation, e);
        }
        finally {
            // Close the stream outside the try-with-resource block.
            if (uploadedInputStream != null) {
                try {
                    uploadedInputStream.close();
                }
                catch (final IOException e) {
                    log.error("error closing input stream in saveToFileGetHash: {}",
                            e.getMessage());
                }
            }
        }
    }

    /**
     * @param uploadedInputStream the BufferedInputStream
     * @return String
     * @throws PayloadEncodingException If an Exception occurred
     */
    public static String getHashFromStream(BufferedInputStream uploadedInputStream) throws PayloadEncodingException {
        try {
            // Generate hash.
            return HashGenerator.hash(uploadedInputStream);
        }
        catch (NoSuchAlgorithmException | IOException e) {
            throw new PayloadEncodingException("Error in hashing stream : ", e);
        }
    }

    /**
     * @param uploadedInputStream  the InputStream
     * @param uploadedFileLocation the String
     */
    public static void saveStreamToFile(InputStream uploadedInputStream,
                                        String uploadedFileLocation) {
        // Save input stream to file.
        try (OutputStream out = new FileOutputStream(uploadedFileLocation)) {
            byte[] buffer = new byte[4096]; // 4 KB buffer
            int bytesRead;
            // Read from the InputStream and write to the OutputStream
            while ((bytesRead = uploadedInputStream.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
        catch (final IOException e) {
            log.error("Exception in saveStreamToFile: {}", e.getMessage());
        }
    }

}
