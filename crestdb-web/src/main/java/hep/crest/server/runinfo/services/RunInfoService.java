/**
 *
 */
package hep.crest.server.runinfo.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
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
import hep.crest.data.runinfo.pojo.RunInfo;
import hep.crest.data.runinfo.repositories.RunInfoRepository;
import hep.crest.swagger.model.RunInfoDto;
import ma.glasnost.orika.MapperFacade;

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
    private RunInfoRepository runinfoRepository;

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
     * @return List<RunInfoDto>
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    public List<RunInfoDto> findAllRunInfo(Predicate qry, Pageable req) throws CdbServiceException {
        Iterable<RunInfo> entitylist = null;
        if (qry == null) {
            entitylist = runinfoRepository.findAll(req);
        }
        else {
            entitylist = runinfoRepository.findAll(qry, req);
        }

        return StreamSupport.stream(entitylist.spliterator(), false)
                .map(s -> mapper.map(s, RunInfoDto.class)).collect(Collectors.toList());
    }

    /**
     * @param dto
     *            the RunInfoDto
     * @return RunInfoDto
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    @Transactional
    public RunInfoDto insertRunInfo(RunInfoDto dto) throws CdbServiceException {
        log.debug("Create runinfo from dto {}", dto);
        final RunInfo entity = mapper.map(dto, RunInfo.class);
        final RunInfo saved = runinfoRepository.save(entity);
        log.debug("Saved entity: {}", saved);
        return mapper.map(saved, RunInfoDto.class);
    }

    /**
     * @param from
     *            the BigDecimal.
     * @param to
     *            the BigDecimal.
     * @throws CdbServiceException
     *             If an Exception occurred.
     * @return List<RunInfoDto>
     */
    public List<RunInfoDto> selectInclusiveByRun(BigDecimal from, BigDecimal to)
            throws CdbServiceException {
        List<RunInfo> entitylist = null;
        entitylist = runinfoRepository.findByRunNumberInclusive(from, to);
        if (entitylist == null) {
            log.warn("Empty list for run information retrieved from selectInclusiveByRun {} {}",
                    from, to);
            return new ArrayList<>();
        }
        return StreamSupport.stream(entitylist.spliterator(), false)
                .map(s -> mapper.map(s, RunInfoDto.class)).collect(Collectors.toList());

    }

    /**
     * @param from
     *            the Date.
     * @param to
     *            the Date.
     * @throws CdbServiceException
     *             If an Exception occurred.
     * @return List<RunInfoDto>
     */
    public List<RunInfoDto> selectInclusiveByDate(Date from, Date to) throws CdbServiceException {
        List<RunInfo> entitylist = null;
        entitylist = runinfoRepository.findByDateInclusive(from, to);
        if (entitylist == null) {
            log.warn("Empty list for run information retrieved from selectInclusiveByDate {} {}",
                    from, to);
            return new ArrayList<>();
        }
        return StreamSupport.stream(entitylist.spliterator(), false)
                .map(s -> mapper.map(s, RunInfoDto.class)).collect(Collectors.toList());

    }

}
