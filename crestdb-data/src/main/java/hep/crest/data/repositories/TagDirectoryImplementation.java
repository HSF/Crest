/**
 * 
 */
package hep.crest.data.repositories;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.utils.DirectoryUtilities;
import hep.crest.swagger.model.TagDto;

/**
 * @author formica
 *
 */
public class TagDirectoryImplementation {

	private static final Logger log = LoggerFactory.getLogger(TagDirectoryImplementation.class);

	private DirectoryUtilities dirtools = null;
	
	/**
	 * 
	 */
	public TagDirectoryImplementation() {
		super();
	}

	/**
	 * @param dutils
	 */
	public TagDirectoryImplementation(DirectoryUtilities dutils) {
		super();
		this.dirtools = dutils;
	}

	/**
	 * @param du
	 */
	public void setDirtools(DirectoryUtilities du) {
		this.dirtools = du;
	}
	
	/**
	 * @param id
	 * @return
	 */
	public boolean exists(String id) {
		try {
			dirtools.getTagPath(id);
			return true;
		} catch (CdbServiceException e) {
			return false;
		}
	}

	/**
	 * @param id
	 * @return
	 */
	public TagDto findOne(String id) {
		
		Path tagfilepath;
		try {
			tagfilepath = dirtools.getTagFilePath(id);
			return readTagFile(tagfilepath);
		} catch (CdbServiceException e1) {
			log.error("Cannot find file with id {} ",id);
		}
		return null;
	}

	/**
	 * @param tagfilepath
	 * @return
	 */
	protected TagDto readTagFile(Path tagfilepath) {
		StringBuilder buf = new StringBuilder();
		try (BufferedReader reader = Files.newBufferedReader(tagfilepath, dirtools.getCharset())) {
		    String line = null;
		    while ((line = reader.readLine()) != null) {
		        log.debug("Reading line from file {}",line);
		        buf.append(line);
		    }
		    String jsonstring = buf.toString();
			TagDto readValue = dirtools.getMapper().readValue(jsonstring, TagDto.class);
			log.debug("Parsed json to get tag object {} with field {} "
					+ " and description {}",readValue,readValue.getName(),readValue.getDescription());
		    return readValue;
		} catch (IOException e) {
			log.error("Error in reading tag file from path {}",tagfilepath);
		}
		return null;
	}
	
	/**
	 * @return
	 */
	public List<TagDto> findAll() {
		List<String> tagnames;
		tagnames = dirtools.getTagDirectories();
		return tagnames.stream().map(x -> this.findOne(x)).collect(Collectors.toList());
	}

	/**
	 * @return
	 */
	public long count() {
		List<TagDto> dtolist = this.findAll();
		return dtolist.size();
	}

	/**
	 * @param name
	 * @return
	 */
	public List<TagDto> findByNameLike(String name) {
		List<String> filteredByNameList;
		filteredByNameList = dirtools.getTagDirectories().stream().filter(x -> x.matches(name)).collect(Collectors.toList());
		return filteredByNameList.stream().map(x -> this.findOne(x)).collect(Collectors.toList());
	}

	
	/**
	 * @param entity
	 * @return
	 * @throws CdbServiceException
	 */
	public TagDto save(TagDto entity) throws CdbServiceException {
		String tagname = entity.getName();
		try {
			Path tagpath = dirtools.createIfNotexistsTag(tagname);
			if (tagpath != null) {
				Path filepath = Paths.get(tagpath.toString(), dirtools.getTagfile());
				Files.deleteIfExists(filepath);
				if (!filepath.toFile().exists()) {
					Files.createFile(filepath);
				}
				String jsonstr = dirtools.getMapper().writeValueAsString(entity);
				writeTagFile(jsonstr, filepath);
				return entity;				
			} else {
				throw new CdbServiceException("Tag path is null...");
			}
		} catch (IOException x) {
			throw new CdbServiceException(x.getMessage());
		}
	}
	
	/**
	 * @param jsonstr
	 * @param filepath
	 * @throws CdbServiceException
	 */
	protected void writeTagFile(String jsonstr, Path filepath) throws CdbServiceException {
		try (BufferedWriter writer = Files.newBufferedWriter(filepath, dirtools.getCharset())) {
			writer.write(jsonstr);
		} catch (IOException x) {
			throw new CdbServiceException("Cannot write " + jsonstr+ " in JSON file");
		}
	}

}
