/**
 *
 */
package hep.crest.server.runinfo.services;

import com.querydsl.core.types.Predicate;
import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.runinfo.pojo.RunLumiInfo;
import hep.crest.data.runinfo.repositories.RunLumiInfoRepository;
import hep.crest.swagger.model.RunLumiInfoDto;
import ma.glasnost.orika.MapperFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author formica
 *
 */
@Service
public class RunInfoService {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(RunInfoService.class);

    /**
     * Repository.
     */
    @Autowired
    private RunLumiInfoRepository runinfoRepository;

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
    public List<RunLumiInfoDto> findAllRunInfo(Predicate qry, Pageable req) throws CdbServiceException {
        Iterable<RunLumiInfo> entitylist = null;
        if (qry == null) {
            entitylist = runinfoRepository.findAll(req);
        }
        else {
            entitylist = runinfoRepository.findAll(qry, req);
        }

        return StreamSupport.stream(entitylist.spliterator(), false)
                .map(s -> mapper.map(s, RunLumiInfoDto.class)).collect(Collectors.toList());
    }

    /**
     * @param dto
     *            the RunInfoDto
     * @return RunLumiInfoDto
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    @Transactional
    public RunLumiInfoDto insertRunInfo(RunLumiInfoDto dto) throws CdbServiceException {
        log.debug("Create runinfo from dto {}", dto);
        final RunLumiInfo entity = mapper.map(dto, RunLumiInfo.class);
        final RunLumiInfo saved = runinfoRepository.save(entity);
        log.debug("Saved entity: {}", saved);
        return mapper.map(saved, RunLumiInfoDto.class);
    }

    /**
     * @param from
     *            the BigDecimal.
     * @param to
     *            the BigDecimal.
     * @throws CdbServiceException
     *             If an Exception occurred.
     * @return List<RunLumiInfoDto>
     */
    public List<RunLumiInfoDto> selectInclusiveByRun(BigDecimal from, BigDecimal to)
            throws CdbServiceException {
        List<RunLumiInfo> entitylist = null;
        entitylist = runinfoRepository.findByRunNumberInclusive(from, to);
        if (entitylist == null) {
            log.warn("Empty list for run information retrieved from selectInclusiveByRun {} {}",
                    from, to);
            return new ArrayList<>();
        }
        return StreamSupport.stream(entitylist.spliterator(), false)
                .map(s -> mapper.map(s, RunLumiInfoDto.class)).collect(Collectors.toList());

    }

    /**
     * @param from
     *            the Date.
     * @param to
     *            the Date.
     * @throws CdbServiceException
     *             If an Exception occurred.
     * @return List<RunLumiInfoDto>
     */
    public List<RunLumiInfoDto> selectInclusiveByDate(Date from, Date to) throws CdbServiceException {
        List<RunLumiInfo> entitylist = null;
        entitylist = runinfoRepository.findByDateInclusive(new BigDecimal(from.getTime()),
                new BigDecimal(to.getTime()));
        if (entitylist == null) {
            log.warn("Empty list for run information retrieved from selectInclusiveByDate {} {}",
                    from, to);
            return new ArrayList<>();
        }
        return StreamSupport.stream(entitylist.spliterator(), false)
                .map(s -> mapper.map(s, RunLumiInfoDto.class)).collect(Collectors.toList());

    }

}
