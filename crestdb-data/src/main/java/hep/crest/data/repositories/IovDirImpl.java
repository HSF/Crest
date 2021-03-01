/**
 *
 */
package hep.crest.data.repositories;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.pojo.Iov;
import hep.crest.data.pojo.IovId;
import hep.crest.data.utils.DirectoryUtilities;
import hep.crest.swagger.model.IovDto;
import ma.glasnost.orika.MapperFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * An implementation for IOVs stored in file system.
 *
 * @author formica
 *
 */
public class IovDirImpl implements IIovCrud {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(IovDirImpl.class);

    /**
     * Directory utilities.
     */
    private DirectoryUtilities dirtools = null;

    /**
     * Mapper.
     */
    private MapperFacade mapper;

    /**
     * @param dutils
     *            the DirectoryUtilities
     * @param mapper
     */
    public IovDirImpl(DirectoryUtilities dutils, MapperFacade mapper) {
        this.dirtools = dutils;
        this.mapper = mapper;
    }


    /**
     * @param du
     *            the DirectoryUtilities
     */
    public void setDirtools(DirectoryUtilities du) {
        this.dirtools = du;
    }

    /**
     * Load iovs from JSON file.
     *
     * @param tagname
     * @return List of IovDto
     */
    protected List<IovDto> readJsonFromFile(String tagname) {
        final StringBuilder buf = new StringBuilder();
        try (BufferedReader reader = Files.newBufferedReader(dirtools.getIovFilePath(tagname), dirtools.getCharset())) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                log.debug(line);
                buf.append(line);
            }
            final String jsonstring = buf.toString();
            if (jsonstring.isEmpty()) {
                return new ArrayList<>();
            }
            List<IovDto> iovdtoList = dirtools.getMapper().readValue(jsonstring, new TypeReference<List<IovDto>>() {
            });
            return iovdtoList;
        }
        catch (final RuntimeException | IOException e) {
            log.error("Cannot find iov list for tag {} : {}", tagname, e);
        }
        return new ArrayList<>();
    }

    /**
     * @param name the String representing the Tag name.
     * @return List<Iov>
     */
    @Override
    public List<Iov> findByIdTagName(String name) {
        try {
            List<IovDto> iovdtoList = readJsonFromFile(name);
            final List<Iov> entitylist = StreamSupport.stream(iovdtoList.spliterator(), false).map(s -> mapper.map(s,
                    Iov.class))
                    .collect(Collectors.toList());
            return entitylist;
        }
        catch (final RuntimeException e) {
            log.error("Cannot find iov list for tag {} : {}", name, e);
        }
        return new ArrayList<>();
    }

    /**
     * @param name  the String
     * @param since the BigDecimal
     * @param hash  the String
     * @return Iov
     */
    @Override
    public Iov findBySinceAndTagNameAndHash(String name, BigDecimal since, String hash) {
        try {
            List<IovDto> iovdtoList = readJsonFromFile(name);
            List<Iov> iovlist = iovdtoList.stream()
                    .filter(s -> (s.getTagName().equals(name) && s.getSince().equals(since)
                                  && s.getPayloadHash().equals(hash)))
                    .map(s -> mapper.map(s, Iov.class))
                    .collect(Collectors.toList());
            return iovlist.get(0);
        }
        catch (final RuntimeException e) {
            log.error("Exception searching iov for tag {}, hash {}, since {} : {}", name, hash, since, e);
        }
        return null;
    }

    /**
     * @param name  the String
     * @param since the BigDecimal
     * @param until the BigDecimal
     * @return List<Iov>
     */
    @Override
    public List<Iov> selectLatestByGroup(String name, BigDecimal since, BigDecimal until) {
        try {
            List<IovDto> iovdtoList = readJsonFromFile(name);
            List<IovDto> selectedList =
                    iovdtoList.stream().filter(s -> s.getTagName().equalsIgnoreCase(name))
                            .filter(s -> s.getSince().compareTo(since) >= 0)
                            .filter(s -> s.getSince().compareTo(until) <= 0).collect(Collectors.toList());
            final List<Iov> entitylist = StreamSupport.stream(selectedList.spliterator(), false)
                    .map(s -> mapper.map(s, Iov.class))
                    .collect(Collectors.toList());
            return entitylist;
        }
        catch (final RuntimeException e) {
            log.error("Exception searching iov for tag {}, since {}, until {} : {}", name, since, until, e);
        }
        return null;
    }

    /**
     * This method is like the getRange method, but it does not include the IOV before the given since.
     * It will provide the same result as getRange only if the since time provided is equivalent
     * to the first since selected in the DB. For other cases it will not contain the first IOV.
     *
     * @param name     the String
     * @param since    the BigDecimal
     * @param until    the BigDecimal
     * @param snapshot the Date
     * @return List<Iov>
     */
    @Override
    public List<Iov> selectSnapshotByGroup(String name, BigDecimal since, BigDecimal until, Date snapshot) {
        try {
            List<IovDto> iovdtoList = readJsonFromFile(name);
            List<IovDto> selectedList =
                    iovdtoList.stream().filter(s -> s.getTagName().equalsIgnoreCase(name))
                            .filter(s -> s.getSince().compareTo(since) >= 0)
                            .filter(s -> s.getSince().compareTo(until) <= 0)
                            .filter(s -> s.getInsertionTime().before(snapshot)).collect(Collectors.toList());
            final List<Iov> entitylist = StreamSupport.stream(selectedList.spliterator(), false)
                    .map(s -> mapper.map(s, Iov.class))
                    .collect(Collectors.toList());
            return entitylist;
        }
        catch (final RuntimeException e) {
            log.error("Exception searching iov for tag {}, since {}, until {} : {}", name, since, until, e);
        }
        return null;
    }

    /**
     * @param name     the String
     * @param since    the BigDecimal
     * @param snapshot the Date
     * @return List<Iov>
     */
    @Override
    public List<Iov> selectAtTime(String name, BigDecimal since, Date snapshot) {
        try {
            List<IovDto> iovdtoList = readJsonFromFile(name);
            Long maxdiff = Long.MAX_VALUE;
            Long closemin = since.longValue();
            Long closemax = since.longValue();
            for (IovDto dto : iovdtoList) {
                Long mindiff = Math.abs(dto.getSince().longValue() - since.longValue());
                if (mindiff < maxdiff) {
                    maxdiff = mindiff;
                    if (dto.getSince().longValue() > closemax) {
                        closemax = dto.getSince().longValue();
                    }
                    if (dto.getSince().longValue() < closemin) {
                        closemin = dto.getSince().longValue();
                    }
                }
            }
            final long cmax = closemax;
            final long cmin = closemin;
            List<IovDto> selectedList =
                    iovdtoList.stream().filter(s -> s.getTagName().equalsIgnoreCase(name))
                            .filter(s -> (s.getSince().longValue() == cmax || s.getSince().longValue() == cmin))
                            .collect(Collectors.toList());
            final List<Iov> entitylist = StreamSupport.stream(selectedList.spliterator(), false)
                    .map(s -> mapper.map(s, Iov.class))
                    .collect(Collectors.toList());
            return entitylist;
        }
        catch (final RuntimeException e) {
            log.error("Exception searching iov for tag {}, since {} : {}", name, since, e);
        }
        return null;
    }

    /**
     * @param name     the String
     * @param since    the BigDecimal
     * @param until    the BigDecimal
     * @param snapshot the Date
     * @return List<Iov>
     */
    @Override
    public List<Iov> getRange(String name, BigDecimal since, BigDecimal until, Date snapshot) {
        return null;
    }

    /**
     * @param name the String
     * @return List<Iov>
     */
    @Override
    public List<Iov> selectLatestByTag(String name) {
        return null;
    }

    /**
     * @param tagname  the String
     * @param snapshot the Date
     * @return List<Iov>
     */
    @Override
    public List<Iov> selectSnapshot(String tagname, Date snapshot) {
        return null;
    }

    /**
     * Remove an iov using the Id.
     *
     * @param id
     */
    @Override
    public void deleteById(IovId id) {
        try {
            Predicate<Iov> isRemoved = item -> (item.getId().equals(id));
            final Path iovfilepath = dirtools.createIfNotexistsIov(id.getTagName());
            List<IovDto> iovdtoList = readJsonFromFile(id.getTagName());
            final List<Iov> entitylist = StreamSupport.stream(iovdtoList.spliterator(), false)
                    .map(s -> mapper.map(s, Iov.class))
                    .collect(Collectors.toList());
            entitylist.removeIf(isRemoved);
            final String jsonstr = dirtools.getMapper().writeValueAsString(entitylist);
            writeIovFile(jsonstr, iovfilepath);
        }
        catch (final RuntimeException | JsonProcessingException x) {
            log.error("Cannot remove iov id {} : {}", id, x);
        }

    }

    /**
     * Remove an iov using the entity.
     *
     * @param entity
     */
    @Override
    public void delete(Iov entity) {
        try {
            Predicate<Iov> isRemoved = item -> (item.equals(entity));
            final Path iovfilepath = dirtools.createIfNotexistsIov(entity.getId().getTagName());
            List<IovDto> iovdtoList = readJsonFromFile(entity.getId().getTagName());
            final List<Iov> entitylist = StreamSupport.stream(iovdtoList.spliterator(), false)
                    .map(s -> mapper.map(s, Iov.class))
                    .collect(Collectors.toList());
            entitylist.removeIf(isRemoved);
            final String jsonstr = dirtools.getMapper().writeValueAsString(entitylist);
            writeIovFile(jsonstr, iovfilepath);
        }
        catch (final RuntimeException | JsonProcessingException x) {
            log.error("Cannot remove iov entity {} : {}", entity, x);
        }
    }

    /**
     * Save an iov.
     *
     * @param entity the Iov to save.
     * @return Iov.
     */
    @Override
    public Iov save(Iov entity) {
        try {
            final String tagname = entity.getId().getTagName();
            final Path iovfilepath = dirtools.createIfNotexistsIov(tagname);
            final List<Iov> iovlist = this.findByIdTagName(tagname);
            iovlist.add(entity);
            List<IovDto> dtolist = StreamSupport.stream(iovlist.spliterator(), false)
                    .map(s -> mapper.map(s, IovDto.class))
                    .collect(Collectors.toList());
            dtolist.sort(Comparator.comparing(IovDto::getSince));
            // FIXME: this is probably inefficient for large number of iovs...to be checked
            final String jsonstr = dirtools.getMapper().writeValueAsString(dtolist);
            writeIovFile(jsonstr, iovfilepath);
            return entity;
        }
        catch (final RuntimeException | JsonProcessingException x) {
            log.error("Cannot save iov dto {} : {}", entity, x);
        }
        return null;
    }

    /**
     * @param tagname
     *            the String
     * @param iovlist
     *            the Iterable<Iov>
     * @return int
     */
    public int saveAll(String tagname, Iterable<Iov> iovlist) {

        try {
            if (iovlist == null) {
                log.error("Cannot save empty iov list for tag {}", tagname);
                return -1;
            }
            // FIXME: this is probably inefficient for large number of iovs...to be checked
            List<IovDto> dtolist = StreamSupport.stream(iovlist.spliterator(), false)
                    .map(s -> mapper.map(s, IovDto.class))
                    .collect(Collectors.toList());
            final String jsonstr = dirtools.getMapper().writeValueAsString(dtolist);
            final Path iovfilepath = dirtools.createIfNotexistsIov(tagname);
            writeIovFile(jsonstr, iovfilepath);
            return dtolist.size();
        }
        catch (final IOException | CdbServiceException x) {
            log.error("Cannot save iov list for tag {} : {}", tagname, x);
        }
        return 0;
    }

    /**
     * @param jsonstr
     *            the String
     * @param iovfilepath
     *            the Path
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    protected void writeIovFile(String jsonstr, Path iovfilepath) {
        try (BufferedWriter writer = Files.newBufferedWriter(iovfilepath, dirtools.getCharset())) {
            writer.write(jsonstr);
        }
        catch (final IOException x) {
            log.error("Cannot write iov file {} from {} : {}", jsonstr, iovfilepath, x);
        }
    }

}
