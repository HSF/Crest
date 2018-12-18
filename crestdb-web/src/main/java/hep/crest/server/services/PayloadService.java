/**
 * 
 */
package hep.crest.server.services;


import java.io.InputStream;

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
	
	@Transactional
	public PayloadDto getPayload(String hash) throws CdbServiceException {
		try {
			Payload pyld = payloaddataRepository.find(hash);
			if (pyld == null) {
				throw new CdbServiceException("Cannot find payload data for hash " + hash);
			}
			PayloadDto dtoentity = payloadHandler.convertToDto(pyld);
			return dtoentity;
		} catch (Exception e) {
			throw new CdbServiceException(e.getMessage());
		}
	}
	
	@Transactional
	public PayloadDto getPayloadMetaInfo(String hash) throws CdbServiceException {
		try {
			Payload pyld = payloaddataRepository.findMetaInfo(hash);
			if (pyld == null) {
				throw new CdbServiceException("Cannot find payload data for hash " + hash);
			}
			PayloadDto dtoentity = payloadHandler.convertToDtoNoData(pyld);
			return dtoentity;
		} catch (Exception e) {
			throw new CdbServiceException(e.getMessage());
		}
	}
	
	@Transactional
	public InputStream getPayloadData(String hash) throws CdbServiceException {
		try {
			Payload pyld = payloaddataRepository.findData(hash);
			if (pyld == null) {
				throw new CdbServiceException("Cannot find payload data for hash " + hash);
			}
			return pyld.getData().getBinaryStream();
		} catch (Exception e) {
			throw new CdbServiceException(e.getMessage());
		}
	}

	@Transactional
	public PayloadDto insertPayload(PayloadDto dto) throws CdbServiceException {
		try {
			log.debug("Save payload " + dto);
			Payload saved = payloaddataRepository.save(dto);
			log.debug("Saved entity: " + saved);
			PayloadDto dtoentity = payloadHandler.convertToDtoNoData(saved);
			return dtoentity;
		} catch (Exception e) {
			log.debug("Exception in storing payload " + dto);
			throw new CdbServiceException("Cannot store payload : " + e.getMessage());
		}
	}

	public String saveInputStreamGetHash(InputStream is, String file) throws CdbServiceException {
		try {
			log.debug("Save inputstream and compute hash");
			return payloadHandler.saveToFileGetHash(is, file);
		} catch (Exception e) {
			log.debug("Exception in copying payload to disk in file {}",file);
			throw new CdbServiceException("Cannot store payload : " + e.getMessage());
		}
	}

	@Transactional
	public PayloadDto insertPayloadAndInputStream(PayloadDto dto, InputStream is) throws CdbServiceException {
		try {
			log.debug("Save payload {} creating blob from inputstream...",dto);
//			Blob blob = payloadHandler.createBlobFromStream(is);
			Payload saved = payloaddataRepository.save(dto,is);
			log.debug("Saved entity: {}", saved);
			PayloadDto dtoentity = payloadHandler.convertToDtoNoData(saved);
			return dtoentity;
		} catch (Exception e) {
			log.debug("Exception in storing payload {}",dto);
			throw new CdbServiceException("Cannot store payload : " + e.getMessage());
		}
	}
	
}
