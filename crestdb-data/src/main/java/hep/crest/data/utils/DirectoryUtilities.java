package hep.crest.data.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
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

	// public final String basedir = "/tmp/cdms"; // This should be configurable

	private final String tagfile = "tag.json";
	private final String iovfile = "iovs.json";
	private final String payloaddir = "data";

	private Charset charset = Charset.forName("UTF-8");
	private ObjectMapper mapper = new ObjectMapper();

	private String _basedir = "/tmp/cdms";

	public DirectoryUtilities() {
		super();
	}

	public DirectoryUtilities(String basedir) {
		_basedir = basedir;
	}

	public ObjectMapper getMapper() {
		return mapper;
	}

	public void setMapper(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	public String getTagfile() {
		return tagfile;
	}

	public String getIovfile() {
		return iovfile;
	}

	public Charset getCharset() {
		return charset;
	}

	public Path getBasePath() throws CdbServiceException {
		return this.getBasePath(_basedir);
	}

	public Path getTagPath(String tagname) throws CdbServiceException {
		return this.getTagPath(_basedir, tagname);
	}

	public Path getTagFilePath(String tagname) throws CdbServiceException {
		return this.getTagFilePath(_basedir, tagname);
	}

	public Path getIovFilePath(String tagname) throws CdbServiceException {
		return this.getIovFilePath(_basedir, tagname);
	}

	public List<String> getTagDirectories() throws CdbServiceException {
		return this.getTagDirectories(_basedir);
	}

	public Path getPayloadPath() {
		return getPayloadPath(_basedir);
	}

	public Path createIfNotexistsTag(String name) {
		return createIfNotexistsTag(_basedir, name);
	}

	public Path createIfNotexistsIov(String name) throws CdbServiceException {
		return createIfNotexistsIov(_basedir, name);
	}

	public Path getTagPath(String basedir, String tagname) throws CdbServiceException {
		Path tagpath = Paths.get(basedir, tagname);
		if (Files.notExists(tagpath)) {
			throw new CdbServiceException("Cannot find directory for tag name " + tagname);
		}
		return tagpath;
	}

	public Path getTagFilePath(String basedir, String tagname) throws CdbServiceException {
		Path tagpath = getTagPath(basedir, tagname);
		Path tagfilepath = Paths.get(tagpath.toString(), tagfile);
		if (Files.notExists(tagfilepath)) {
			throw new CdbServiceException("Cannot find tag file for tag name " + tagname);
		}
		return tagfilepath;
	}

	public Path getIovFilePath(String basedir, String tagname) throws CdbServiceException {
		Path tagpath = getTagPath(basedir, tagname);
		Path iovfilepath = Paths.get(tagpath.toString(), iovfile);
		if (Files.notExists(iovfilepath)) {
			throw new CdbServiceException("Cannot find tag file for tag name " + tagname);
		}
		return iovfilepath;
	}

	public List<String> getTagDirectories(String basedir) {
		Path basedirpath = Paths.get(basedir);
		List<Path> files;
		try {
			files = Files.walk(basedirpath).collect(Collectors.toList());
			List<String> tagnames = files.stream().filter(s -> (s.getFileName().toString().contains("tag.json")))
					.map(x -> new String(x.getName(x.getNameCount() - 2).toString())).collect(Collectors.toList());
			return tagnames;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public Path getBasePath(String basedir) {
		Path base = Paths.get(basedir);
		log.info("creating directory " + base);
		if (Files.notExists(base)) {
			// create the directory
			try {
				Files.createDirectories(base);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return base;
	}

	public Path getPayloadPath(String basedir) {
		Path ppath = Paths.get(basedir, payloaddir);
		log.info("creating directory if does not exists " + ppath);
		if (Files.notExists(ppath)) {
			// create the directory
			try {
				Files.createDirectories(ppath);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ppath;
	}

	public Path createIfNotexistsTag(String basedir, String name) {
		String tagname = name;
		Path tagpath = Paths.get(basedir, tagname);
		if (Files.exists(tagpath)) {
			return tagpath;
		} else {
			try {
				Files.createDirectories(tagpath);
				return tagpath;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	public Path createIfNotexistsIov(String basedir, String name) throws CdbServiceException {
		String tagname = name;
		Path tagpath = Paths.get(basedir, tagname);
		if (Files.notExists(tagpath)) {
			throw new CdbServiceException("Cannot find tag for tag name " + tagname);
		} else {
			try {
				Path iovfilepath = Paths.get(basedir, tagname, iovfile);
				if (Files.notExists(iovfilepath)) {
					Files.createFile(iovfilepath);
				}
				return iovfilepath;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				throw new CdbServiceException("Error in finding or creating iov file for tag " + name);
			}
		}
	}

	public String hashdir(String hash) {
		return hash.substring(0, 2);
	}

	public Boolean existsFile(Path apath, String filename) {
		if (Files.notExists(apath)) {
			return false;
		}
		Path filepath = Paths.get(apath.toString(), filename);
		if (Files.exists(filepath)) {
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @param source
	 */
	public String createTarFile(String source, String outdir) {
		TarArchiveOutputStream tarOs = null;
		try {
			// Using input name to create output name
			String outtarfile = outdir.concat(".tar.gz");
			FileOutputStream fos = new FileOutputStream(outdir.concat(".tar.gz"));
			GZIPOutputStream gos = new GZIPOutputStream(new BufferedOutputStream(fos));
			tarOs = new TarArchiveOutputStream(gos);
			File folder = new File(source);
			File[] fileNames = folder.listFiles();
			for (File file : fileNames) {
				log.debug("PATH " + file.getAbsolutePath());
				log.debug("File name " + file.getName());
//				addFilesToTarGZ(file.getAbsolutePath(), file, tarOs);
				addFileToTarGz(tarOs,file.getAbsolutePath(), "" );
			}
			return outtarfile;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				tarOs.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
		log.debug("check if exists..."+f.exists());
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
