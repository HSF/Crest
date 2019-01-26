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

public class DirectoryUtilities {

	private static final Logger log = LoggerFactory.getLogger(DirectoryUtilities.class);

	private static final String TAG_FILE = "tag.json";
	private static final String IOV_FILE = "iovs.json";
	private static final String PAYLOAD_DIR = "data";

	private Charset charset = Charset.forName("UTF-8");
	private ObjectMapper mapper = new ObjectMapper();

	private String locbasedir = "/tmp/cdms";

	public DirectoryUtilities() {
		super();
	}

	public DirectoryUtilities(String basedir) {
		locbasedir = basedir;
	}

	public ObjectMapper getMapper() {
		return mapper;
	}

	public void setMapper(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	public String getTagfile() {
		return TAG_FILE;
	}

	public String getIovfile() {
		return IOV_FILE;
	}

	public Charset getCharset() {
		return charset;
	}

	public Path getBasePath() {
		return this.getBasePath(locbasedir);
	}

	public Path getTagPath(String tagname) throws CdbServiceException {
		return this.getTagPath(locbasedir, tagname);
	}

	public Path getTagFilePath(String tagname) throws CdbServiceException {
		return this.getTagFilePath(locbasedir, tagname);
	}

	public Path getIovFilePath(String tagname) throws CdbServiceException {
		return this.getIovFilePath(locbasedir, tagname);
	}

	public List<String> getTagDirectories() {
		return this.getTagDirectories(locbasedir);
	}

	public Path getPayloadPath() {
		return getPayloadPath(locbasedir);
	}

	public Path createIfNotexistsTag(String name) {
		return createIfNotexistsTag(locbasedir, name);
	}

	public Path createIfNotexistsIov(String name) throws CdbServiceException {
		return createIfNotexistsIov(locbasedir, name);
	}

	public Path getTagPath(String basedir, String tagname) throws CdbServiceException {
		Path tagpath = Paths.get(basedir, tagname);
		if (!tagpath.toFile().exists()) {
			throw new CdbServiceException("Cannot find directory for tag name " + tagname);
		}
		return tagpath;
	}

	/**
	 * @param basedir
	 * @param tagname
	 * @return
	 * @throws CdbServiceException
	 */
	public Path getTagFilePath(String basedir, String tagname) throws CdbServiceException {
		Path tagpath = getTagPath(basedir, tagname);
		Path tagfilepath = Paths.get(tagpath.toString(), TAG_FILE);
		if (!tagfilepath.toFile().exists()) {
			throw new CdbServiceException("Cannot find tag file for tag name " + tagname);
		}
		return tagfilepath;
	}

	/**
	 * @param basedir
	 * @param tagname
	 * @return
	 * @throws CdbServiceException
	 */
	public Path getIovFilePath(String basedir, String tagname) throws CdbServiceException {
		Path tagpath = getTagPath(basedir, tagname);
		Path iovfilepath = Paths.get(tagpath.toString(), IOV_FILE);
		if (!iovfilepath.toFile().exists()) {
			throw new CdbServiceException("Cannot find iov file for tag name " + tagname);
		}
		return iovfilepath;
	}

	/**
	 * @param basedir
	 * @return
	 */
	public List<String> getTagDirectories(String basedir) {
		Path basedirpath = Paths.get(basedir);
		List<Path> pfiles;
		try (Stream<Path> pstream = Files.walk(basedirpath);) {
			pfiles = pstream.collect(Collectors.toList());
			return pfiles.stream().filter(s -> (s.getFileName().toString().contains(TAG_FILE)))
					.map(x -> (x.getName(x.getNameCount() - 2).toString())).collect(Collectors.toList());
		} catch (IOException e) {
			log.error("Error getting tags directories from {}",basedirpath);
		}
		return new ArrayList<>();
	}

	/**
	 * @param basedir
	 * @return
	 */
	public Path getBasePath(String basedir) {
		Path base = Paths.get(basedir);
		log.info("creating directory {}",base);
		if (!base.toFile().exists()) {
			// create the directory
			try {
				Files.createDirectories(base);
			} catch (IOException e) {
				log.error("Error creating base directory {}",base);
			}
		}
		return base;
	}

	/**
	 * @param basedir
	 * @return
	 */
	public Path getPayloadPath(String basedir) {
		Path ppath = Paths.get(basedir, PAYLOAD_DIR);
		log.info("creating directory if does not exists {}",ppath);
		if (!ppath.toFile().exists()) {
			// create the directory
			try {
				Files.createDirectories(ppath);
			} catch (IOException e) {
				log.error("Error creating directory for payload {}",ppath);
			}
		}
		return ppath;
	}

	/**
	 * @param basedir
	 * @param name
	 * @return
	 */
	public Path createIfNotexistsTag(String basedir, String name) {
		String tagname = name;
		Path tagpath = Paths.get(basedir, tagname);
		if (tagpath.toFile().exists()) {
			return tagpath;
		} else {
			try {
				Files.createDirectories(tagpath);
				return tagpath;
			} catch (IOException e) {
				log.error("Error creating directory for tag {}",tagpath);
			}
		}
		return null;
	}

	/**
	 * @param basedir
	 * @param name
	 * @return
	 * @throws CdbServiceException
	 */
	public Path createIfNotexistsIov(String basedir, String name) throws CdbServiceException {
		String tagname = name;
		Path tagpath = Paths.get(basedir, tagname);
		if (!tagpath.toFile().exists()) {
			throw new CdbServiceException("Cannot find tag for tag name " + tagname);
		} else {
			try {
				Path iovfilepath = Paths.get(basedir, tagname, IOV_FILE);
				if (!iovfilepath.toFile().exists()) {
					Files.createFile(iovfilepath);
				}
				return iovfilepath;
			} catch (IOException e) {
				throw new CdbServiceException("Error in finding or creating iov file for tag " + name);
			}
		}
	}

	/**
	 * @param hash
	 * @return
	 */
	public String hashdir(String hash) {
		return hash.substring(0, 2);
	}

	/**
	 * @param apath
	 * @param filename
	 * @return
	 */
	public Boolean existsFile(Path apath, String filename) {
		if (!apath.toFile().exists()) {
			return false;
		}
		Path filepath = Paths.get(apath.toString(), filename);
		return filepath.toFile().exists();
	}

	/**
	 * 
	 * @param source
	 */
	public String createTarFile(String source, String outdir) {
		try (FileOutputStream fos = new FileOutputStream(outdir.concat(".tar.gz"));
			 GZIPOutputStream gos = new GZIPOutputStream(new BufferedOutputStream(fos));
			 TarArchiveOutputStream tarOs = new TarArchiveOutputStream(gos);
			){
			// Using input name to create output name
			String outtarfile = outdir.concat(".tar.gz");
			File folder = new File(source);
			File[] fileNames = folder.listFiles();
			for (File file : fileNames) {
				log.debug("PATH {}",file.getAbsolutePath());
				log.debug("File name {}",file.getName());
				addFileToTarGz(tarOs,file.getAbsolutePath(), "" );
			}
			return outtarfile;
		} catch (IOException e) {
			log.error("Cannot create tar file from source {} in dir {}",source,outdir);
		} 
		return "none";
	}

	/**
	 * @param tOut
	 * @param path
	 * @param base
	 * @throws IOException
	 */
	private void addFileToTarGz(TarArchiveOutputStream tOut, String path, String base) throws IOException {
		File f = new File(path);
		log.debug("check if path {} exists...{}",path,f.exists());
		String entryName = base + f.getName();
		TarArchiveEntry tarEntry = new TarArchiveEntry(f, entryName);
		tOut.putArchiveEntry(tarEntry);
		if (f.isFile()) {
			IOUtils.copy(new FileInputStream(f), tOut);
			tOut.closeArchiveEntry();
		} else {
			tOut.closeArchiveEntry();
			File[] children = f.listFiles();
			if (children != null) {
				for (File child : children) {
					log.debug(child.getName());
					addFileToTarGz(tOut, child.getAbsolutePath(), entryName + "/");
				}
			}
		}
	}

}
