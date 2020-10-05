/**
 *
 */
package hep.crest.server.services;

import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.pojo.Iov;
import hep.crest.data.pojo.Tag;
import hep.crest.data.repositories.PayloadDataBaseCustom;
import hep.crest.server.exceptions.NotExistsPojoException;
import hep.crest.swagger.model.HTTPResponse;
import hep.crest.swagger.model.IovDto;
import hep.crest.swagger.model.PayloadDto;
import ma.glasnost.orika.MapperFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.core.Response;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
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
        log.debug("Save payload dto {}", dto);
        if (dto.getSize() == null) {
            dto.setSize(dto.getData().length);
        }
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
    @Transactional
    public PayloadDto insertPayloadAndInputStream(PayloadDto dto, InputStream is)
            throws CdbServiceException {
        log.debug("Save payload {} creating blob from inputstream...", dto);
        final PayloadDto saved = payloaddataRepository.save(dto, is);
        log.debug("Saved entity: {}", saved);
        return saved;
    }


    @Transactional
    public HTTPResponse saveIovAndPayload(IovDto dto, PayloadDto pdto, String filename)
            throws CdbServiceException {
        log.debug("Create dto with hash {},  format {}, ...", dto.getPayloadHash(),
                pdto.getObjectType());
        Path temppath = null;
        try {
            PayloadDto saved = null;
            if (filename != null) {
                temppath = Paths.get(filename);
                try (InputStream is = new FileInputStream(filename);) {
                    final FileChannel tempchan = FileChannel.open(temppath);
                    pdto.size((int) tempchan.size());
                    tempchan.close();
                    saved = insertPayloadAndInputStream(pdto, is);
                }
                catch (IOException ex) {
                    log.error("IO Exception in reading payload data: {}", ex);
                    return new HTTPResponse().code(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                            .id(dto.getPayloadHash()).message("IO Exception in reading payload data for "
                                    + dto.getTagName());
                }
            }
            else {
                pdto.size(pdto.getData().length);
                saved = insertPayload(pdto);
            }
            String tagname = dto.getTagName();
            Iov entity = mapper.map(dto, Iov.class);
            entity.setTag(new Tag(tagname));
            final Iov savediov = iovService.insertIov(entity);
            IovDto saveddto = mapper.map(savediov, IovDto.class);
            dto.tagName(tagname);

            log.debug("Created payload {} and iov {} ", saved, savediov);
            return new HTTPResponse().code(Response.Status.CREATED.getStatusCode())
                    .id(savediov.getPayloadHash()).message("Iov created in tag "
                            + dto.getTagName() + " @ " + saveddto.getSince());
        }
        catch (final NotExistsPojoException e) {
            return new HTTPResponse().code(Response.Status.NOT_FOUND.getStatusCode())
                    .id(dto.getPayloadHash()).message("Tag not found "
                            + dto.getTagName());
        }
        catch (DataIntegrityViolationException e) {
            String msg = "Api method saveIovAndPayload got SQL exception " + e.getMessage();
            msg += "\nCannot store data in : " + dto.getTagName() + " @ " + dto.getSince();
            new HTTPResponse().code(Response.Status.CONFLICT.getStatusCode())
                    .id(dto.getPayloadHash()).message(msg);
        }
        catch (RuntimeException e) {
            log.error("A Runtime exception occurred in saveIovAndPayload method: {}", e);
        }
//        catch (RuntimeException e) {
//            String msg = "Api method saveIovAndPayload got exception " + e.getMessage();
//            msg += "\nCannot store data in : " + dto.getTagName() + " @ " + dto.getSince();
//            new HTTPResponse().code(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
//                    .id(dto.getPayloadHash()).message(msg);
//        }
        finally {
            try {
                Files.deleteIfExists(temppath);
            }
            catch (IOException e) {
                log.error("Cannot delete temporary file");
            }
            log.debug("Removed temporary file");
        }
        String msg = "Api method saveIovAndPayload error. ";
        msg += "\nCannot store data in : " + dto.getTagName() + " @ " + dto.getSince();
        return new HTTPResponse().code(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .id(dto.getPayloadHash()).message(msg);
    }
}
