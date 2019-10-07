/**
 *
 */
package hep.crest.server.services;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.pojo.GlobalTag;
import hep.crest.data.pojo.GlobalTagMap;
import hep.crest.data.pojo.GlobalTagMapId;
import hep.crest.data.pojo.Tag;
import hep.crest.data.repositories.GlobalTagMapRepository;
import hep.crest.data.repositories.GlobalTagRepository;
import hep.crest.data.repositories.TagRepository;
import hep.crest.swagger.model.GlobalTagMapDto;
import ma.glasnost.orika.MapperFacade;


/**
 * @author formica
 * @author rsipos
 *
 */
@Service
public class GlobalTagMapService {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private GlobalTagMapRepository globalTagMapRepository;
	@Autowired
	private GlobalTagRepository globalTagRepository;
	@Autowired
	private TagRepository tagRepository;
	@Autowired
	@Qualifier("mapper")
	private MapperFacade mapper;

	/**
	 * @return the globalTagMapRepository
	 */
	public GlobalTagMapRepository getGlobalTagMapRepository() {
		return globalTagMapRepository;
	}

	/**
	 * @param tagname
	 * @return
	 * @throws ConddbServiceException
	 */
	public List<GlobalTagMapDto> getTagMap(String gtName) throws CdbServiceException {

		try {
			log.debug("Search for GlobalTagMap entries by GlobalTag name {}",gtName);
			Iterable<GlobalTagMap> entitylist = globalTagMapRepository.findByGlobalTagName(gtName);
			return StreamSupport.stream(entitylist.spliterator(), false).map(s -> mapper.map(s,GlobalTagMapDto.class)).collect(Collectors.toList());
		} catch (Exception e) {
			log.debug("Exception in retrieving GlobalTagMap entries using findByGlobalTagName expression...{}",gtName);
			throw new CdbServiceException("Cannot find GTMap entries by name: " + e.getMessage());
		}
	}

	/**
	 * @param tagName
	 * @return
	 * @throws CdbServiceException
	 */
	public List<GlobalTagMapDto> getTagMapByTagName(String tagName) throws CdbServiceException {
		try {
			
			log.debug("Search for GlobalTagMap entries by TAG name {}",tagName);

			Iterable<GlobalTagMap> entitylist = globalTagMapRepository.findByTagName(tagName);
			return StreamSupport.stream(entitylist.spliterator(), false).map(s -> mapper.map(s,GlobalTagMapDto.class)).collect(Collectors.toList());
			
		} catch (Exception e) {
			log.debug("Exception in retrieving GlobalTagMap entries using findByTagName expression...{}",tagName);
			throw new CdbServiceException("Cannot find GTMap entries by name: " + e.getMessage());
		}
	}

	/**
	 * @param dto
	 * @return
	 * @throws CdbServiceException
	 */
	@Transactional
	public GlobalTagMapDto insertGlobalTagMap(GlobalTagMapDto dto) throws CdbServiceException {
		try {
			log.debug("Create global tag map from dto {}",dto);
			GlobalTagMap entity = new GlobalTagMap();
			Optional<GlobalTag> gt = globalTagRepository.findById(dto.getGlobalTagName());
			Tag tg = tagRepository.findByName(dto.getTagName());
			
			GlobalTagMapId id = new GlobalTagMapId(dto.getGlobalTagName(),dto.getRecord(),dto.getLabel());
			entity.setId(id);
			gt.ifPresent(new Consumer<GlobalTag>() {
			    @Override
			    public void accept(GlobalTag agt) {
			    		globalTagRepository.save(agt); // re-save it because it will change the modification time
					entity.setGlobalTag(agt);
			    }
			});
			if (tg != null) {
				entity.setTag(tg);
			} else {
				throw new EntityNotFoundException();
			}
			
			GlobalTagMap saved = globalTagMapRepository.save(entity);
			log.debug("Saved entity: {}",saved);
			return mapper.map(saved,GlobalTagMapDto.class);
		} catch (Exception e) {
			log.debug("Exception in storing global tag map {}", dto);
			throw new CdbServiceException("Cannot store global tag map: " + e.getMessage());
		}
	}
	
}
