/**
 *
 */
package hep.crest.server.services;

import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.pojo.Iov;
import hep.crest.data.pojo.Tag;
import hep.crest.data.repositories.PayloadDataBaseCustom;
import hep.crest.server.exceptions.AlreadyExistsIovException;
import hep.crest.server.exceptions.HashExistsException;
import hep.crest.server.exceptions.NotExistsPojoException;
import hep.crest.swagger.model.HTTPResponse;
import hep.crest.swagger.model.IovDto;
import hep.crest.swagger.model.PayloadDto;
import ma.glasnost.orika.MapperFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.ws.rs.core.Response;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author aformic
 *
 */
@Service
public class PayloadService {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(PayloadService.class);

    /**
     * Repository.
     */
    @Autowired
    private IovService iovService;
    /**
     * Mapper.
     */
    @Autowired
    @Qualifier("mapper")
    private MapperFacade mapper;

    /**
     * Repository.
     */
    @Autowired
    @Qualifier("payloaddatadbrepo")
    private PayloadDataBaseCustom payloaddataRepository;

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
    public PayloadDto getPayload(String hash) {
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
    public PayloadDto getPayloadMetaInfo(String hash) {
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
    public InputStream getPayloadData(String hash) {
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
    //@Transactional
    public PayloadDto insertPayload(PayloadDto dto) {
        log.debug("Save payload dto {}", dto);
        if (dto.getSize() == null) {
            dto.setSize(dto.getData().length);
        }
        // Verify if hash exists
        String dbhash = payloaddataRepository.exists(dto.getHash());
        if (dbhash != null && dbhash.length() > 0) {
            throw new HashExistsException("Hash already exists " + dto.getHash());
        }
        // Store the payload dto
        final PayloadDto saved = payloaddataRepository.save(dto);
        log.debug("Saved entity: {}", saved);
        return saved;
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
    public PayloadDto insertPayloadAndInputStream(PayloadDto dto, InputStream is) {
        log.debug("Save payload {} creating blob from inputstream...", dto);
        // Verify if hash exists
        String dbhash = payloaddataRepository.exists(dto.getHash());
        if (dbhash != null && dbhash.length() > 0) {
            throw new HashExistsException("Hash already exists " + dto.getHash());
        }

        final PayloadDto saved = payloaddataRepository.save(dto, is);
        log.debug("Saved entity: {}", saved);
        return saved;
    }


    /**
     * Save IOV and Payload in one request.
     *
     * @param dto
     * @param pdto
     * @param filename
     * @return HTTPResponse
     * @throws CdbServiceException
     */
    @Transactional(rollbackOn = {CdbServiceException.class})
    public HTTPResponse saveIovAndPayload(IovDto dto, PayloadDto pdto, String filename) {
        log.debug("Create dto with hash {},  format {}, ...", dto.getPayloadHash(),
                pdto.getObjectType());
        try {
            PayloadDto saved = null;
            if (filename != null) {
                saved = insertPayloadFromFile(filename, pdto, dto);
            }
            else {
                saved = insertPayloadFromDto(pdto, dto);
            }
            String tagname = dto.getTagName();
            Iov entity = mapper.map(dto, Iov.class);
            entity.setTag(new Tag(tagname));
            final Iov savediov = iovService.insertIov(entity);
            IovDto saveddto = mapper.map(savediov, IovDto.class);
            saveddto.tagName(tagname);
            dto.tagName(tagname);
            log.debug("Saved Iov Dto {} ", saveddto);
            // Everything ok, so send back a "created" status code.
            log.debug("Created payload {} and iov {} ", saved, savediov);
            return new HTTPResponse().code(Response.Status.CREATED.getStatusCode())
                    .id(saveddto.getPayloadHash()).message("Iov created in tag "
                                                           + saveddto.getTagName() + " @ " + saveddto.getSince());
        }
        catch (IOException ex) {
            log.error("IO Exception in reading payload data: {}", ex.getMessage());
            return new HTTPResponse().code(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                    .id(dto.getPayloadHash()).message("IO Exception in reading payload data for "
                                                      + dto.getTagName());
        }
        catch (final NotExistsPojoException e) {
            throw new NotExistsPojoException("Tag not found: " + dto.getTagName(), e);
        }
        catch (final AlreadyExistsIovException e) {
            throw new AlreadyExistsIovException("Iov already exists: " + dto.toString(), e);
        }
        catch (RuntimeException e) {
            throw new CdbServiceException("Service runtime exception in saveIovAndPayload: ", e);
        }
        finally {
            log.debug("Clean up files when non null...");
            try {
                if (filename != null) {
                    Files.deleteIfExists(Paths.get(filename));
                }
            }
            catch (IOException e) {
                log.error("Cannot delete temporary file: {}", e.getMessage());
            }
            log.debug("Removed temporary file");
        }
    }

    /**
     *
     * @param filename
     * @param pdto
     * @param dto
     * @return PayloadDto
     * @throws IOException
     */
    protected PayloadDto insertPayloadFromFile(String filename, PayloadDto pdto, IovDto dto) throws IOException {
        try (InputStream is = new FileInputStream(filename);
             FileChannel tempchan = FileChannel.open(Paths.get(filename));) {
            pdto.size((int) tempchan.size());
            return insertPayloadAndInputStream(pdto, is);
        }
        catch (final HashExistsException e) {
            log.warn("Payload hash duplication, will not store : {}", dto.getPayloadHash());
            return getPayloadMetaInfo(dto.getPayloadHash());
        }
    }

    /**
     *
     * @param pdto
     * @param dto
     * @return PayloadDto
     */
    protected PayloadDto insertPayloadFromDto(PayloadDto pdto, IovDto dto) {
        try {
            pdto.size(pdto.getData().length);
            return insertPayload(pdto);
        }
        catch (final HashExistsException e) {
            log.warn("Payload hash duplication, will not store : {}", dto.getPayloadHash());
            return getPayloadMetaInfo(dto.getPayloadHash());
        }
    }
}
