/**
 * 
 */
package hep.crest.server.services;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.handlers.PayloadHandler;
import hep.crest.data.pojo.Payload;
import hep.crest.data.repositories.PayloadDataBaseCustom;
import hep.crest.swagger.model.PayloadDto;


/**
 * @author aformic
 *
 */
@Service
public class PayloadService {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	@Qualifier("payloaddatadbrepo")
	private PayloadDataBaseCustom payloaddataRepository;

	@Autowired
	private PayloadHandler payloadHandler;
	
	/**
	 * @param hash
	 * @return
	 * @throws CdbServiceException
	 */
	@Transactional
	public PayloadDto getPayload(String hash) throws CdbServiceException {
		try {
			Payload pyld = payloaddataRepository.find(hash);
			if (pyld == null) {
				throw new CdbServiceException("Cannot find payload dto for hash " + hash);
			}
			return payloadHandler.convertToDto(pyld);
		} catch (Exception e) {
			throw new CdbServiceException(e.getMessage());
		}
	}
	
	/**
	 * @param hash
	 * @return
	 * @throws CdbServiceException
	 */
	@Transactional
	public PayloadDto getPayloadMetaInfo(String hash) throws CdbServiceException {
		try {
			Payload pyld = payloaddataRepository.findMetaInfo(hash);
			if (pyld == null) {
				throw new CdbServiceException("Cannot find payload meta data for hash " + hash);
			}
			return payloadHandler.convertToDtoNoData(pyld);
		} catch (Exception e) {
			throw new CdbServiceException(e.getMessage());
		}
	}

	/**
	 * @param hash
	 * @return
	 * @throws CdbServiceException
	 */
	@Transactional
	public InputStream getPayloadData(String hash) throws CdbServiceException {
		try {
			Payload pyld = payloaddataRepository.findData(hash);
			if (pyld == null) {
				throw new CdbServiceException("Cannot find payload data for hash " + hash);
			}			
			byte[] bindata = payloadHandler.convertToByteArray(pyld);
			log.debug("Converted pojo in byte array of length {}",bindata.length);
			return new ByteArrayInputStream(bindata);
		} catch (Exception e) {
			throw new CdbServiceException(e.getMessage());
		}
	}

	/**
	 * @param hash
	 * @return
	 * @throws CdbServiceException
	 */
	public StreamingOutput getPayloadDataStream(String hash) throws CdbServiceException {
		try {
			Payload pyld = payloaddataRepository.findData(hash);
			if (pyld == null) {
				throw new CdbServiceException("Cannot find payload data for hash " + hash);
			}
			// Here we should distinguish between Oracle, h2, or postgres
			// Payload handling in postgres requires a more complex way of reading: the whole LOB
			// should be read beforehand. For the moment we use this method with any DB, but later on
			// we may try to do better for non-postgres db.
			
			// Oracle and others
			// code example: InputStream in = pyld.getData().getBinaryStream();
			log.debug("Read data from Blob...{} length {}",pyld.getData(),pyld.getData().length());
			byte[] data = payloadHandler.getBytesFromInputStream(pyld.getData().getBinaryStream());
			InputStream in = new ByteArrayInputStream(data);
			return new StreamingOutput() {
				@Override
				public void write(OutputStream os) throws IOException, WebApplicationException {
					try {
						int read = 0;
						byte[] bytes = new byte[2048];

						while ((read = in.read(bytes)) != -1) {
							os.write(bytes, 0, read);
							log.trace("Copying {} bytes into the output...",read);
						}
						os.flush();
					} catch (Exception e) {
						throw new WebApplicationException(e);
					} finally {
						log.debug("closing streams...");
						if (os != null)
							os.close();
					}
				}
			};
		} catch (Exception e) {
			throw new CdbServiceException(e.getMessage());
		}
	}

	/**
	 * @param dto
	 * @return
	 * @throws CdbServiceException
	 */
	@Transactional
	public PayloadDto insertPayload(PayloadDto dto) throws CdbServiceException {
		try {
			log.debug("Save payload {}", dto);
			if (dto.getSize() == null) {
				dto.setSize(dto.getData().length);
			}
			Payload saved = payloaddataRepository.save(dto);
			log.debug("Saved entity: {}", saved);
			return payloadHandler.convertToDtoNoData(saved);
		} catch (Exception e) {
			log.error("Exception in storing payload {}", dto);
			throw new CdbServiceException("Cannot store payload dto : " + e.getMessage());
		}
	}

	/**
	 * @param is
	 * @param file
	 * @return
	 * @throws CdbServiceException
	 */
	public String saveInputStreamGetHash(InputStream is, String file) throws CdbServiceException {
		try {
			log.debug("Save inputstream and compute hash");
			return payloadHandler.saveToFileGetHash(is, file);
		} catch (Exception e) {
			log.error("Exception in copying payload to disk in file {}",file);
			throw new CdbServiceException("Cannot store payload : " + e.getMessage());
		}
	}

	/**
	 * @param dto
	 * @param is
	 * @return
	 * @throws CdbServiceException
	 */
	@Transactional
	public PayloadDto insertPayloadAndInputStream(PayloadDto dto, InputStream is) throws CdbServiceException {
		try {
			log.debug("Save payload {} creating blob from inputstream...",dto);
			Payload saved = payloaddataRepository.save(dto,is);
			log.debug("Saved entity: {}", saved);
			return payloadHandler.convertToDtoNoData(saved);
		} catch (Exception e) {
			log.debug("Exception in storing payload {}",dto);
			throw new CdbServiceException("Cannot store payload : " + e.getMessage());
		}
	}
	
}
