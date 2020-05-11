/**
 *
 */
package hep.crest.server.runinfo.services;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.querydsl.core.types.Predicate;

import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.runinfo.pojo.RunLumiInfo;
import hep.crest.data.runinfo.repositories.RunLumiInfoRepository;
import hep.crest.swagger.model.RunLumiInfoDto;
import ma.glasnost.orika.MapperFacade;

/**
 * @author formica
 *
 */
@Service
public class RunLumiInfoService {

    /**
     * Logger.
     */
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Repository.
     */
    @Autowired
    private RunLumiInfoRepository runlumiRepository;

    /**
     * Mapper.
     */
    @Autowired
    @Qualifier("mapper")
    private MapperFacade mapper;

    /**
     * @param qry
     *            the Predicate
     * @param req
     *            the Pageable
     * @return List<RunLumiInfoDto>
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    public List<RunLumiInfoDto> findAllRunLumiInfo(Predicate qry, Pageable req)
            throws CdbServiceException {
        try {
            Iterable<RunLumiInfo> entitylist = null;
            if (qry == null) {
                entitylist = runlumiRepository.findAll(req);
            }
            else {
                entitylist = runlumiRepository.findAll(qry, req);
            }
            return StreamSupport.stream(entitylist.spliterator(), false)
                    .map(s -> mapper.map(s, RunLumiInfoDto.class)).collect(Collectors.toList());
        }
        catch (final Exception e) {
            log.error("Exception in retrieving iov list using predicate and pagination...");
            throw new CdbServiceException(
                    "Cannot find all iovs using predicate and pagination: " + e.getMessage());
        }
    }

    /**
     * @param dto
     *            the RunLumiInfoDto
     * @return RunLumiInfoDto
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    @Transactional
    public RunLumiInfoDto insertRunLumiInfo(RunLumiInfoDto dto) throws CdbServiceException {
        try {
            log.debug("Create runlumiinfo from dto {}", dto);
            final RunLumiInfo entity = mapper.map(dto, RunLumiInfo.class);
            final RunLumiInfo saved = runlumiRepository.save(entity);
            log.debug("Saved entity: {}", saved);
            return mapper.map(saved, RunLumiInfoDto.class);
        }
        catch (final Exception e) {
            log.error("Exception in storing runlumi {}", dto);
            throw new CdbServiceException("Cannot store runlumi : " + e.getMessage());
        }
    }

}