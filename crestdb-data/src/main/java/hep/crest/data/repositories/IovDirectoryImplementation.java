/**
 *
 */
package hep.crest.data.repositories;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.utils.DirectoryUtilities;
import hep.crest.swagger.model.IovDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * An implementation for IOVs stored in file system.
 *
 * @author formica
 *
 */
public class IovDirectoryImplementation {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(IovDirectoryImplementation.class);

    /**
     * Directory utilities.
     */
    private DirectoryUtilities dirtools = null;

    /**
     * Default ctor.
     */
    public IovDirectoryImplementation() {
        super();
    }

    /**
     * @param dutils
     *            the DirectoryUtilities
     */
    public IovDirectoryImplementation(DirectoryUtilities dutils) {
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
     * @param tagname
     *            the String
     * @return List<IovDto>
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    public List<IovDto> findByTagName(String tagname) throws CdbServiceException {
        //final Path iovfilepath = dirtools.getIovFilePath(tagname);
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
            return dirtools.getMapper().readValue(jsonstring, new TypeReference<List<IovDto>>() {
            });
        }
        catch (final RuntimeException | IOException x) {
            log.error("Cannot find iov list for tag {} : {}", tagname, x);
        }
        return new ArrayList<>();
    }

    /**
     * @param iovdto
     *            the IovDto
     * @return IovDto
     */
    public IovDto save(IovDto iovdto) {

        try {
            final String tagname = iovdto.getTagName();
            final Path iovfilepath = dirtools.createIfNotexistsIov(tagname);
            final List<IovDto> iovlist = this.findByTagName(tagname);
            iovlist.add(iovdto);
            iovlist.sort(Comparator.comparing(IovDto::getSince));
            // FIXME: this is probably inefficient for large number of iovs...to be checked
            final String jsonstr = dirtools.getMapper().writeValueAsString(iovlist);
            writeIovFile(jsonstr, iovfilepath);

            return iovdto;
        }
        catch (final RuntimeException | JsonProcessingException x) {
            log.error("Cannot save iov dto {} : {}", iovdto, x);
        }
        return null;
    }

    /**
     * @param jsonstr
     *            the String
     * @param iovfilepath
     *            the Path
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    protected void writeIovFile(String jsonstr, Path iovfilepath) throws CdbServiceException {
        try (BufferedWriter writer = Files.newBufferedWriter(iovfilepath, dirtools.getCharset())) {
            writer.write(jsonstr);
        }
        catch (final IOException x) {
            log.error("Cannot write iov file {} from {} : {}", jsonstr, iovfilepath, x);
        }
    }

    /**
     * @param tagname
     *            the String
     * @param iovdtolist
     *            the List<IovDto>
     * @return List<IovDto>
     */
    public List<IovDto> saveAll(String tagname, List<IovDto> iovdtolist) {

        try {
            if (iovdtolist == null) {
                log.error("Cannot save empty iov list for tag {}", tagname);
                return new ArrayList<>();
            }
            // FIXME: this is probably inefficient for large number of iovs...to be checked
            final String jsonstr = dirtools.getMapper().writeValueAsString(iovdtolist);
            final Path iovfilepath = dirtools.createIfNotexistsIov(tagname);
            writeIovFile(jsonstr, iovfilepath);
            return iovdtolist;
        }
        catch (final IOException | CdbServiceException x) {
            log.error("Cannot save iov list for tag {} : {}", tagname, x);
        }
        return new ArrayList<>();
    }
}
