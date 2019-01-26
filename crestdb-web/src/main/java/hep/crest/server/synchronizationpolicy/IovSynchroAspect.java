/**
 * 
 */
package hep.crest.server.synchronizationpolicy;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import hep.crest.data.config.CrestProperties;
import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.server.services.IovService;
import hep.crest.server.services.TagService;
import hep.crest.swagger.model.IovDto;
import hep.crest.swagger.model.TagDto;


/**
 * @author formica
 *
 */
@Aspect
@Component
public class IovSynchroAspect {
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private CrestProperties cprops;
	
	@Autowired
	private TagService tagService;
	@Autowired
	private IovService iovService;

	/**
	 * @param dto
	 */
	@Before("execution(* hep.crest.server.services.IovService.insertIov(*)) && args(dto)")
	public void checkSynchro(IovDto dto) {
		log.debug("Iov insertion should verify the tag synchronization type : {}", dto.getTagName());
		
		if (cprops.getSynchro().equals("none")) {
			log.warn("synchronization checks are disabled in this configuration....");
			return;
		}
		TagDto tagdto = null;
		try {
			tagdto = tagService.findOne(dto.getTagName());
		} catch (CdbServiceException e) {
			log.error("Error checking synchronization : {}",e.getMessage());
		}
		if (tagdto == null) {
			log.debug("Cannot find synchro for null tag");
			return;
		}
		String synchro = tagdto.getSynchronization();
		if (synchro.equalsIgnoreCase("SV")) {
			log.warn("Can only append IOVs....");
			IovDto latest = null;
			try {
				latest = iovService.latest(tagdto.getName());
			} catch (CdbServiceException e) {
				log.error("Error checking SV synchronization : {}",e.getMessage());
			}
			if (latest == null) {
				log.info("No iov could be retrieved");
			} else if (latest.getSince().compareTo(dto.getSince()) <= 0) {
				log.info("IOV in insert has correct time");
			} else {
				log.error("IOV in insert has wrong time....cannot insert");
			}
		} else {
			log.debug("Synchro type not found....");
		}
	}
}
