/**
 *
 */
package hep.crest.server.services;

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
import hep.crest.server.controllers.PageRequestHelper;
import hep.crest.swagger.model.GroupDto;
import hep.crest.swagger.model.IovDto;
import hep.crest.swagger.model.TagSummaryDto;
import ma.glasnost.orika.MapperFacade;

/**
 * @author formica
 *
 */
@Service
public class IovService {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private IovRepository iovRepository;
	@Autowired
	private TagRepository tagRepository;
	@Autowired
	@Qualifier("payloaddatadbrepo")
	private PayloadDataBaseCustom payloaddataRepository;
	
	@Autowired
	private IovGroupsCustom iovgroupsrepo;
	
	@Autowired
	@Qualifier("mapper")
	private MapperFacade mapper;

	public List<IovDto> findAllIovsByTagName(String tagname) throws CdbServiceException {
		try {
			log.debug("Search for iovs by tag name "+tagname);
			List<Iov> entitylist =  iovRepository.findByIdTagName(tagname);
			List<IovDto> dtolist = new ArrayList<>();
			dtolist = entitylist.stream().map(s -> mapper.map(s,IovDto.class)).collect(Collectors.toList());
			return dtolist;
		} catch (Exception e) {
			log.debug("Exception in retrieving iov list using IdByTagName expression..."+tagname);
			throw new CdbServiceException("Cannot find iovs by tag name: " + e.getMessage());
		}
	}
	
	public IovDto latest(String tagname) throws CdbServiceException {
		try {
			log.debug("Search for latest iovs by tag name "+tagname+ " using pagination query");
			PageRequestHelper prh = new PageRequestHelper();
			PageRequest req = prh.createPageRequest(0, 1, "stime:DESC");
			List<IovDto> iovlist =  this.findAllIovsByTagName(tagname, req);
			if (iovlist.size()<=0) {
				return null;
			}
			return iovlist.get(0);
		} catch (Exception e) {
			log.debug("Exception in retrieving iov list using IdByTagName expression and pagination..."+tagname);
			throw new CdbServiceException("Cannot find iovs by tag name and pagination: " + e.getMessage());
		}
	}
	
	public List<IovDto> findAllIovsByTagName(String tagname, Pageable req) throws CdbServiceException {
		try {
			log.debug("Search for iovs by tag name "+tagname+ " using pagination request");
			List<Iov> entitylist =  iovRepository.findByIdTagName(tagname,req);
			List<IovDto> dtolist = new ArrayList<>();
			dtolist = entitylist.stream().map(s -> mapper.map(s,IovDto.class)).collect(Collectors.toList());
			return dtolist;
		} catch (Exception e) {
			log.debug("Exception in retrieving iov list using IdByTagName expression and pagination..."+tagname);
			throw new CdbServiceException("Cannot find iovs by tag name and pagination: " + e.getMessage());
		}
	}
	
	/**
	 * @return
	 * @throws ConddbServiceException
	 */
	@SuppressWarnings("unchecked")
	public List<IovDto> findAllIovs(Predicate qry, Pageable req) throws CdbServiceException {
		try {
			List<IovDto> dtolist = new ArrayList<>();
			Iterable<Iov> entitylist = null;
			if (qry == null) {
				entitylist = iovRepository.findAll(req);
			} else {
				entitylist = iovRepository.findAll(qry, req);
			}
			dtolist = StreamSupport.stream(entitylist.spliterator(), false).map(s -> mapper.map(s,IovDto.class)).collect(Collectors.toList());
			return dtolist;
		} catch (Exception e) {
			log.debug("Exception in retrieving iov list using predicate and pagination...");
			throw new CdbServiceException("Cannot find all iovs using predicate and pagination: " + e.getMessage());
		}
	}

		
	public List<BigDecimal> selectGroupsByTagNameAndSnapshotTime(String tagname, Date snapshot, Integer groupsize) throws CdbServiceException {
		try {
			log.debug("Search for iovs groups by tag name "+tagname+" and snapshot time "+snapshot);
			List<BigDecimal> minsincelist = null;
			if (snapshot == null) {
				minsincelist =  iovgroupsrepo.selectGroups(tagname,groupsize);
			} else {
				minsincelist =  iovgroupsrepo.selectSnapshotGroups(tagname, snapshot, groupsize);
			}
			
			return minsincelist;
		} catch (Exception e) {
			log.debug("Exception in retrieving iov groups list using "+tagname);
			throw new CdbServiceException("Cannot find iov groups by tag name: " + e.getMessage());
		}
	}
	
	public GroupDto selectGroupDtoByTagNameAndSnapshotTime(String tagname, Date snapshot, Integer groupsize) throws CdbServiceException {
		try {
			List<BigDecimal> minsincelist = selectGroupsByTagNameAndSnapshotTime(tagname, snapshot, groupsize);
			return new GroupDto().groups(minsincelist);
		} catch (Exception e) {
			log.debug("Exception in retrieving iov groups list using "+tagname);
			throw new CdbServiceException("Cannot find iov groups by tag name: " + e.getMessage());
		}
	}
		
	@SuppressWarnings("unchecked")
	public List<IovDto> selectIovsByTagRangeSnapshot(String tagname, BigDecimal since, BigDecimal until,Date snapshot) throws CdbServiceException {
		try {
			log.debug("Search for iovs by tag name "+tagname+" and range time "+since+" -> "+until+" using snapshot "+snapshot);
			List<IovDto> dtolist = new ArrayList<>();
			Iterable<Iov> entities = null;
			if (snapshot == null) {
				entities =  iovRepository.selectLatestByGroup(tagname, since, until);
			} else {
				entities =  iovRepository.selectSnapshotByGroup(tagname, since, until, snapshot);
			}
			dtolist = StreamSupport.stream(entities.spliterator(), false).map(s -> mapper.map(s,IovDto.class)).collect(Collectors.toList());
			return dtolist;
		} catch (Exception e) {
			log.debug("Exception in retrieving iov groups list using "+tagname);
			throw new CdbServiceException("Cannot find iov size by tag name: " + e.getMessage());
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<IovDto> selectSnapshotByTag(String tagname, Date snapshot) throws CdbServiceException {
		try {
			log.debug("Search for snapshot by tag name "+tagname+" using snapshot "+snapshot);
			List<IovDto> dtolist = new ArrayList<>();
			Iterable<Iov> entities = null;
			entities = iovRepository.selectSnapshot(tagname, snapshot);
			dtolist = StreamSupport.stream(entities.spliterator(), false).map(s -> mapper.map(s,IovDto.class)).collect(Collectors.toList());
			return dtolist;
		} catch (Exception e) {
			log.debug("Exception in retrieving iov groups list using "+tagname);
			throw new CdbServiceException("Cannot find iov size by tag name: " + e.getMessage());
		}
	}

	public Long getSizeByTag(String tagname) throws CdbServiceException {
		try {
			return iovgroupsrepo.getSize(tagname);	
		} catch (Exception e) {
			log.debug("Exception in retrieving iov size using tag " + tagname);
			throw new CdbServiceException("Cannot find iov size by tag name: " + e.getMessage());
		}
	}
	
	public Long getSizeByTagAndSnapshot(String tagname, Date snapshot) throws CdbServiceException {
		try {
			return iovgroupsrepo.getSizeBySnapshot(tagname, snapshot);	
		} catch (Exception e) {
			log.debug("Exception in retrieving iov size using tag " + tagname + " and snapshot " + snapshot);
			throw new CdbServiceException("Cannot find iov size by tag name and snapshot: " + e.getMessage());
		}
	}

	public List<TagSummaryDto> getTagSummaryInfo(String tagname) throws CdbServiceException {
		try {
			return iovgroupsrepo.getTagSummaryInfo(tagname);	
		} catch (Exception e) {
			log.debug("Exception in retrieving iov summary information for tag matchin " + tagname );
			throw new CdbServiceException("Cannot find niovs by tag name: " + e.getMessage());
		}
	}
	
	@Transactional
	public IovDto insertIov(IovDto dto) throws CdbServiceException {
		try {
			log.debug("Create iov from dto " + dto);
			Iov entity =  mapper.map(dto,Iov.class);
			log.debug("Verify if the same IOV is already stored with the same hash....");
			Iov tmpiov = iovRepository.findBySinceAndTagNameAndHash(entity.getId().getTagName(), entity.getId().getSince(), entity.getPayloadHash());
			if (tmpiov != null) {
				log.debug("Found iov with the same Id and Hash...skip insertion....");
				return  mapper.map(tmpiov,IovDto.class);
			}
			Tag tg = tagRepository.findOne(dto.getTagName());
			if (tg == null) {
				throw new CdbServiceException("insertIov: Cannot find tag "+dto.getTagName()+" in the database...");
			}
			tg.setModificationTime(null);
			Tag updtag = tagRepository.save(tg);
			entity.setTag(updtag);
			Iov saved = iovRepository.save(entity);
			log.debug("Saved entity: " + saved);
			IovDto dtoentity = mapper.map(saved,IovDto.class);
			return dtoentity;

		} catch (Exception e) {
			log.debug("Exception in storing iov " + dto);
			throw new CdbServiceException("Cannot store iov : " + e.getMessage());
		}
	}

}
