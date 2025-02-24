/**
 *
 */
package hep.crest.server.services;

import hep.crest.server.controllers.PageRequestHelper;
import hep.crest.server.converters.RunLumiMapper;
import hep.crest.server.data.runinfo.pojo.RunLumiInfo;
import hep.crest.server.data.runinfo.repositories.RunLumiInfoRepository;
import hep.crest.server.exceptions.AbstractCdbServiceException;
import hep.crest.server.exceptions.CdbNotFoundException;
import hep.crest.server.swagger.model.RunLumiInfoDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.math.BigInteger;
import java.util.Date;

/**
 * @author formica
 */
@Service
@Slf4j
public class RunInfoService {

    /**
     * Repository.
     */
    private RunLumiInfoRepository runinfoRepository;
    /**
     * Mapper.
     */
    private RunLumiMapper mapper;
    /**
     * Helper.
     */
    private PageRequestHelper prh;

    /**
     * Ctors with injected repository.
     * @param runinfoRepository the repository
     * @param mapper the RunLumi mapper
     * @param prh the PageRequestHelper
     */
    @Autowired
    public RunInfoService(RunLumiInfoRepository runinfoRepository,
                          RunLumiMapper mapper, PageRequestHelper prh) {
        this.runinfoRepository = runinfoRepository;
        this.mapper = mapper;
        this.prh = prh;
    }

    /**
     * @param dto the RunInfoDto
     * @return RunLumiInfoDto
     * @throws AbstractCdbServiceException If an Exception occurred
     */
    @Transactional
    public RunLumiInfoDto insertRunInfo(RunLumiInfoDto dto) throws AbstractCdbServiceException {
        log.debug("Create runinfo from dto {}", dto);
        final RunLumiInfo entity = mapper.toEntity(dto);
        final RunLumiInfo saved = runinfoRepository.save(entity);
        log.debug("Saved entity: {}", saved);
        return mapper.toDto(saved);
    }

    /**
     * Can update the starttime or endtime fields of a RunLumi entity.
     *
     * @param dto
     * @return RunLumiInfoDto
     */
    @Transactional
    public RunLumiInfoDto updateRunInfo(RunLumiInfoDto dto) throws AbstractCdbServiceException {
        log.debug("Update runinfo from dto {}", dto);
        final RunLumiInfo entity = mapper.toEntity(dto);
        RunLumiInfo dbentry = runinfoRepository.findById(entity.getId());
        if (dbentry == null) {
            log.error("Cannot find runinfo with id {}", entity.getId());
            throw new CdbNotFoundException("Cannot find runinfo with id " + entity.getId());
        }
        if (!entity.getStarttime().equals(dbentry.getStarttime())) {
            dbentry.setStarttime(entity.getStarttime());
        }
        if (!entity.getEndtime().equals(dbentry.getEndtime())) {
            dbentry.setEndtime(entity.getEndtime());
        }
        log.debug("Update runinfo with id {}", entity.getId());
        final RunLumiInfo saved = runinfoRepository.save(dbentry);
        log.debug("Saved entity: {}", saved);
        return mapper.toDto(saved);
    }

    /**
     * @param from the BigInteger.
     * @param to   the BigInteger.
     * @param preq the PageRequest
     * @return List<RunLumiInfo>
     * @throws AbstractCdbServiceException If an Exception occurred.
     */
    public Page<RunLumiInfo> selectInclusiveByRun(BigInteger from, BigInteger to, Pageable preq) {
        Page<RunLumiInfo> entitylist = null;
        if (preq == null) {
            String sort = "id.runNumber:ASC";
            preq = prh.createPageRequest(0, 1000, sort);
        }
        log.debug("Search runinfo list using from={}, to={}", from, to);
        entitylist = runinfoRepository.findByRunNumberInclusive(from, to, preq);
        log.trace("Retrieved list of runs {}", entitylist.getNumberOfElements());
        return entitylist;
    }


    /**
     * @param run  the BigInteger
     * @param from the BigInteger.
     * @param to   the BigInteger.
     * @param preq the PageRequest
     * @return List<RunLumiInfo>
     * @throws AbstractCdbServiceException If an Exception occurred.
     */
    public Page<RunLumiInfo> selectInclusiveByLumiBlock(BigInteger run, BigInteger from,
                                                        BigInteger to,
                                                        Pageable preq) {
        Page<RunLumiInfo> entitylist = null;
        if (preq == null) {
            String sort = "id.runNumber:ASC,id.lb:ASC";
            preq = prh.createPageRequest(0, 1000, sort);
        }
        entitylist = runinfoRepository.findByLumiBlockInclusive(run, from, to, preq);
        log.trace("Retrieved list of runs {}", entitylist.getNumberOfElements());
        return entitylist;
    }

    /**
     * @param from the Date.
     * @param to   the Date.
     * @param preq the PageRequest
     * @return Page<RunLumiInfo>
     * @throws AbstractCdbServiceException If an Exception occurred.
     */
    public Page<RunLumiInfo> selectInclusiveByDate(Date from, Date to, Pageable preq) {
        Page<RunLumiInfo> entitylist = null;
        if (preq == null) {
            String sort = "id.runNumber:ASC";
            preq = prh.createPageRequest(0, 1000, sort);
        }
        entitylist = runinfoRepository.findByDateInclusive(BigInteger.valueOf(from.getTime()),
                BigInteger.valueOf(to.getTime()), preq);
        log.trace("Retrieved list of runs {}", entitylist.getNumberOfElements());
        return entitylist;
    }
}
