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

	
	
	public TagDirectoryImplementation() {
		super();
		// TODO Auto-generated constructor stub
	}

	public TagDirectoryImplementation(DirectoryUtilities dutils) {
		super();
		this.dirtools = dutils;
	}

	public void setDirtools(DirectoryUtilities du) {
		this.dirtools = du;
	}
	
	public boolean exists(String id) {
		try {
			dirtools.getTagPath(id);
			return true;
		} catch (CdbServiceException e) {
			return false;
		}
	}

	public TagDto findOne(String id) {
		
		Path tagfilepath;
		try {
			tagfilepath = dirtools.getTagFilePath(id);
			StringBuffer buf = new StringBuffer();
			try (BufferedReader reader = Files.newBufferedReader(tagfilepath, dirtools.getCharset())) {
			    String line = null;
			    while ((line = reader.readLine()) != null) {
			        System.out.println(line);
			        buf.append(line);
			    }
			    String jsonstring = buf.toString();
				TagDto readValue = dirtools.getMapper().readValue(jsonstring, TagDto.class);
				log.debug("Parsed json to get tag object " + readValue + " with field " + readValue.getName()
						+ " and description " + readValue.getDescription());
			    return readValue;
			    
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (CdbServiceException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		return null;
	}

	
	public List<TagDto> findAll() {
		List<String> tagnames;
		try {
			tagnames = dirtools.getTagDirectories();
			List<TagDto> dtolist = tagnames.stream().map(x -> this.findOne(x)).collect(Collectors.toList());
			return dtolist;
		} catch (CdbServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public long count() {
		List<TagDto> dtolist = this.findAll();
		return dtolist.size();
	}

	public List<TagDto> findByNameLike(String name) {
		List<String> filteredByNameList;
		try {
			filteredByNameList = dirtools.getTagDirectories().stream().filter(x -> x.matches(name)).collect(Collectors.toList());
			List<TagDto> dtolist = filteredByNameList.stream().map(x -> this.findOne(x)).collect(Collectors.toList());
			return dtolist;
		} catch (CdbServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	
	public TagDto save(TagDto entity) throws CdbServiceException {
		// TODO Auto-generated method stub
		String tagname = entity.getName();
		try {
			Path tagpath = dirtools.createIfNotexistsTag(tagname);
			if (tagpath != null) {
				Path filepath = Paths.get(tagpath.toString(), dirtools.getTagfile());
				Files.deleteIfExists(filepath);
				if (Files.notExists(filepath)) {
					Files.createFile(filepath);
				}
				String jsonstr = dirtools.getMapper().writeValueAsString(entity);
				
				try (BufferedWriter writer = Files.newBufferedWriter(filepath, dirtools.getCharset())) {
					writer.write(jsonstr);
				} catch (IOException x) {
					throw new CdbServiceException("Cannot write " + jsonstr+ " in JSON file");
				}

				return entity;				
			} else {
				throw new CdbServiceException("Tag path is null...");
			}
		} catch (IOException x) {
			throw new CdbServiceException(x.getMessage());
		}
	}

}
