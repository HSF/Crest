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
 * An implementation for IOVs stored in file system.
 *
 * @author formica
 *
 */
public class IovDirectoryImplementation {

    /**
     * Logger.
     */
    private final Logger log = LoggerFactory.getLogger(IovDirectoryImplementation.class);

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
        super();
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
        final Path iovfilepath = dirtools.getIovFilePath(tagname);
        final StringBuilder buf = new StringBuilder();
        try (BufferedReader reader = Files.newBufferedReader(iovfilepath, dirtools.getCharset())) {
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
        catch (final IOException x) {
            throw new CdbServiceException("Cannot find iov list for tag " + tagname);
        }
    }

    /**
     * @param iovdto
     *            the IovDto
     * @return IovDto
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    public IovDto save(IovDto iovdto) throws CdbServiceException {

        try {
            final String tagname = iovdto.getTagName();
            final Path iovfilepath = dirtools.createIfNotexistsIov(tagname);
            List<IovDto> iovlist = this.findByTagName(tagname);
            if (iovlist == null) {
                iovlist = new ArrayList<>();
            }

            iovlist.add(iovdto);
            iovlist.sort(Comparator.comparing(IovDto::getSince));
            // FIXME: this is probably inefficient for large number of iovs...to be checked
            final String jsonstr = dirtools.getMapper().writeValueAsString(iovlist);
            writeIovFile(jsonstr, iovfilepath);

            return iovdto;
        }
        catch (final IOException x) {
            throw new CdbServiceException("IO error " + x.getMessage());
        }
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
            throw new CdbServiceException("Cannot write " + jsonstr + " in JSON file");
        }
    }

    /**
     * @param tagname
     *            the String
     * @param iovdtolist
     *            the List<IovDto>
     * @return List<IovDto>
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    public List<IovDto> saveAll(String tagname, List<IovDto> iovdtolist)
            throws CdbServiceException {

        try {
            final Path iovfilepath = dirtools.createIfNotexistsIov(tagname);
            if (iovdtolist == null) {
                throw new CdbServiceException("Iov list is empty...cannot create file for iovs");
            }
            // FIXME: this is probably inefficient for large number of iovs...to be checked
            final String jsonstr = dirtools.getMapper().writeValueAsString(iovdtolist);
            writeIovFile(jsonstr, iovfilepath);
            return iovdtolist;
        }
        catch (final IOException x) {
            throw new CdbServiceException("IO error " + x.getMessage());
        }
    }
}
