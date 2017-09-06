/**
 * 
 */
package hep.crest.server.services;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.repositories.IovDirectoryImplementation;
import hep.crest.data.repositories.PayloadDirectoryImplementation;
import hep.crest.data.repositories.TagDirectoryImplementation;
import hep.crest.data.utils.DirectoryUtilities;
import hep.crest.swagger.model.IovDto;
import hep.crest.swagger.model.PayloadDto;
import hep.crest.swagger.model.TagDto;

/**
 * @author formica
 *
 */
@Service
public class DirectoryService {

	private static final Logger log = LoggerFactory.getLogger(DirectoryService.class);

	@Autowired
	private TagDirectoryImplementation fstagrepository;
	@Autowired
	private IovDirectoryImplementation fsiovrepository;
	@Autowired
	private PayloadDirectoryImplementation fspayloadrepository;
	
	@Autowired
	private IovService iovservice;
	@Autowired
	private TagService tagservice;
	@Autowired
	private PayloadService pyldservice;
	
	public TagDto getTag(String tagname) {
		return fstagrepository.findOne(tagname);
	}
	
	public List<IovDto> listIovs(String tagname) {
		try {
			return fsiovrepository.findByTagName(tagname);
		} catch (CdbServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public PayloadDto getPayload(String hash) {
		try {
			return fspayloadrepository.find(hash);
		} catch (CdbServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	@Async
	public Future<String> dumpTag(String tagname, Date snapshot, String path) {
		String threadname = Thread.currentThread().getName();
		log.debug("Running task in asynchronous mode for name "+threadname);
		DirectoryUtilities du = new DirectoryUtilities("/tmp/cdms/"+path);
		try {
			fstagrepository.setDirtools(du);
			fsiovrepository.setDirtools(du);
			fspayloadrepository.setDirtools(du);
			
			TagDto seltag = tagservice.findOne(tagname);
			List<IovDto> iovlist = iovservice.selectSnapshotByTag(tagname, snapshot);
			fstagrepository.save(seltag);
			fsiovrepository.saveAll(tagname, iovlist);
			for (IovDto iovDto : iovlist) {
				PayloadDto pyld = pyldservice.getPayload(iovDto.getPayloadHash());
				fspayloadrepository.save(pyld);
			}
			return new AsyncResult<String>("Dump a list of "+iovlist.size()+" iovs into file system...");
		} catch (CdbServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
