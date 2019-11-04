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
import hep.crest.data.handlers.CrestLobHandler;
import hep.crest.data.repositories.PayloadDataBaseCustom;
import hep.crest.server.exceptions.NotExistsPojoException;
import hep.crest.swagger.model.PayloadDto;

/**
 * @author aformic
 *
 */
@Service
public class PayloadService {

    /**
     * Logger.
     */
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Repository.
     */
    @Autowired
    @Qualifier("payloaddatadbrepo")
    private PayloadDataBaseCustom payloaddataRepository;

    /**
     * Handler.
     */
    @Autowired
    private CrestLobHandler lobHandler;

    /**
     * @param hash
     *            the String
     * @return PayloadDto
     * @throws CdbServiceException
     *             If an Exception occurred
     * @throws NotExistsPojoException
     *             If object was not found
     */
    @Transactional
    public PayloadDto getPayload(String hash) throws CdbServiceException, NotExistsPojoException {
        final PayloadDto pyld = payloaddataRepository.find(hash);
        if (pyld == null) {
            throw new NotExistsPojoException("Cannot find payload dto for hash " + hash);
        }
        return pyld;
    }

    /**
     * @param hash
     *            the String
     * @return PayloadDto
     * @throws CdbServiceException
     *             If an Exception occurred
     * @throws NotExistsPojoException
     *             If object was not found
     */
    @Transactional
    public PayloadDto getPayloadMetaInfo(String hash)
            throws CdbServiceException, NotExistsPojoException {
        final PayloadDto pyld = payloaddataRepository.findMetaInfo(hash);
        if (pyld == null) {
            throw new NotExistsPojoException("Cannot find payload meta data for hash " + hash);
        }
        return pyld;
    }

    /**
     * @param hash
     *            the String
     * @return InputStream
     * @throws CdbServiceException
     *             If an Exception occurred
     * @throws NotExistsPojoException
     *             If object was not found
     */
    @Transactional
    public InputStream getPayloadData(String hash)
            throws CdbServiceException, NotExistsPojoException {
        final InputStream is = payloaddataRepository.findData(hash);
        if (is == null) {
            throw new NotExistsPojoException("Cannot find payload data for hash " + hash);
        }
        return is;
    }

    /**
     * @param dto
     *            the PayloadDto
     * @return PayloadDto
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    @Transactional
    public PayloadDto insertPayload(PayloadDto dto) throws CdbServiceException {
        try {
            log.debug("Save payload dto {}", dto);
            if (dto.getSize() == null) {
                dto.setSize(dto.getData().length);
            }
            final PayloadDto saved = payloaddataRepository.save(dto);
            log.debug("Saved entity: {}", saved);
            return saved;
        }
        catch (final Exception e) {
            log.error("Exception in storing payload {}", dto);
            throw new CdbServiceException("Cannot store payload dto : " + e.getMessage());
        }
    }

    /**
     * @param dto
     *            the PayloadDto
     * @param is
     *            the InputStream
     * @return PayloadDto
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    @Transactional
    public PayloadDto insertPayloadAndInputStream(PayloadDto dto, InputStream is)
            throws CdbServiceException {
        try {
            log.debug("Save payload {} creating blob from inputstream...", dto);
            final PayloadDto saved = payloaddataRepository.save(dto, is);
            log.debug("Saved entity: {}", saved);
            return saved;
        }
        catch (final Exception e) {
            log.debug("Exception in storing payload {}", dto);
            throw new CdbServiceException("Cannot store payload : " + e.getMessage());
        }
    }

}
