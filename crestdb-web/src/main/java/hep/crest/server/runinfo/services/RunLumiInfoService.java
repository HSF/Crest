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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.querydsl.core.types.Predicate;

import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.pojo.Iov;
import hep.crest.data.pojo.Tag;
import hep.crest.data.repositories.IovGroupsCustom;
import hep.crest.data.repositories.IovRepository;
import hep.crest.data.repositories.PayloadDataBaseCustom;
import hep.crest.data.repositories.TagRepository;
import hep.crest.data.runinfo.pojo.RunLumiInfo;
import hep.crest.data.runinfo.repositories.RunLumiInfoRepository;
import hep.crest.server.annotations.ProfileAndLog;
import hep.crest.server.controllers.PageRequestHelper;
import hep.crest.swagger.model.GroupDto;
import hep.crest.swagger.model.IovDto;
import hep.crest.swagger.model.RunLumiInfoDto;
import hep.crest.swagger.model.TagSummaryDto;
import ma.glasnost.orika.MapperFacade;

/**
 * @author formica
 *
 */
@Service
public class RunLumiInfoService {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private RunLumiInfoRepository runlumiRepository;
	
	@Autowired
	@Qualifier("mapper")
	private MapperFacade mapper;

	
	/**
	 * @return
	 * @throws ConddbServiceException
	 */
	@SuppressWarnings("unchecked")
	public List<RunLumiInfoDto> findAllRunLumiInfo(Predicate qry, Pageable req) throws CdbServiceException {
		try {
			List<RunLumiInfoDto> dtolist = new ArrayList<>();
			Iterable<RunLumiInfo> entitylist = null;
			if (qry == null) {
				entitylist = runlumiRepository.findAll(req);
			} else {
				entitylist = runlumiRepository.findAll(qry, req);
			}
			dtolist = StreamSupport.stream(entitylist.spliterator(), false).map(s -> mapper.map(s,RunLumiInfoDto.class)).collect(Collectors.toList());
			return dtolist;
		} catch (Exception e) {
			log.debug("Exception in retrieving iov list using predicate and pagination...");
			throw new CdbServiceException("Cannot find all iovs using predicate and pagination: " + e.getMessage());
		}
	}

		
	
	@Transactional
	public RunLumiInfoDto insertRunLumiInfo(RunLumiInfoDto dto) throws CdbServiceException {
		try {
			log.debug("Create runlumiinfo from dto " + dto);
			RunLumiInfo entity =  mapper.map(dto,RunLumiInfo.class);
			RunLumiInfo saved = runlumiRepository.save(entity);
			log.debug("Saved entity: " + saved);
			RunLumiInfoDto dtoentity = mapper.map(saved,RunLumiInfoDto.class);
			return dtoentity;

		} catch (Exception e) {
			log.debug("Exception in storing runlumi " + dto);
			throw new CdbServiceException("Cannot store runlumi : " + e.getMessage());
		}
	}

}
