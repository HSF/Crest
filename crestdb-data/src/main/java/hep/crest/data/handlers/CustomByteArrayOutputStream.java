package hep.crest.data.handlers;

import java.io.ByteArrayOutputStream;


// This small utility class extends ByteArrayOutputStream to be able
// to immediately access the buffer of the stream.
public class CustomByteArrayOutputStream extends ByteArrayOutputStream {
	public CustomByteArrayOutputStream() {
	}

	public CustomByteArrayOutputStream(int size) {
		super(size);
	}

	public int getCount() {
		return super.count;
	}

	public byte[] getByteBuffer() {
		return super.buf;
	}
}
