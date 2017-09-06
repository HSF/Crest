/**
 * 
 */
package hep.crest.data.repositories;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;

import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.utils.DirectoryUtilities;
import hep.crest.swagger.model.IovDto;

/**
 * @author formica
 *
 */
public class IovDirectoryImplementation {

	private static final Logger log = LoggerFactory.getLogger(IovDirectoryImplementation.class);

	private DirectoryUtilities dirtools = null;

	
	public IovDirectoryImplementation() {
		super();
		// TODO Auto-generated constructor stub
	}

	public IovDirectoryImplementation(DirectoryUtilities dutils) {
		super();
		this.dirtools = dutils;
	}

	public void setDirtools(DirectoryUtilities du) {
		this.dirtools = du;
	}
	
	public List<IovDto> findByTagName(String tagname) throws CdbServiceException {
		Path iovfilepath = dirtools.getIovFilePath(tagname);
		StringBuffer buf = new StringBuffer();
		try (BufferedReader reader = Files.newBufferedReader(iovfilepath, dirtools.getCharset())) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				log.debug(line);
				buf.append(line);
			}
			String jsonstring = buf.toString();
			if (jsonstring.isEmpty()) {
				return null;
			}
			List<IovDto> readValue = dirtools.getMapper().readValue(jsonstring, new TypeReference<List<IovDto>>() {
			});
			return readValue;
		} catch (IOException x) {
			throw new CdbServiceException("Cannot find iov list for tag " + tagname);
		}
	}

	public IovDto save(IovDto iovdto) throws CdbServiceException {

		try {
			String tagname = iovdto.getTagName();
			Path iovfilepath = dirtools.createIfNotexistsIov(tagname);
			List<IovDto> iovlist = this.findByTagName(tagname);
			if (iovlist == null) {
				iovlist = new ArrayList<>();
			}
			
			iovlist.add(iovdto);
			iovlist.sort(Comparator.comparing(IovDto::getSince));

			// FIXME: this is probably inefficient for large number of iovs...to be checked
			String jsonstr = dirtools.getMapper().writeValueAsString(iovlist);

			BufferedWriter writer = Files.newBufferedWriter(iovfilepath, dirtools.getCharset());
			writer.write(jsonstr);
			writer.close();

			return iovdto;
		} catch (IOException x) {
			throw new CdbServiceException("IO error " + x.getMessage());
		}
	}

	public List<IovDto> saveAll(String tagname, List<IovDto> iovdtolist) throws CdbServiceException {

		try {
			Path iovfilepath = dirtools.createIfNotexistsIov(tagname);
			if (iovdtolist == null) {
				throw new CdbServiceException("Iov list is empty...cannot create file for iovs");
			}
			
			// FIXME: this is probably inefficient for large number of iovs...to be checked
			String jsonstr = dirtools.getMapper().writeValueAsString(iovdtolist);

			BufferedWriter writer = Files.newBufferedWriter(iovfilepath, dirtools.getCharset());
			writer.write(jsonstr);
			writer.close();

			return iovdtolist;
		} catch (IOException x) {
			throw new CdbServiceException("IO error " + x.getMessage());
		}
	}

}
