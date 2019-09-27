/**
 *
 */
package hep.crest.server.services;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
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
import hep.crest.server.annotations.ProfileAndLog;
import hep.crest.server.controllers.PageRequestHelper;
import hep.crest.server.exceptions.AlreadyExistsPojoException;
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

	/**
	 * @param tagname
	 * @return
	 * @throws CdbServiceException
	 */
	public List<IovDto> findAllIovsByTagName(String tagname) throws CdbServiceException {
		try {
			log.debug("Search for iovs by tag name {}", tagname);
			Tag atag = tagRepository.findByName(tagname);
			List<Iov> entitylist =  iovRepository.findByIdTagid(atag.getTagid());
			return entitylist.stream().map(s -> mapper.map(s,IovDto.class)).collect(Collectors.toList());
		} catch (Exception e) {
			log.error("Exception in retrieving iov list using IdByTagName expression...{}",tagname);
			throw new CdbServiceException("Cannot find iovs by tag name: " + e.getMessage());
		}
	}
	
	/**
	 * @param tagname
	 * @return
	 * @throws CdbServiceException
	 */
	public IovDto latest(String tagname) throws CdbServiceException {
		try {
			log.debug("Search for latest iovs by tag name {} using pagination query",tagname);
			PageRequestHelper prh = new PageRequestHelper();
			PageRequest req = prh.createPageRequest(0, 1, "stime:DESC");
			List<IovDto> iovlist =  this.findAllIovsByTagName(tagname, req);
			if (iovlist.isEmpty()) {
				return null;
			}
			return iovlist.get(0);
		} catch (Exception e) {
			log.debug("Exception in retrieving iov list using IdByTagName expression and pagination...{}", tagname);
			throw new CdbServiceException("Cannot find iovs by tag name and pagination: " + e.getMessage());
		}
	}
	
	/**
	 * @param tagname
	 * @param req
	 * @return
	 * @throws CdbServiceException
	 */
	public List<IovDto> findAllIovsByTagName(String tagname, Pageable req) throws CdbServiceException {
		try {
			log.debug("Search for iovs by tag name {} using pagination request",tagname);
			Tag atag = tagRepository.findByName(tagname);
			Page<Iov> entitylist =  iovRepository.findByIdTagid(atag.getTagid(),req);
			return entitylist.stream().map(s -> mapper.map(s,IovDto.class)).collect(Collectors.toList());
		} catch (Exception e) {
			log.debug("Exception in retrieving iov list using IdByTagName expression and pagination...{}", tagname);
			throw new CdbServiceException("Cannot find iovs by tag name and pagination: " + e.getMessage());
		}
	}
		
	/**
	 * @return
	 * @throws ConddbServiceException
	 */
	public List<IovDto> findAllIovs(Tag tag, Predicate qry, Pageable req) throws CdbServiceException {
		try {
			Iterable<Iov> entitylist = null;
			if (qry == null) {
				entitylist = iovRepository.findAll(req);
			} else {
				entitylist = iovRepository.findAll(qry, req);
			}
			return StreamSupport.stream(entitylist.spliterator(), false).map(i -> {i.setTag(tag); return i;}).map(s -> mapper.map(s,IovDto.class)).collect(Collectors.toList());
		} catch (Exception e) {
			log.error("Exception in retrieving iov list using predicate and pagination...");
			throw new CdbServiceException("Cannot find all iovs using predicate and pagination: " + e.getMessage());
		}
	}

		
	/**
	 * @param tagname
	 * @param snapshot
	 * @param groupsize
	 * @return
	 * @throws CdbServiceException
	 */
	public List<BigDecimal> selectGroupsByTagNameAndSnapshotTime(String tagname, Date snapshot, Long groupsize) throws CdbServiceException {
		try {
			log.debug("Search for iovs groups by tag name {} and snapshot time {}",tagname,snapshot);
			List<BigDecimal> minsincelist = null;
			Tag atag = tagRepository.findByName(tagname);
			if (snapshot == null) {
				minsincelist =  iovgroupsrepo.selectGroups(atag.getTagid(),groupsize);
			} else {
				minsincelist =  iovgroupsrepo.selectSnapshotGroups(atag.getTagid(), snapshot, groupsize);
			}
			
			return minsincelist;
		} catch (Exception e) {
			log.error("Exception in retrieving iov groups list using tag {} and group size {}", tagname,groupsize);
			throw new CdbServiceException("Cannot find iov groups by tag name: " + e.getMessage());
		}
	}
	
	/**
	 * @param tagname
	 * @param snapshot
	 * @param groupsize
	 * @return
	 * @throws CdbServiceException
	 */
	@ProfileAndLog
	public GroupDto selectGroupDtoByTagNameAndSnapshotTime(String tagname, Date snapshot, Long groupsize) throws CdbServiceException {
		try {
			List<BigDecimal> minsincelist = selectGroupsByTagNameAndSnapshotTime(tagname, snapshot, groupsize);
			return new GroupDto().groups(minsincelist);
		} catch (Exception e) {
			log.error("Exception in retrieving iov groups list using tag and snapshot {}", tagname);
			throw new CdbServiceException("Cannot find iov groups by tag name: " + e.getMessage());
		}
	}
		
	/**
	 * @param tagname
	 * @param since
	 * @param until
	 * @param snapshot
	 * @return
	 * @throws CdbServiceException
	 */
	public List<IovDto> selectIovsByTagRangeSnapshot(String tagname, BigDecimal since, BigDecimal until,Date snapshot) throws CdbServiceException {
		try {
			log.debug("Search for iovs by tag name {}  and range time {} -> {} using snapshot {}",tagname,since,until,snapshot);
			Iterable<Iov> entities = null;
			if (snapshot == null) {
				entities =  iovRepository.selectLatestByGroup(tagname, since, until);
			} else {
				entities =  iovRepository.selectSnapshotByGroup(tagname, since, until, snapshot);
			}
			return StreamSupport.stream(entities.spliterator(), false).map(s -> mapper.map(s,IovDto.class)).collect(Collectors.toList());
		} catch (Exception e) {
			log.debug("Exception in retrieving iov list using tag {} and snapshot and time range",tagname);
			throw new CdbServiceException("Cannot find iov size by tag name: " + e.getMessage());
		}
	}
	
	/**
	 * @param tagname
	 * @param snapshot
	 * @return
	 * @throws CdbServiceException
	 */
	public List<IovDto> selectSnapshotByTag(String tagname, Date snapshot) throws CdbServiceException {
		try {
			log.debug("Search for snapshot by tag name {} using snapshot {}",tagname,snapshot);
			Iterable<Iov> entities = null;
			entities = iovRepository.selectSnapshot(tagname, snapshot);
			return StreamSupport.stream(entities.spliterator(), false).map(s -> mapper.map(s,IovDto.class)).collect(Collectors.toList());
		} catch (Exception e) {
			log.error("Exception in retrieving iov list by tag using {}", tagname);
			throw new CdbServiceException("Cannot find iov size by tag name and snapshot: " + e.getMessage());
		}
	}

	/**
	 * @param tagname
	 * @return
	 * @throws CdbServiceException
	 */
	public Long getSizeByTag(String tagname) throws CdbServiceException {
		try {
			Tag atag = tagRepository.findByName(tagname);
			return iovgroupsrepo.getSize(atag.getTagid());	
		} catch (Exception e) {
			log.error("Exception in retrieving iov size using tag {}", tagname);
			throw new CdbServiceException("Cannot find iov size by tag name: " + e.getMessage());
		}
	}
	
	/**
	 * @param tagname
	 * @param snapshot
	 * @return
	 * @throws CdbServiceException
	 */
	public Long getSizeByTagAndSnapshot(String tagname, Date snapshot) throws CdbServiceException {
		try {
			Tag atag = tagRepository.findByName(tagname);
			return iovgroupsrepo.getSizeBySnapshot(atag.getTagid(), snapshot);	
		} catch (Exception e) {
			log.debug("Exception in retrieving iov size using tag {} and snapshot {}",tagname,snapshot);
			throw new CdbServiceException("Cannot find iov size by tag name and snapshot: " + e.getMessage());
		}
	}

	/**
	 * @param tagname
	 * @return
	 * @throws CdbServiceException
	 */
	public List<TagSummaryDto> getTagSummaryInfo(String tagname) throws CdbServiceException {
		try {
			return iovgroupsrepo.getTagSummaryInfo(tagname);	
		} catch (Exception e) {
			log.error("Exception in retrieving iov summary information for tag matching {}",tagname);
			throw new CdbServiceException("Cannot find niovs by tag name: " + e.getMessage());
		}
	}
	
	/**
	 * @param dto
	 * @return
	 * @throws CdbServiceException
	 */
	@Transactional
	public IovDto insertIov(IovDto dto) throws CdbServiceException {
		log.debug("Create iov from dto {}", dto);
		Iov tmpiov = null;
		Iov entity = null;
		Tag atag = null;
		try {
			atag = tagRepository.findByName(dto.getTagName());
			if (atag == null) {
				throw new CdbServiceException("Unkown tag : " +dto.getTagName());
			}
			log.debug("Found tag entity {} to store iov",atag);
		} catch (Exception e) {
			throw new CdbServiceException("Unkown tag : " +dto.getTagName());		
		}
		try {
			entity =  mapper.map(dto,Iov.class);
			log.debug("Verify if the same IOV is already stored with the same hash....");
			tmpiov = iovRepository.findBySinceAndTagidAndHash(atag.getTagid(), entity.getId().getSince(), entity.getPayloadHash());
		} catch (Exception e) {
			log.warn("Searching iov {} has not found anything...",dto);
		}
		if (entity == null) {
			throw new CdbServiceException("Cannot map entity to dto "+dto); 
		}
		if (tmpiov != null) {
			log.debug("Found iov with the same Id and Hash...skip insertion....");
			throw new AlreadyExistsPojoException(tmpiov.toString());
		}
		try {
			// The IOV is not yet stored. Verify that the tag exists before inserting it.
	    	atag.setModificationTime(null);
			Tag updtag = tagRepository.save(atag);
			log.debug("Update tag for modification time {} ",updtag);
			entity.setTag(updtag);
			entity.getId().setTagid(updtag.getTagid());
			Iov saved = iovRepository.save(entity);
			log.debug("Saved entity: {}", saved);
			IovDto dtoentity = mapper.map(saved,IovDto.class);
			dtoentity.setTagName(atag.getName());
			log.debug("Returning iovDto: {}", dtoentity);
			return dtoentity;
			

		} catch (Exception e) {
			log.error("Exception in storing iov {}", dto);
			throw new CdbServiceException("Cannot store iov : " + e.getMessage());
		}
	}

}
