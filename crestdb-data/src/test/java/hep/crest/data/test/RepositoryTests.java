package hep.crest.data.test;


import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

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

import hep.crest.data.handlers.PayloadHandler;
import hep.crest.data.pojo.Iov;
import hep.crest.data.pojo.IovId;
import hep.crest.data.pojo.Payload;
import hep.crest.data.pojo.Tag;
import hep.crest.data.repositories.IovDirectoryImplementation;
import hep.crest.data.repositories.IovGroupsImpl;
import hep.crest.data.repositories.IovRepository;
import hep.crest.data.repositories.PayloadDataDBImpl;
import hep.crest.data.repositories.PayloadDirectoryImplementation;
import hep.crest.data.repositories.TagDirectoryImplementation;
import hep.crest.data.repositories.TagRepository;
import hep.crest.data.test.tools.DataGenerator;
import hep.crest.data.utils.DirectoryUtilities;
import hep.crest.swagger.model.IovDto;
import hep.crest.swagger.model.PayloadDto;
import hep.crest.swagger.model.TagDto;
import hep.crest.swagger.model.TagSummaryDto;

@RunWith(SpringRunner.class)
@DataJpaTest
@ActiveProfiles("test")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RepositoryTests {

 
    private static final String SORT_PATTERN = "([a-zA-Z0-9_\\-\\.]+?)(:)([ASC|DESC]+?),";
    private static final String QRY_PATTERN = "([a-zA-Z0-9_\\-\\.]+?)(:|<|>)([a-zA-Z0-9_\\-\\/\\.\\*\\%]+?),";

    private final Logger log = LoggerFactory.getLogger(this.getClass());


    @Autowired
    private TagRepository tagrepository;

    @Autowired
    private IovRepository iovrepository;

    @Autowired
    @Qualifier("dataSource") 
    private DataSource mainDataSource;
    

    @Test
    public void testPayload() throws Exception {
        
        final PayloadDataDBImpl repobean = new PayloadDataDBImpl(mainDataSource);
        final PayloadHandler handler = new PayloadHandler();
        repobean.setPayloadHandler(handler);

        final PayloadDto dto = DataGenerator.generatePayloadDto("myhash1", "mydata", "mystreamer",
                "test");
        log.debug("Save payload {}", dto);
        if (dto.getSize() == null) {
            dto.setSize(dto.getData().length);
        }
        final Payload saved = repobean.save(dto);
        assertThat(saved).isNotNull();
        final Payload loaded = repobean.find("myhash1");
        assertThat(loaded.toString().length()).isGreaterThan(0);
        
        DataGenerator.generatePayloadData("/tmp/cdms/payloadata.blob","none");
        final File f = new File("/tmp/cdms/payloadata.blob");
        InputStream ds = new BufferedInputStream(new FileInputStream(f));
        
        dto.hash("mynewhash1");
        final Payload savedfromblob = repobean.save(dto,ds);
        assertThat(savedfromblob.toString().length()).isGreaterThan(0);
        if (ds != null) {
            ds.close();
        }
        final Payload loadedblob = repobean.findData(savedfromblob.getHash());
        assertThat(loadedblob.toString().length()).isGreaterThan(0);
        repobean.delete(loadedblob.getHash());
        
        ds = new BufferedInputStream(new FileInputStream(f));
        handler.saveStreamToFile(ds, "/tmp/cdms/payloadatacopy.blob");
        final File f1 = new File("/tmp/cdms/payloadatacopy.blob");
        final InputStream ds1 = new BufferedInputStream(new FileInputStream(f1));
        final byte[] barr = handler.getBytesFromInputStream(ds1);
        assertThat(barr.length).isGreaterThan(0);
        if (ds1 != null) {
            ds1.close();
        }
        final Payload loadedblob1 = repobean.find(savedfromblob.getHash());
        assertThat(loadedblob1.toString().length()).isGreaterThan(0);

        final PayloadDto pdto = handler.convertToDto(loadedblob1);
        assertThat(pdto).isNotNull();
        assertThat(pdto.getHash()).isEqualTo(loadedblob1.getHash());
    }
    
    @Test
    public void testIovs() throws Exception {
        
        final IovGroupsImpl iovsrepobean = new IovGroupsImpl(mainDataSource);
        final PayloadDataDBImpl repobean = new PayloadDataDBImpl(mainDataSource);
        
        final PayloadDto dto = DataGenerator.generatePayloadDto("myhash2", "mynewdata", "mystreamer",
                "test");
        log.debug("Save payload {}", dto);
        if (dto.getSize() == null) {
            dto.setSize(dto.getData().length);
        }
        final Payload saved = repobean.save(dto);
        assertThat(saved).isNotNull();
        final Payload loaded = repobean.find("myhash2");
      
        
        final Tag mtag = DataGenerator.generateTag("A-TEST-01", "test");
        final Tag savedtag = tagrepository.save(mtag);
        final IovId id = new IovId("A-TEST-01",new BigDecimal(999L), new Date());
        final Iov miov = new Iov(id,savedtag,loaded.getHash());
        final Iov savediov = iovrepository.save(miov);
        assertThat(savediov.toString().length()).isGreaterThan(0);
        log.info("Stored iov {}",savediov);

        final IovId id2 = new IovId("A-TEST-01",new BigDecimal(1999L), new Date());
        final Iov miov2 = new Iov(id2,savedtag,loaded.getHash());
        final Iov savediov2 = iovrepository.save(miov2);
        log.info("Stored iov2 {}",savediov2);
        assertThat(savediov2.toString().length()).isGreaterThan(0);
        assertThat(id2.hashCode()).isNotNull();
        assertThat(id2.equals(id)).isFalse();
        
        final Iterable<Iov> storedlist = iovrepository.findAll();
        for (final Iov iov : storedlist) {
            log.info("Found iov {}", iov);
        }
        final Long s = iovsrepobean.getSize("A-TEST-01");
        assertThat(s).isGreaterThan(0);
        
        final List<TagSummaryDto> iovlist = iovsrepobean.getTagSummaryInfo("A-TEST-01");
        assertThat(iovlist.size()).isGreaterThan(0);
        
        final List<BigDecimal> groups = iovsrepobean.selectGroups("A-TEST-01", 10L);
        assertThat(groups.size()).isGreaterThan(0);

    }
   
    @Test
    public void testDirectories() throws Exception {
        final TagDirectoryImplementation tagrepo = new TagDirectoryImplementation(new DirectoryUtilities());
        final TagDto tdto = DataGenerator.generateTagDto("A-TEST-02", "test");
        final TagDto savedtag = tagrepo.save(tdto);
        final TagDto loadedtag = tagrepo.findOne("A-TEST-02");
        assertThat(loadedtag.getName()).isEqualTo(savedtag.getName());
        final List<TagDto> taglist = tagrepo.findByNameLike("A-TEST.*");
        assertThat(taglist.size()).isGreaterThan(0);
        assertThat(tagrepo.exists("A-TEST-02")).isTrue();

        final PayloadDirectoryImplementation pyldrepo = new PayloadDirectoryImplementation(new DirectoryUtilities());
        final PayloadDto pdto = DataGenerator.generatePayloadDto("anotherhash", "some content", "sinfo", "test");
        pyldrepo.save(pdto);
 
        final IovDirectoryImplementation iovrepo = new IovDirectoryImplementation(new DirectoryUtilities());
        final IovDto idto = DataGenerator.generateIovDto("anotherhash", "A-TEST-02", new BigDecimal(22222L));
        iovrepo.save(idto);
        assertThat(tdto).isNotNull();
    }
    
    
}
