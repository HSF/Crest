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

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(TagDirectoryImplementation.class);

    /**
     * The directory tools.
     */
    private DirectoryUtilities dirtools = null;

    /**
     * Default ctor.
     */
    public TagDirectoryImplementation() {
    }

    /**
     * @param dutils
     *            the DirectoryUtilities
     */
    public TagDirectoryImplementation(DirectoryUtilities dutils) {
        this.dirtools = dutils;
    }

    /**
     * @param du
     *            the DirectoryUtilities
     */
    public void setDirtools(DirectoryUtilities du) {
        this.dirtools = du;
    }

    /**
     * @param id
     *            the String
     * @return boolean
     */
    public boolean exists(String id) {
        try {
            dirtools.getTagPath(id);
            return true;
        }
        catch (final CdbServiceException e) {
            log.error("Cannot find tag directory {} : {}", id, e);
            return false;
        }
    }

    /**
     * @param id
     *            the String
     * @return TagDto
     */
    public TagDto findOne(String id) {

        Path tagfilepath;
        try {
            tagfilepath = dirtools.getTagFilePath(id);
            return readTagFile(tagfilepath);
        }
        catch (final CdbServiceException e) {
            log.error("Cannot find tag {} : {}", id, e);
        }
        return null;
    }

    /**
     * @param tagfilepath
     *            the Path
     * @return TagDto
     */
    protected TagDto readTagFile(Path tagfilepath) {
        final StringBuilder buf = new StringBuilder();
        try (BufferedReader reader = Files.newBufferedReader(tagfilepath, dirtools.getCharset())) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                log.debug("Reading line from file {}", line);
                buf.append(line);
            }
            final String jsonstring = buf.toString();
            final TagDto readValue = dirtools.getMapper().readValue(jsonstring, TagDto.class);
            log.debug("Parsed json to get tag object {} with field {} " + " and description {}",
                    readValue, readValue.getName(), readValue.getDescription());
            return readValue;
        }
        catch (final IOException e) {
            log.error("Error in reading tag file from path {}: {}", tagfilepath, e);
        }
        return null;
    }

    /**
     * @return List<TagDto>
     */
    public List<TagDto> findAll() {
        List<String> tagnames;
        tagnames = dirtools.getTagDirectories();
        return tagnames.stream().map(x -> this.findOne(x)).collect(Collectors.toList());
    }

    /**
     * @return long
     */
    public long count() {
        final List<TagDto> dtolist = this.findAll();
        return dtolist.size();
    }

    /**
     * @param name
     *            the String
     * @return List<TagDto>
     */
    public List<TagDto> findByNameLike(String name) {
        final List<String> filteredByNameList;
        filteredByNameList = dirtools.getTagDirectories().stream().filter(x -> x.matches(name))
                .collect(Collectors.toList());
        return filteredByNameList.stream().map(x -> this.findOne(x)).collect(Collectors.toList());

    }

    /**
     * @param entity
     *            the TagDto
     * @return TagDto
     */
    public TagDto save(TagDto entity) {
        final String tagname = entity.getName();
        try {
            final Path tagpath = dirtools.createIfNotexistsTag(tagname);
            if (tagpath != null) {
                final Path filepath = Paths.get(tagpath.toString(), dirtools.getTagfile());
                Files.deleteIfExists(filepath);
                if (!filepath.toFile().exists()) {
                    Files.createFile(filepath);
                }
                final String jsonstr = dirtools.getMapper().writeValueAsString(entity);
                writeTagFile(jsonstr, filepath);
                return entity;
            }
        }
        catch (final RuntimeException | CdbServiceException | IOException x) {
            log.error("Cannot save tag dto {} : {}", entity, x);
        }
        return null;
    }

    /**
     * @param jsonstr
     *            the String
     * @param filepath
     *            the Path
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    protected void writeTagFile(String jsonstr, Path filepath) throws CdbServiceException {
        try (BufferedWriter writer = Files.newBufferedWriter(filepath, dirtools.getCharset())) {
            writer.write(jsonstr);
        }
        catch (final IOException x) {
            throw new CdbServiceException("Cannot write " + jsonstr + " in JSON file", x);
        }
    }

}
