package hep.crest.data.handlers;

import java.io.ByteArrayOutputStream;

// This small utility class extends ByteArrayOutputStream to be able
// to immediately access the buffer of the stream.
/**
 * @author formica
 *
 */
public class CustomByteArrayOutputStream extends ByteArrayOutputStream {
    /**
     * Default ctor.
     */
    public CustomByteArrayOutputStream() {
    }

    /**
     * @param size
     *            the int
     */
    public CustomByteArrayOutputStream(int size) {
        super(size);
    }

    /**
     * @return int
     */
    public int getCount() {
        return super.count;
    }

    /**
     * @return byte[]
     */
    public byte[] getByteBuffer() {
        return super.buf;
    }
}
