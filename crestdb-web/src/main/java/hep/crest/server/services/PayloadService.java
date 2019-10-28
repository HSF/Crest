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
    private PayloadHandler payloadHandler;

    /**
     * @param hash
     *            the String
     * @return PayloadDto
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    @Transactional
    public PayloadDto getPayload(String hash) throws CdbServiceException {
        try {
            final Payload pyld = payloaddataRepository.find(hash);
            if (pyld == null) {
                throw new CdbServiceException("Cannot find payload dto for hash " + hash);
            }
            return payloadHandler.convertToDto(pyld);
        }
        catch (final Exception e) {
            throw new CdbServiceException(e.getMessage());
        }
    }

    /**
     * @param hash
     *            the String
     * @return PayloadDto
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    @Transactional
    public PayloadDto getPayloadMetaInfo(String hash) throws CdbServiceException {
        try {
            final Payload pyld = payloaddataRepository.findMetaInfo(hash);
            if (pyld == null) {
                throw new CdbServiceException("Cannot find payload meta data for hash " + hash);
            }
            return payloadHandler.convertToDtoNoData(pyld);
        }
        catch (final Exception e) {
            throw new CdbServiceException(e.getMessage());
        }
    }

    /**
     * @param hash
     *            the String
     * @return InputStream
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    @Transactional
    public InputStream getPayloadData(String hash) throws CdbServiceException {
        try {
            final Payload pyld = payloaddataRepository.findData(hash);
            if (pyld == null) {
                throw new CdbServiceException("Cannot find payload data for hash " + hash);
            }
            final byte[] bindata = payloadHandler.convertToByteArray(pyld);
            log.debug("Converted pojo in byte array of length {}", bindata.length);
            return new ByteArrayInputStream(bindata);
        }
        catch (final Exception e) {
            throw new CdbServiceException(e.getMessage());
        }
    }

    /**
     * @param hash
     *            the String
     * @return StreamingOutput
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    public StreamingOutput getPayloadDataStream(String hash) throws CdbServiceException {
        try {
            final Payload pyld = payloaddataRepository.findData(hash);
            if (pyld == null) {
                throw new CdbServiceException("Cannot find payload data for hash " + hash);
            }
            // Here we should distinguish between Oracle, h2, or postgres
            // Payload handling in postgres requires a more complex way of reading: the
            // whole LOB
            // should be read beforehand. For the moment we use this method with any DB, but
            // later on
            // we may try to do better for non-postgres db.

            // Oracle and others
            // code example: InputStream in = pyld.getData().getBinaryStream();
            log.debug("Read data from Blob...{} length {}", pyld.getData(),
                    pyld.getData().length());
            final byte[] data = payloadHandler.getBytesFromInputStream(pyld.getData().getBinaryStream());
            final InputStream in = new ByteArrayInputStream(data);
            return new StreamingOutput() {
                @Override
                public void write(OutputStream os) throws IOException, WebApplicationException {
                    try {
                        int read = 0;
                        final byte[] bytes = new byte[2048];

                        while ((read = in.read(bytes)) != -1) {
                            os.write(bytes, 0, read);
                            log.trace("Copying {} bytes into the output...", read);
                        }
                        os.flush();
                    }
                    catch (final Exception e) {
                        throw new WebApplicationException(e);
                    }
                    finally {
                        log.debug("closing streams...");
                        if (os != null) {
                            os.close();
                        }
                    }
                }
            };
        }
        catch (final Exception e) {
            throw new CdbServiceException(e.getMessage());
        }
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
            log.debug("Save payload {}", dto);
            if (dto.getSize() == null) {
                dto.setSize(dto.getData().length);
            }
            final Payload saved = payloaddataRepository.save(dto);
            log.debug("Saved entity: {}", saved);
            return payloadHandler.convertToDtoNoData(saved);
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
            final Payload saved = payloaddataRepository.save(dto, is);
            log.debug("Saved entity: {}", saved);
            return payloadHandler.convertToDtoNoData(saved);
        }
        catch (final Exception e) {
            log.debug("Exception in storing payload {}", dto);
            throw new CdbServiceException("Cannot store payload : " + e.getMessage());
        }
    }

}
