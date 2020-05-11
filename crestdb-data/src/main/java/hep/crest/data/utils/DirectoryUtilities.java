package hep.crest.data.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import hep.crest.data.exceptions.CdbServiceException;

/**
 * An utility class to deal with disk based storage.
 *
 * @author formica
 *
 */
public class DirectoryUtilities {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(DirectoryUtilities.class);

    /**
     * Name of the tag file.
     */
    private static final String TAG_FILE = "tag.json";
    /**
     * Name of the iov file.
     */
    private static final String IOV_FILE = "iovs.json";
    /**
     * Name of the payload directory.
     */
    private static final String PAYLOAD_DIR = "data";

    /**
     * Charset.
     */
    private final Charset charset = Charset.forName("UTF-8");

    /**
     * Mapper.
     */
    private ObjectMapper mapper = new ObjectMapper();

    /**
     * Temporary default for base directory.
     */
    private String locbasedir = "/tmp/cdms";

    /**
     * Default Ctor.
     */
    public DirectoryUtilities() {
        super();
    }

    /**
     * @param basedir
     *            the Base directory
     */
    public DirectoryUtilities(String basedir) {
        locbasedir = basedir;
    }

    /**
     * @return ObjectMapper
     */
    public ObjectMapper getMapper() {
        return mapper;
    }

    /**
     * @param mapper
     *            the ObjectMapper
     * @return
     */
    public void setMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * @return String
     */
    public String getTagfile() {
        return TAG_FILE;
    }

    /**
     * @return String
     */
    public String getIovfile() {
        return IOV_FILE;
    }

    /**
     * @return Charset
     */
    public Charset getCharset() {
        return charset;
    }

    /**
     * @return Path
     */
    public Path getBasePath() {
        return this.getBasePath(locbasedir);
    }

    /**
     * @param tagname
     *            the String
     * @throws CdbServiceException
     *             If an Exception occurred
     * @return Path
     */
    public Path getTagPath(String tagname) throws CdbServiceException {
        return this.getTagPath(locbasedir, tagname);
    }

    /**
     * @param tagname
     *            the String
     * @throws CdbServiceException
     *             If an Exception occurred
     * @return Path
     */
    public Path getTagFilePath(String tagname) throws CdbServiceException {
        return this.getTagFilePath(locbasedir, tagname);
    }

    /**
     * @param tagname
     *            the String
     * @throws CdbServiceException
     *             If an Exception occurred
     * @return Path
     */
    public Path getIovFilePath(String tagname) throws CdbServiceException {
        return this.getIovFilePath(locbasedir, tagname);
    }

    /**
     * @return List<String>
     */
    public List<String> getTagDirectories() {
        return this.getTagDirectories(locbasedir);
    }

    /**
     * @return Path
     */
    public Path getPayloadPath() {
        return getPayloadPath(locbasedir);
    }

    /**
     * @param name
     *            the String
     * @return Path
     * @throws CdbServiceException If an Exception occurred
     */
    public Path createIfNotexistsTag(String name) throws CdbServiceException {
        return createIfNotexistsTag(locbasedir, name);
    }

    /**
     * @param name
     *            the String
     * @return Path
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    public Path createIfNotexistsIov(String name) throws CdbServiceException {
        return createIfNotexistsIov(locbasedir, name);
    }

    /**
     * @param basedir
     *            the String
     * @param tagname
     *            the String
     * @throws CdbServiceException
     *             If an Exception occurred
     * @return Path
     */
    public Path getTagPath(String basedir, String tagname) throws CdbServiceException {
        final Path tagpath = Paths.get(basedir, tagname);
        if (!tagpath.toFile().exists()) {
            throw new CdbServiceException("DirectoryUtility: cannot find directory for tag name " + tagname);
        }
        return tagpath;
    }

    /**
     * @param basedir
     *            the String
     * @param tagname
     *            the String
     * @throws CdbServiceException
     *             If an Exception occurred
     * @return Path
     */
    public Path getTagFilePath(String basedir, String tagname) throws CdbServiceException {
        final Path tagpath = getTagPath(basedir, tagname);
        final Path tagfilepath = Paths.get(tagpath.toString(), TAG_FILE);
        if (!tagfilepath.toFile().exists()) {
            throw new CdbServiceException("DirectoryUtility: cannot find tag file for tag name " + tagname);
        }
        return tagfilepath;
    }

    /**
     * @param basedir
     *            the String
     * @param tagname
     *            the String
     * @throws CdbServiceException
     *             If an Exception occurred
     * @return Path
     */
    public Path getIovFilePath(String basedir, String tagname) throws CdbServiceException {
        final Path tagpath = getTagPath(basedir, tagname);
        final Path iovfilepath = Paths.get(tagpath.toString(), IOV_FILE);
        if (!iovfilepath.toFile().exists()) {
            throw new CdbServiceException("DirectoryUtility: cannot find iov file for tag name " + tagname);
        }
        return iovfilepath;
    }

    /**
     * @param basedir
     *            the String
     * @return List<String>
     */
    public List<String> getTagDirectories(String basedir) {
        final Path basedirpath = Paths.get(basedir);
        List<Path> pfiles;
        try (Stream<Path> pstream = Files.walk(basedirpath);) {
            pfiles = pstream.collect(Collectors.toList());
            return pfiles.stream().filter(s -> s.getFileName().toString().contains(TAG_FILE))
                    .map(x -> x.getName(x.getNameCount() - 2).toString())
                    .collect(Collectors.toList());
        }
        catch (final IOException e) {
            log.error("Error getting tags directories from {}: {}", basedirpath, e);
        }
        return new ArrayList<>();
    }

    /**
     * @param basedir
     *            the String
     * @return Path
     */
    public Path getBasePath(String basedir) {
        final Path base = Paths.get(basedir);
        log.info("creating directory {}", base);
        if (!base.toFile().exists()) {
            // create the directory
            try {
                Files.createDirectories(base);
            }
            catch (final IOException e) {
                log.error("Error creating base directory {}: {}", base, e);
                return null;
            }
        }
        return base;
    }

    /**
     * @param basedir
     *            the String
     * @return Path
     */
    public Path getPayloadPath(String basedir) {
        final Path ppath = Paths.get(basedir, PAYLOAD_DIR);
        log.info("Creating directory if does not exists {}", ppath);
        if (!ppath.toFile().exists()) {
            // create the directory
            try {
                Files.createDirectories(ppath);
            }
            catch (final IOException e) {
                log.error("Error creating directory for payload {}: {}", ppath, e);
                return null;
            }
        }
        return ppath;
    }

    /**
     * @param basedir
     *            the String
     * @param name
     *            the String
     * @return Path
     * @throws CdbServiceException If an Exception occurred
     */
    public Path createIfNotexistsTag(String basedir, String name) throws CdbServiceException {
        if (name == null) {
            throw new CdbServiceException("Cannot use null tag name");
        }
        final String tagname = name;
        final Path tagpath = Paths.get(basedir, tagname);
        if (tagpath.toFile().exists()) {
            return tagpath;
        }
        else {
            try {
                Files.createDirectories(tagpath);
                final Path tagfilepath = Paths.get(basedir, tagname, TAG_FILE);
                Files.createFile(tagfilepath);
                return tagpath;
            }
            catch (final IOException e) {
                log.error("Error creating directory for tag {}: {}", tagpath, e);
            }
        }
        return null;
    }

    /**
     * @param basedir
     *            the String
     * @param name
     *            the String
     * @return Path
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    public Path createIfNotexistsIov(String basedir, String name) throws CdbServiceException {
        if (name == null) {
            throw new CdbServiceException("Cannot use null tag name");
        }
        final String tagname = name;
        final Path tagpath = Paths.get(basedir, tagname);
        if (!tagpath.toFile().exists()) {
            throw new CdbServiceException("Cannot find tag directory for tag name " + tagname);
        }
        else {
            try {
                final Path iovfilepath = Paths.get(basedir, tagname, IOV_FILE);
                Files.createFile(iovfilepath);
                return iovfilepath;
            }
            catch (final IOException e) {
                log.error("Error creating iov file for tag  {}: {}", name, e);
            }
        }
        return null;
    }

    /**
     * @param hash
     *            the String
     * @return String
     */
    public String hashdir(String hash) {
        return hash.substring(0, 2);
    }

    /**
     * @param apath
     *            the Path
     * @param filename
     *            the String
     * @return Boolean
     */
    public Boolean existsFile(Path apath, String filename) {
        if (!apath.toFile().exists()) {
            return false;
        }
        final Path filepath = Paths.get(apath.toString(), filename);
        return filepath.toFile().exists();
    }

    /**
     * @param source
     *            the String
     * @param outdir
     *            the String
     * @return String
     */
    public String createTarFile(String source, String outdir) {
        final String outtarfile = outdir.concat(".tar.gz");
        try (FileOutputStream fos = new FileOutputStream(outtarfile);
                GZIPOutputStream gos = new GZIPOutputStream(new BufferedOutputStream(fos));
                TarArchiveOutputStream tarOs = new TarArchiveOutputStream(gos);) {
            // Using input name to create output name
            final File folder = new File(source);
            final File[] fileNames = folder.listFiles();
            for (final File file : fileNames) {
                log.debug("PATH {}", file.getAbsolutePath());
                log.debug("File name {}", file.getName());
                addFileToTarGz(tarOs, file.getAbsolutePath(), "");
            }
            return outtarfile;
        }
        catch (final IOException e) {
            log.error("Cannot create tar file from source {} in dir {}: {}", source, outdir, e);
        }
        return "none";
    }

    /**
     * @param tOut
     *            the TarArchiveOutputStream
     * @param path
     *            the String
     * @param base
     *            the String
     * @throws IOException
     *             If an Exception occurred
     * @return
     */
    private void addFileToTarGz(TarArchiveOutputStream tOut, String path, String base)
            throws IOException {
        final File f = new File(path);
        log.debug("check if path {} exists...{}", path, f.exists());
        final String entryName = base + f.getName();
        final TarArchiveEntry tarEntry = new TarArchiveEntry(f, entryName);
        tOut.putArchiveEntry(tarEntry);
        if (f.isFile()) {
            IOUtils.copy(new FileInputStream(f), tOut);
            tOut.closeArchiveEntry();
        }
        else {
            tOut.closeArchiveEntry();
            final File[] children = f.listFiles();
            if (children != null) {
                for (final File child : children) {
                    log.debug(child.getName());
                    addFileToTarGz(tOut, child.getAbsolutePath(), entryName + "/");
                }
            }
        }
    }

}
