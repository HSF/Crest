package hep.crest.data.handlers;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.DigestUtils;

import hep.crest.data.exceptions.PayloadEncodingException;

/**
 * Utility class for hash generation. Should be used when the client does not
 * provide the hash himself.
 *
 * @author formica
 *
 */
public final class HashGenerator {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(HashGenerator.class);

    /**
     * MD5.
     */
    private static final String MD5 = "MD5";
    /**
     * SHA.
     */
    private static final String SHA = "SHA-256";

    /**
     * Default ctor.
     */
    private HashGenerator() {
        // Hide the default ctor.
    }

    /**
     * Java program to generate MD5 hash or digest for String. In this example * we
     * will see 3 ways to create MD5 hash or digest using standard Java API, *
     * Spring framework and open source library, Apache commons codec utilities.*
     * Generally MD5 has are represented as Hex String so each of this function *
     * will return MD5 hash in hex format.
     *
     * @author Javin Paul
     *
     * @param message
     *            The message string from which to generate md5.
     * @return The MD5 representation of the message string.
     * @throws PayloadEncodingException If an Exception occurred
     **/
    public static String md5Java(String message) {
        return md5Java(message.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * @param message
     *            The message byte[] from which to generate md5.
     * @return The MD5 representation of the message string.
     * @throws PayloadEncodingException If an Exception occurred
     */
    public static String md5Java(byte[] message) {
        String digest = null;
        try {
            final MessageDigest md = MessageDigest.getInstance(MD5);
            final byte[] hash = md.digest(message);
            // converting byte array to Hexadecimal String
            final StringBuilder sb = new StringBuilder(2 * hash.length);
            for (final byte b : hash) {
                sb.append(String.format("%02x", b & 0xff));
            }
            digest = sb.toString();
            log.trace("md5Java generated hash: {}", digest);
        }
        catch (final NoSuchAlgorithmException ex) {
            throw new PayloadEncodingException("md5java encoding error:", ex);
        }
        return digest;
    }

    /**
     * @param message
     *            The message byte[] from which to generate md5.
     * @return The MD5 representation of the message string.
     * @throws PayloadEncodingException If an Exception occurred
     */
    public static String shaJava(byte[] message) {
        String digest = null;
        try {
            final MessageDigest md = MessageDigest.getInstance(SHA);
            final byte[] hash = md.digest(message);
            // converting byte array to Hexadecimal String
            final StringBuilder sb = new StringBuilder(2 * hash.length);
            for (final byte b : hash) {
                sb.append(String.format("%02x", b & 0xff));
            }
            digest = sb.toString();
            log.trace("shaJava generated hash: {}", digest);
        }
        catch (final NoSuchAlgorithmException ex) {
            throw new PayloadEncodingException("shaJava exception: ", ex);
        }
        return digest;
    }

    /**
     * Spring framework also provides overloaded md5 methods. You can pass input as
     * String or byte array and Spring can return hash or digest either as byte
     * array or Hex String. Here we are passing String as input and getting MD5 hash
     * as hex String.
     *
     * @param text
     *            The text string from which to generate md5 using DigestUtils.
     * @return The MD5 representation of the message string.
     * @throws PayloadEncodingException If an Exception occurred
     **/
    public static String md5Spring(String text) {
        return DigestUtils.md5DigestAsHex(text.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * @param text
     *            The text byte[] from which to generate md5 using DigestUtils.
     * @return The M5 representation of text.
     */
    public static String md5Spring(byte[] text) {
        return DigestUtils.md5DigestAsHex(text);
    }

    /**
     * @param in the BufferedInputStream
     * @return String
     * @throws IOException If an Exception occurred
     * @throws NoSuchAlgorithmException If an Exception occurred
     */
    public static String hash(BufferedInputStream in) throws IOException, NoSuchAlgorithmException {

        String digestHash;
        final MessageDigest digest = MessageDigest.getInstance(SHA);
        final byte[] buffer = new byte[1024];
        int sizeRead = -1;
        while ((sizeRead = in.read(buffer)) != -1) {
            digest.update(buffer, 0, sizeRead);
        }
        in.close();

        byte[] hash = null;
        hash = digest.digest();

        // converting byte array to Hexadecimal String
        final StringBuilder sb = new StringBuilder(2 * hash.length);
        for (final byte b : hash) {
            sb.append(String.format("%02x", b & 0xff));
        }
        digestHash = sb.toString();
        return digestHash;
    }

    /**
     * @param in
     *            the InputStream
     * @param os
     *            the OutputStream
     * @return String
     * @throws IOException
     *             If an Exception occurred
     * @throws NoSuchAlgorithmException
     *             If an Exception occurred
     */
    public static String hashoutstream(InputStream in, OutputStream os)
            throws IOException, NoSuchAlgorithmException {

        String digestHash;
        final MessageDigest digest = MessageDigest.getInstance(SHA);
        final byte[] buffer = new byte[1024];
        int sizeRead = -1;
        int loop = 0;
        while ((sizeRead = in.read(buffer)) != -1) {
            digest.update(buffer, 0, sizeRead);
            os.write(buffer, 0, sizeRead);
            loop++;
            if (loop % 100 == 0) {
                os.flush();
            }
        }
        os.flush();
        os.close();
        in.close();

        byte[] hash = null;
        hash = digest.digest();
        // converting byte array to Hexadecimal String
        final StringBuilder sb = new StringBuilder(2 * hash.length);
        for (final byte b : hash) {
            sb.append(String.format("%02x", b & 0xff));
        }
        digestHash = sb.toString();
        return digestHash;
    }

}
