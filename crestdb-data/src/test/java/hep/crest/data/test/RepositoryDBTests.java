package hep.crest.data.test;

import hep.crest.data.config.PojoDtoConverterConfig;
import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.exceptions.PayloadEncodingException;
import hep.crest.data.handlers.PayloadHandler;
import hep.crest.data.pojo.Iov;
import hep.crest.data.pojo.IovId;
import hep.crest.data.pojo.Tag;
import hep.crest.data.repositories.IovDirImpl;
import hep.crest.data.repositories.IovGroupsImpl;
import hep.crest.data.repositories.IovRepository;
import hep.crest.data.repositories.PayloadDataBaseCustom;
import hep.crest.data.repositories.PayloadDataDBImpl;
import hep.crest.data.repositories.PayloadDirectoryImplementation;
import hep.crest.data.repositories.TagDirImpl;
import hep.crest.data.repositories.TagRepository;
import hep.crest.data.security.pojo.CrestFolders;
import hep.crest.data.security.pojo.FolderRepository;
import hep.crest.data.test.tools.DataGenerator;
import hep.crest.data.utils.DirectoryUtilities;
import hep.crest.swagger.model.IovDto;
import hep.crest.swagger.model.IovPayloadDto;
import hep.crest.swagger.model.PayloadDto;
import hep.crest.swagger.model.TagDto;
import hep.crest.swagger.model.TagSummaryDto;
import ma.glasnost.orika.MapperFacade;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
@ActiveProfiles("test")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RepositoryDBTests {

    private static final Logger log = LoggerFactory.getLogger(RepositoryDBTests.class);

    @Autowired
    private TagRepository tagrepository;

    @Autowired
    private IovRepository iovrepository;

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    @Qualifier("dataSource")
    private DataSource mainDataSource;

    private MapperFacade mapper;

    @Before
    public void setUp() {
        final Path bpath = Paths.get("/tmp/cdms");
        if (!bpath.toFile().exists()) {
            try {
                Files.createDirectories(bpath);
            }
            catch (final IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        final Path cpath = Paths.get("/tmp/crest-dump");
        if (!cpath.toFile().exists()) {
            try {
                Files.createDirectories(cpath);
            }
            catch (final IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        final PojoDtoConverterConfig cf = new PojoDtoConverterConfig();
        mapper = cf.createOrikaMapperFactory().getMapperFacade();
    }

    @Test
    public void testPayload() throws Exception {

        final PayloadDataBaseCustom repobean = new PayloadDataDBImpl(mainDataSource);
        final Instant now = Instant.now();
        final Date time = new Date(now.toEpochMilli());

        final PayloadDto dto = DataGenerator.generatePayloadDto("myhash1", "mydata", "mystreamer",
                "test", time);
        log.debug("Save payload {}", dto);
        if (dto.getSize() == null) {
            dto.setSize(dto.getData().length);
        }
        final PayloadDto saved = repobean.save(dto);
        assertThat(saved).isNotNull();
        final PayloadDto loaded = repobean.find("myhash1");
        assertThat(loaded.toString().length()).isPositive();

        DataGenerator.generatePayloadData("/tmp/cdms/payloadata.blob", "none");
        final File f = new File("/tmp/cdms/payloadata.blob");
        InputStream ds = new BufferedInputStream(new FileInputStream(f));

        dto.hash("mynewhash1");
        final PayloadDto savedfromblob = repobean.save(dto, ds);
        assertThat(savedfromblob.toString().length()).isPositive();
        if (ds != null) {
            ds.close();
        }
        final InputStream loadedblob = repobean.findData(savedfromblob.getHash());
        assertThat(loadedblob.available()).isPositive();
        repobean.delete(savedfromblob.getHash());

        ds = new BufferedInputStream(new FileInputStream(f));
        PayloadHandler.saveStreamToFile(ds, "/tmp/cdms/payloadatacopy.blob");
        final File f1 = new File("/tmp/cdms/payloadatacopy.blob");
        final InputStream ds1 = new BufferedInputStream(new FileInputStream(f1));
        final byte[] barr = PayloadHandler.getBytesFromInputStream(ds1);
        assertThat(barr).isNotEmpty();
        if (ds1 != null) {
            ds1.close();
        }

        ds = new BufferedInputStream(new FileInputStream(f));
        final OutputStream out = new FileOutputStream(
                new File("/tmp/cdms/payloadatacopy.blob.copy"));
        PayloadHandler.saveToOutStream(ds, out);
//        lobhandler.createBlobFromFile("/tmp/cdms/payloadatacopy.blob.copy");

        final PayloadDto loadedblob1 = repobean.find(savedfromblob.getHash());
        assertThat(loadedblob1).isNull();
        log.info("loaded payload 1 {}", loadedblob1);

        final byte[] parr = PayloadHandler.readFromFile("/tmp/cdms/payloadatacopy.blob.copy");
        assertThat(parr).isNotNull().isNotEmpty();

        final long fsize = PayloadHandler.lengthOfFile("/tmp/cdms/payloadatacopy.blob.copy");
        assertThat(fsize).isPositive();
//        final Blob bldata = lobhandler.createBlobFromByteArr(parr);
//        assertThat(bldata).isNotNull();
//        final Blob bldatastr = lobhandler
//                .createBlobFromStream(new BufferedInputStream(new FileInputStream(f)));
//        assertThat(bldatastr).isNotNull();

        final String fhash = PayloadHandler.saveToFileGetHash(
                new BufferedInputStream(new FileInputStream(f)),
                "/tmp/cdms/payloadatacopy.blob.copy2");
        assertThat(fhash).isNotNull();
    }

    @Test
    public void testIovs() throws Exception {

        final IovGroupsImpl iovsrepobean = new IovGroupsImpl(mainDataSource);
        final PayloadDataDBImpl repobean = new PayloadDataDBImpl(mainDataSource);
        final Instant now = Instant.now();
        final Date time = new Date(now.toEpochMilli());

        final PayloadDto dto = DataGenerator.generatePayloadDto("myhash2", "mynewdata",
                "mystreamer", "test", time);
        log.debug("Save payload {}", dto);
        if (dto.getSize() == null) {
            dto.setSize(dto.getData().length);
        }
        final PayloadDto saved = repobean.save(dto);
        assertThat(saved).isNotNull();
        final PayloadDto loaded = repobean.find("myhash2");

        final Tag mtag = DataGenerator.generateTag("A-TEST-01", "test");
        final Tag savedtag = tagrepository.save(mtag);
        final IovId id = new IovId("A-TEST-01", new BigDecimal(999L), new Date());
        final Iov miov = new Iov(id, savedtag, loaded.getHash());
        final Iov savediov = iovrepository.save(miov);
        assertThat(savediov.toString().length()).isPositive();
        log.info("Stored iov {}", savediov);

        final IovId id2 = new IovId("A-TEST-01", new BigDecimal(1999L), new Date());
        final Iov miov2 = new Iov(id2, savedtag, loaded.getHash());
        final Iov savediov2 = iovrepository.save(miov2);
        log.info("Stored iov2 {}", savediov2);
        assertThat(savediov2.toString().length()).isPositive();
        assertThat(id2.hashCode()).isNotZero();
        assertThat(id2).isNotEqualTo(id);

        final Iterable<Iov> storedlist = iovrepository.findAll();
        for (final Iov iov : storedlist) {
            log.info("Found iov {}", iov);
        }
        final Long s = iovsrepobean.getSize("A-TEST-01");
        assertThat(s).isPositive();

        final Long ssnap = iovsrepobean.getSizeBySnapshot("A-TEST-01", new Date());
        assertThat(ssnap).isPositive();

        final List<TagSummaryDto> iovlist = iovsrepobean.getTagSummaryInfo("A-TEST-01");
        assertThat(iovlist.size()).isPositive();

        final List<BigDecimal> groups = iovsrepobean.selectGroups("A-TEST-01", 10L);
        assertThat(groups.size()).isPositive();

        final List<BigDecimal> groupsnap = iovsrepobean.selectSnapshotGroups("A-TEST-01",
                new Date(), 10L);
        assertThat(groupsnap.size()).isPositive();

        final List<IovPayloadDto> pdtolist = iovsrepobean.getRangeIovPayloadInfo("A-TEST-01", new BigDecimal(99L),
                new BigDecimal(1200L), new Date());
        assertThat(pdtolist.size()).isNotNegative();

    }

    @Test
    public void testDirectoriesImpl() throws Exception {
        final TagDirImpl tagrepo = new TagDirImpl(
                new DirectoryUtilities(), mapper);
        final TagDto tdto = DataGenerator.generateTagDto("A-TEST-02", "test");
        Tag entity = mapper.map(tdto, Tag.class);
        log.info("Tag to be stored: {}", entity);
        final Tag savedtag = tagrepo.save(entity);
        final Tag loadedtag = tagrepo.findOne("A-TEST-02");
        assertThat(loadedtag.getName()).isEqualTo(savedtag.getName());
        final List<Tag> taglist = tagrepo.findByNameLike("A-TEST.*");
        assertThat(taglist.size()).isPositive();

        // Search all tags
        log.debug("Search all tags in directory");
        final List<Tag> alltaglist = tagrepo.findAll();
        assertThat(alltaglist.size()).isPositive();

        final PayloadDirectoryImplementation pyldrepo = new PayloadDirectoryImplementation(
                new DirectoryUtilities());
        final Instant now = Instant.now();
        final Date time = new Date(now.toEpochMilli());
        final PayloadDto pdto = DataGenerator.generatePayloadDto("anotherhash", "some content",
                "sinfo", "test", time);
        pyldrepo.save(pdto);

        log.debug("Payload saved for hash : anotherhash");
        final PayloadDto loadedp = pyldrepo.find("anotherhash");
        assertThat(loadedp).isNotNull();

        final IovDirImpl iovrepo = new IovDirImpl(
                new DirectoryUtilities(), mapper);
        log.debug("Store iov for hash : anotherhash");
        final IovDto idto = DataGenerator.generateIovDto("anotherhash", "A-TEST-02",
                new BigDecimal(22222L));
        Iov ioventity = mapper.map(idto, Iov.class);
        ioventity.getId().setTagName(idto.getTagName());
        iovrepo.save(ioventity);
        assertThat(tdto).isNotNull();

        log.debug("Store new tag A-TEST-03");
        final TagDto tdto3 = DataGenerator.generateTagDto("A-TEST-03", "test");
        Tag tagentity3 = mapper.map(tdto3, Tag.class);
        final Tag savedtag3 = tagrepo.save(tagentity3);

        log.debug("Store list of 2 iovs in tag A-TEST-03");
        final IovDto idto1 = DataGenerator.generateIovDto("anotherhash3", "A-TEST-03",
                new BigDecimal(22224L));
        final IovDto idto2 = DataGenerator.generateIovDto("anotherhash4", "A-TEST-03",
                new BigDecimal(32224L));
        final List<IovDto> dtolist = new ArrayList<>();
        dtolist.add(idto1);
        dtolist.add(idto2);
        for (IovDto d : dtolist) {
            Iov e = mapper.map(d, Iov.class);
            e.getId().setTagName(d.getTagName());
            iovrepo.save(e);
        }
        log.debug("Search iovs in tag A-TEST-03");
        final List<Iov> savedilist = iovrepo.findByIdTagName("A-TEST-03");
        assertThat(savedilist.size()).isPositive();
    }

    @Test
    public void testFolders() {
        final CrestFolders entity = DataGenerator.generateFolder("RTBLOB", "/MDT/RTBLOB",
                "COOLOFL_MDT");
        final CrestFolders saved = folderRepository.save(entity);
        assertThat(saved.getGroupRole()).isEqualTo(entity.getGroupRole());

        final List<CrestFolders> flist = folderRepository.findBySchemaName("COOLOFL_MDT");
        assertThat(flist.size()).isPositive();
    }

    @Test
    public void testLobHandlers() {
        //final CrestLobHandler clh = new CrestLobHandler(mainDataSource);
        DataGenerator.generatePayloadData("/tmp/cdms/payloadataforhandler.blob", "none");
        final File f = new File("/tmp/cdms/payloadataforhandler.blob");
        final PayloadDataDBImpl repobean = new PayloadDataDBImpl(mainDataSource);

        try {
            //final Blob b = clh.createBlobFromFile("/tmp/cdms/payloadataforhandler.blob");
            //assertThat(b).isNotNull();
            final InputStream ds = new BufferedInputStream(new FileInputStream(f));
            //final Blob bs = clh.createBlobFromStream(ds);
            //assertThat(bs).isNotNull();
            final Instant now = Instant.now();
            final Date time = new Date(now.toEpochMilli());
            ds.close();
            final PayloadDto dto = DataGenerator.generatePayloadDto("myhash3", "mynewdataforhandler",
                    "mystreamer", "test", time);
            if (dto.getSize() == null) {
                dto.setSize(dto.getData().length);
            }
            final PayloadDto saved = repobean.save(dto);
            assertThat(saved).isNotNull();
            final InputStream ds1 = new BufferedInputStream(new FileInputStream(f));
            final byte[] barr = PayloadHandler.getBytesFromInputStream(ds1);
            assertThat(barr).isNotEmpty();

        }
        catch (final IOException e) {
            log.error("Cannot create or operate on blob: {}", e.getMessage());
        }
        catch (final CdbServiceException e) {
            log.error("Cannot save payload: {}", e.getMessage());
        }
        try {
            final File fbad = new File("/tmp/cdms/payloadataforhandler.blob");
            final BufferedInputStream dsbad = new BufferedInputStream(new FileInputStream(fbad));
            dsbad.close();
            PayloadHandler.getHashFromStream(dsbad);
        }
        catch (final PayloadEncodingException e) {
            log.error("Bad stream");
        }
        catch (final FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
