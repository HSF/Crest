package hep.crest.data.handlers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.hibernate.engine.jdbc.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import hep.crest.data.exceptions.PayloadEncodingException;
import hep.crest.data.pojo.Payload;
import hep.crest.swagger.model.PayloadDto;

@Service
public class PayloadHandler {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	@Qualifier("dataSource")
	private DataSource ds;

	private static Integer MAX_LENGTH = 1024;

	public byte[] getBytesFromInputStream(InputStream is) {
		try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
			int nRead;
			byte[] data = new byte[16384];

			while ((nRead = is.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, nRead);
			}
			buffer.flush();
			return buffer.toByteArray();
		} catch (IOException e) {
			log.error("Exception : {}",e.getMessage());
		}
		return new byte[0];
	}

	public void saveToFile(InputStream uploadedInputStream, String uploadedFileLocation) {

		try (OutputStream out = new FileOutputStream(new File(uploadedFileLocation))) {
			int read = 0;
			byte[] bytes = new byte[MAX_LENGTH];

			while ((read = uploadedInputStream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			out.flush();
		} catch (IOException e) {
			log.error("Exception : {}",e.getMessage());
		}
	}

	public void saveToOutStream(InputStream uploadedInputStream, OutputStream out) {

		try {
			int read = 0;
			byte[] bytes = new byte[MAX_LENGTH];
			while ((read = uploadedInputStream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			out.flush();
		} catch (IOException e) {
			log.error("Exception : {}",e.getMessage());
		} finally {
			try {
				uploadedInputStream.close();
				out.close();
			} catch (IOException e) {
				log.error("Exception : {}",e.getMessage());
			}
		}
	}

	public String saveToFileGetHash(InputStream uploadedInputStream, String uploadedFileLocation)
			throws PayloadEncodingException {

		try (OutputStream out = new FileOutputStream(new File(uploadedFileLocation))) {
			return HashGenerator.hashoutstream(uploadedInputStream, out);
		} catch (NoSuchAlgorithmException | IOException e) {
			throw new PayloadEncodingException(e.getMessage());
		}
	}

	public void saveStreamToFile(InputStream uploadedInputStream, String uploadedFileLocation) {

		try (OutputStream out = new FileOutputStream(new File(uploadedFileLocation))) {
			StreamUtils.copy(uploadedInputStream, out);
		} catch (IOException e) {
			log.error("Exception : {}",e.getMessage());
		}
	}

	public byte[] readFromFile(String uploadedFileLocation) {
		try {
			java.nio.file.Path path = Paths.get(uploadedFileLocation);
			byte[] data = Files.readAllBytes(path);
			return data;
		} catch (IOException e) {
			log.error("Exception : {}",e.getMessage());
		}
		return new byte[0];

	}

	public long lengthOfFile(String uploadedFileLocation) {

		try {
			java.nio.file.Path path = Paths.get(uploadedFileLocation);
			Files.size(path);
			return Files.size(path);
		} catch (IOException e) {
			log.error("Exception : {}",e.getMessage());
		}
		return 0;
	}

	public Blob createBlobFromFile(String filelocation) {
		Blob blob = null;
		BufferedOutputStream bstream = null;
		File f = new File(filelocation);
		try (Connection conn = ds.getConnection();
			 BufferedInputStream fstream = new BufferedInputStream(new FileInputStream(f));
				) {
			
			blob = conn.createBlob();
			bstream = new BufferedOutputStream(blob.setBinaryStream(1));
			// stream copy runs a high-speed upload across the network
			StreamUtils.copy(fstream, bstream);
			return blob;
		} catch (IOException | SQLException e) {
			log.error("Exception : {}",e.getMessage());
		} finally {
			try {
				if (bstream != null) {
					bstream.close();
				}
			} catch (IOException e) {
				log.error("Exception : {}",e.getMessage());
			}
		}
		return blob;
	}

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
		} catch (IOException | SQLException e) {
			log.error("Exception : {}",e.getMessage());
		} finally {
			try {
				if (bstream != null) {
					bstream.close();
				}
			} catch (IOException e) {
				log.error("Exception : {}",e.getMessage());
			}
		}
		return blob;
	}

	public Blob createBlobFromByteArr(byte[] data) {
		Blob blob = null;
		Connection conn = null;
		try {
			conn = ds.getConnection();
			InputStream is = new ByteArrayInputStream(data);
			BufferedInputStream fstream = new BufferedInputStream(is);
			blob = conn.createBlob();
			BufferedOutputStream bstream = new BufferedOutputStream(blob.setBinaryStream(1));
			// stream copy runs a high-speed upload across the network
			StreamUtils.copy(fstream, bstream);
			return blob;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return blob;
	}

	protected byte[] readLobs(InputStream in) {
		byte[] databarr = null;
		try (ByteArrayOutputStream fos = new ByteArrayOutputStream()) {
			int read = 0;
			byte[] bytes = new byte[2048];

			while ((read = in.read(bytes)) != -1) {
				fos.write(bytes, 0, read);
				log.trace("Copying {} bytes into the output...",read);
			}
			fos.flush();
			databarr = fos.toByteArray();
			return databarr;
		} catch (IOException e) {
			log.error("Exception : {}",e.getMessage());
		}
		return null;		
	}
	
	public PayloadDto convertToDto(Payload dataentity) {
		try {
			log.debug("Retrieving binary stream from payload entity including the DATA blob");
			byte[] databarr = null;
			byte[] strinfobarr = null;
			InputStream in = dataentity.getData().getBinaryStream();
			databarr = readLobs(in);
			dataentity.getData().free();

			// Now get Streamerinfo BLOB.
			InputStream insi = dataentity.getStreamerInfo().getBinaryStream();
			strinfobarr = readLobs(insi);
			dataentity.getStreamerInfo().free();

			PayloadDto entitydto = new PayloadDto().hash(dataentity.getHash()).version(dataentity.getVersion())
					.objectType(dataentity.getObjectType()).size(dataentity.getSize()).data(databarr).streamerInfo(strinfobarr)
					.insertionTime(dataentity.getInsertionTime());
			return entitydto;
		} catch (SQLException e) {
			log.error("Exception : {}",e.getMessage());
		}
		return null;
	}

	public PayloadDto convertToDtoNoData(Payload dataentity) {
		try {
			log.debug("Retrieving binary stream from payload entity without the DATA blob");
			byte[] strinfobarr = null;

			// Now get Streamerinfo BLOB.
			InputStream insi = dataentity.getStreamerInfo().getBinaryStream();
			strinfobarr = readLobs(insi);
			dataentity.getStreamerInfo().free();

			log.info("Retrieved payload: {} {} {} ",dataentity.getHash(),dataentity.getObjectType(),dataentity.getVersion());
			PayloadDto entitydto = new PayloadDto().hash(dataentity.getHash()).version(dataentity.getVersion())
					.objectType(dataentity.getObjectType()).size(dataentity.getSize()).streamerInfo(strinfobarr);
			return entitydto;
		} catch (SQLException e) {
			log.error("Exception : {} ",e.getMessage());
		}
		return null;
	}

}
