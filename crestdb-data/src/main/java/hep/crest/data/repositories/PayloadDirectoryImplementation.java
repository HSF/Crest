/**
 * 
 */
package hep.crest.data.repositories;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.utils.DirectoryUtilities;
import hep.crest.swagger.model.PayloadDto;

/**
 * @author formica
 *
 */
public class PayloadDirectoryImplementation {

	private static final Logger log = LoggerFactory.getLogger(PayloadDirectoryImplementation.class);

	private DirectoryUtilities dirtools = null;

	
	/**
	 * 
	 */
	public PayloadDirectoryImplementation() {
		super();
	}

	/**
	 * @param dutils
	 */
	public PayloadDirectoryImplementation(DirectoryUtilities dutils) {
		super();
		this.dirtools = dutils;
	}

	/**
	 * @param du
	 */
	public void setDirtools(DirectoryUtilities du) {
		this.dirtools = du;
	}
	
	/**
	 * @param hash
	 * @return
	 * @throws CdbServiceException
	 */
	public PayloadDto find(String hash) throws CdbServiceException {
		Path payloadpath = dirtools.getPayloadPath();
		String hashdir = dirtools.hashdir(hash);
		Path payloadhashpath = Paths.get(payloadpath.toString(),hashdir);
		if (!payloadhashpath.toFile().exists()) {
			throw new CdbServiceException("Cannot find hash dir "+payloadhashpath.toString());
		}
		String filename = hash + ".blob";
		Path payloadfilepath = Paths.get(payloadhashpath.toString(),filename);
		if (!payloadfilepath.toFile().exists()) {
			throw new CdbServiceException("Cannot find file for "+payloadfilepath.toString());
		}

		StringBuilder buf = new StringBuilder();
		try (BufferedReader reader = Files.newBufferedReader(payloadfilepath, dirtools.getCharset())) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				log.debug(line);
				buf.append(line);
			}
			String jsonstring = buf.toString();
			if (jsonstring.isEmpty()) {
				return null;
			}
			return dirtools.getMapper().readValue(jsonstring, PayloadDto.class) ;
		} catch (IOException x) {
			throw new CdbServiceException("Cannot find iov list for hash " + hash);
		}
	}

	/**
	 * @param dto
	 * @return
	 * @throws CdbServiceException
	 */
	public String save(PayloadDto dto) throws CdbServiceException {

		try {
			String hash = dto.getHash();
			Path payloadpath = dirtools.getPayloadPath();

			String hashdir = dirtools.hashdir(hash);
			String payloadfilename = hash+".blob";
			
			Path payloadhashdir = Paths.get(payloadpath.toString(),hashdir);
			if (!payloadhashdir.toFile().exists()) {
				Files.createDirectories(payloadhashdir);
			}
			Path payloadfilepath = Paths.get(payloadhashdir.toString(),payloadfilename);
			if (!payloadfilepath.toFile().exists()) {
				Files.createFile(payloadfilepath);
			} else {
				throw new CdbServiceException("Payload file "+payloadfilepath+"already exists for hash " + hash);
			}
			String jsonstr = dirtools.getMapper().writeValueAsString(dto);

			this.writeBuffer(jsonstr, payloadfilepath);
			return hash;
		} catch (IOException x) {
			throw new CdbServiceException("IO error " + x.getMessage());
		}
	}

	/**
	 * @param jsonstr
	 * @param payloadfilepath
	 * @throws IOException
	 */
	protected void writeBuffer(String jsonstr, Path payloadfilepath) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(payloadfilepath, dirtools.getCharset())) {
			writer.write(jsonstr);
		} catch (IOException x) {
			log.error("Cannot write string {} in {}",jsonstr,payloadfilepath);
			throw x;
		}		
	}
}
