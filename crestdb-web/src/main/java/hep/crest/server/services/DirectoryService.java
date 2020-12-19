/**
 * 
 */
package hep.crest.server.services;

import hep.crest.data.config.CrestProperties;
import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.pojo.Iov;
import hep.crest.data.pojo.Tag;
import hep.crest.data.repositories.IovDirectoryImplementation;
import hep.crest.data.repositories.PayloadDirectoryImplementation;
import hep.crest.data.repositories.TagDirectoryImplementation;
import hep.crest.data.utils.DirectoryUtilities;
import hep.crest.server.controllers.EntityDtoHelper;
import hep.crest.server.exceptions.NotExistsPojoException;
import hep.crest.swagger.model.IovDto;
import hep.crest.swagger.model.PayloadDto;
import hep.crest.swagger.model.TagDto;
import ma.glasnost.orika.MapperFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

/**
 * An implementation for filesystem based storage.
 *
 * @author formica
 *
 */
@Service
public class DirectoryService {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(DirectoryService.class);

    /**
     * Repository.
     */
    @Autowired
    @Qualifier("fstagrepository")
    private TagDirectoryImplementation fstagrepository;
    /**
     * Repository.
     */
    @Autowired
    @Qualifier("fsiovrepository")
    private IovDirectoryImplementation fsiovrepository;
    /**
     * Repository.
     */
    @Autowired
    @Qualifier("fspayloadrepository")
    private PayloadDirectoryImplementation fspayloadrepository;

    /**
     * Helper.
     */
    @Autowired
    private EntityDtoHelper edh;
    /**
     * Service.
     */
    @Autowired
    private IovService iovservice;
    /**
     * Service.
     */
    @Autowired
    private TagService tagservice;
    /**
     * Service.
     */
    @Autowired
    private PayloadService pyldservice;

    /**
     * Properties.
     */
    @Autowired
    private CrestProperties cprops;

    /**
     * Mapper.
     */
    @Autowired
    @Qualifier("mapper")
    private MapperFacade mapper;

    /**
     * @param tagname
     *            the String
     * @return TagDto
     */
    public TagDto getTag(String tagname) {
        return fstagrepository.findOne(tagname);
    }

    /**
     * @param dto
     *            the TagDto
     * @return TagDto or null.
     */
    public TagDto insertTag(TagDto dto) {
        return fstagrepository.save(dto);
    }

    /**
     * @param tagname
     *            the String
     * @return List<IovDto>
     */
    public List<IovDto> listIovs(String tagname) {
        try {
            return fsiovrepository.findByTagName(tagname);
        }
        catch (final CdbServiceException e) {
            log.error("Cannot find iov list for tag {}: {}", tagname, e.getMessage());
        }
        return new ArrayList<>();
    }

    /**
     * @param hash
     *            the String
     * @return PayloadDto
     */
    public PayloadDto getPayload(String hash) {
        try {
            return fspayloadrepository.find(hash);
        }
        catch (final CdbServiceException e) {
            log.error("Cannot find payload for hash {} : {}", hash, e.getMessage());
        }
        return null;
    }

    /**
     * @param tagname
     *            the String
     * @param snapshot
     *            the Date
     * @param path
     *            the String
     * @return Future<String>
     */
    @Async
    public Future<String> dumpTag(String tagname, Date snapshot, String path) {
        final String threadname = Thread.currentThread().getName();
        log.debug("Running task in asynchronous mode for name {}", threadname);
        final String outdir = cprops.getDumpdir() + File.separator + path;
        log.debug("Output directory is {}", outdir);

        final DirectoryUtilities du = new DirectoryUtilities(outdir);
        try {
            fstagrepository.setDirtools(du);
            fsiovrepository.setDirtools(du);
            fspayloadrepository.setDirtools(du);

            final Tag seltag = tagservice.findOne(tagname);
            final Iterable<Iov> iovlist = iovservice.selectSnapshotByTag(tagname, snapshot);
            final TagDto dto = mapper.map(seltag, TagDto.class);
            final List<IovDto> dtolist = edh.entityToDtoList(iovlist, IovDto.class);
            fstagrepository.save(dto);
            fsiovrepository.saveAll(tagname, dtolist);
            for (final IovDto iovDto : dtolist) {
                final PayloadDto pyld = pyldservice.getPayload(iovDto.getPayloadHash());
                fspayloadrepository.save(pyld);
            }
            final String tarpath = cprops.getWebstaticdir() + File.separator + path;
            final String outtar = du.createTarFile(outdir, tarpath);
            log.debug("Created output tar file {}", outtar);
            return new AsyncResult<>(
                    "Dump a list of " + dtolist.size() + " iovs into file system...");
        }
        catch (final NotExistsPojoException e) {
            log.error("Cannot find tag or payload  : {}", e.getMessage());
        }
        catch (final CdbServiceException e) {
            log.error("Server exception, cannot dump tag {} in path {} : {}", tagname, path, e.getMessage());
        }
        return null;
    }
}
