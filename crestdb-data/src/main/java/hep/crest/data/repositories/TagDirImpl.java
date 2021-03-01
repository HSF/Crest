package hep.crest.data.repositories;

import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.pojo.Tag;
import hep.crest.data.utils.DirectoryUtilities;
import hep.crest.swagger.model.TagDto;
import ma.glasnost.orika.MapperFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class TagDirImpl implements ITagCrud {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(TagDirImpl.class);

    /**
     * Directory utilities.
     */
    private DirectoryUtilities dirtools = null;

    /**
     * Mapper.
     */
    private MapperFacade mapper;

    /**
     * @param dutils the DirectoryUtilities
     * @param mapper
     */
    public TagDirImpl(DirectoryUtilities dutils, MapperFacade mapper) {
        this.dirtools = dutils;
        this.mapper = mapper;
    }

    /**
     * @param du the DirectoryUtilities
     */
    public void setDirtools(DirectoryUtilities du) {
        this.dirtools = du;
    }


    /**
     * @param entity the Tag
     * @return Tag
     */
    @Override
    public Tag save(Tag entity) {
        final String tagname = entity.getName();
        try {
            final Path tagpath = dirtools.createIfNotexistsTag(tagname);
            if (tagpath != null) {
                final Path filepath = Paths.get(tagpath.toString(), dirtools.getTagfile());
                Files.deleteIfExists(filepath);
                if (!filepath.toFile().exists()) {
                    Files.createFile(filepath);
                }
                TagDto dto = mapper.map(entity, TagDto.class);
                final String jsonstr = dirtools.getMapper().writeValueAsString(dto);
                writeTagFile(jsonstr, filepath);
                return entity;
            }
        }
        catch (final RuntimeException | IOException x) {
            log.error("Cannot save tag {} : {}", entity, x.getMessage());
        }
        return null;
    }

    /**
     * The method does not access blob data.
     *
     * @param name the String
     * @return The tag metadata or null.
     */
    @Override
    public Tag findByName(String name) {
        Path tagfilepath;
        try {
            tagfilepath = dirtools.getTagFilePath(name);
            log.debug("findByName uses tag file path {}", tagfilepath);
            return readTagFile(tagfilepath);
        }
        catch (final CdbServiceException e) {
            log.error("Cannot find tag {} : {}", name, e.getMessage());
        }
        return null;
    }

    /**
     * @param id the String
     * @return
     */
    @Override
    public void deleteById(String id) {
        throw new UnsupportedOperationException();
    }

    /**
     * Remove an iov using the entity.
     *
     * @param entity
     */
    @Override
    public void delete(Tag entity) {
        throw new UnsupportedOperationException();
    }

    /**
     * @param name the String
     * @return List<Tag>
     */
    @Override
    public List<Tag> findByNameLike(String name) {
        final List<String> filteredByNameList;
        filteredByNameList = dirtools.getTagDirectories().stream().filter(x -> x.matches(name))
                .collect(Collectors.toList());
        return filteredByNameList.stream().map(this::findOne).collect(Collectors.toList());
    }

    /**
     * Find all tags in the backend.
     *
     * @return
     */
    @Override
    public List<Tag> findAll() {
        List<String> tagnames;
        tagnames = dirtools.getTagDirectories();
        return tagnames.stream().map(this::findOne).collect(Collectors.toList());
    }

    /**
     * @param id the String
     * @return Tag
     */
    @Override
    public Tag findOne(String id) {
        return findByName(id);
    }

    /**
     * @param tagfilepath the Path
     * @return TagDto
     */
    protected Tag readTagFile(Path tagfilepath) {
        final StringBuilder buf = new StringBuilder();
        try (BufferedReader reader = Files.newBufferedReader(tagfilepath, dirtools.getCharset())) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                log.debug("Reading line from file {}", line);
                buf.append(line);
            }
            final String jsonstring = buf.toString();
            final TagDto readValue = dirtools.getMapper().readValue(jsonstring, TagDto.class);
            final Tag entity = mapper.map(readValue, Tag.class);
            log.debug("Parsed json to get tag object {} ", entity);
            return entity;
        }
        catch (final IOException e) {
            log.error("Error in reading tag file from path {}: {}", tagfilepath, e.getMessage());
        }
        return null;
    }

    /**
     * @param jsonstr  the String
     * @param filepath the Path
     * @throws CdbServiceException If an Exception occurred
     */
    protected void writeTagFile(String jsonstr, Path filepath) {
        try (BufferedWriter writer = Files.newBufferedWriter(filepath, dirtools.getCharset())) {
            writer.write(jsonstr);
        }
        catch (final IOException x) {
            throw new CdbServiceException("Cannot write " + jsonstr + " in JSON file", x);
        }
    }
}
