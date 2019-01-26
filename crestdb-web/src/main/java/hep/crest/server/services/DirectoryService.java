/**
 * 
 */
package hep.crest.server.services;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import hep.crest.data.config.CrestProperties;
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
	
	@Autowired
	private CrestProperties cprops;

	/**
	 * @param tagname
	 * @return
	 */
	public TagDto getTag(String tagname) {
		return fstagrepository.findOne(tagname);
	}
	
	/**
	 * @param tagname
	 * @return
	 */
	public List<IovDto> listIovs(String tagname) {
		try {
			return fsiovrepository.findByTagName(tagname);
		} catch (CdbServiceException e) {
			log.error("Cannot find iov list for tag {}: {}",tagname,e.getMessage());
		}
		return new ArrayList<>();
	}
	
	/**
	 * @param hash
	 * @return
	 */
	public PayloadDto getPayload(String hash) {
		try {
			return fspayloadrepository.find(hash);
		} catch (CdbServiceException e) {
			log.error("Cannot find payload for hash {} : {}",hash,e.getMessage());
		}
		return null;
	}
	
	/**
	 * @param tagname
	 * @param snapshot
	 * @param path
	 * @return
	 */
	@Async
	public Future<String> dumpTag(String tagname, Date snapshot, String path) {
		String threadname = Thread.currentThread().getName();
		log.debug("Running task in asynchronous mode for name {}",threadname);
		String outdir = cprops.getDumpdir()+File.separator+path;
		log.debug("Output directory is {}",outdir);

		DirectoryUtilities du = new DirectoryUtilities(outdir);
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
			String tarpath = cprops.getWebstaticdir()+File.separator+path;
			String outtar = du.createTarFile(outdir,tarpath);
			log.debug("Created output tar file {}",outtar);
			return new AsyncResult<>("Dump a list of "+iovlist.size()+" iovs into file system...");
		} catch (CdbServiceException e) {
			log.error("Cannot dump tag {} in path {} : {}",tagname,path,e.getMessage());
		}
		return null;
	}
	
}
