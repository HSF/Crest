package hep.crest.server.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.server.exceptions.AlreadyExistsPojoException;
import hep.crest.server.exceptions.NotExistsPojoException;
import hep.crest.server.services.DirectoryService;
import hep.crest.server.services.GlobalTagService;
import hep.crest.server.services.IovService;
import hep.crest.server.services.PayloadService;
import hep.crest.server.services.TagService;
import hep.crest.swagger.model.GlobalTagDto;
import hep.crest.swagger.model.IovDto;
import hep.crest.swagger.model.PayloadDto;
import hep.crest.swagger.model.TagDto;
import hep.crest.testutils.DataGenerator;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ActiveProfiles("test")
public class TestCrestServices {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private GlobalTagService globaltagService;
    @Autowired
    private TagService tagService;
    @Autowired
    private IovService iovService;
    @Autowired
    private PayloadService payloadService;
    @Autowired
    private DirectoryService directoryService;

    @Autowired
    @Qualifier("jacksonMapper")
    private ObjectMapper mapper;

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
    }

    @Test
    public void testA_Globaltags() {
        final GlobalTagDto dto = DataGenerator.generateGlobalTagDto("TEST-GT");
        try {
            final GlobalTagDto saved = globaltagService.insertGlobalTag(dto);
            saved.description("this is an updated description");
            final GlobalTagDto updated = globaltagService.updateGlobalTag(saved);
            assertThat(updated.getDescription()).isNotEqualTo(dto.getDescription());
            updated.name("ANOTHER-UNEXISTING-GT");
            updated.description("this should not be updated");
            globaltagService.updateGlobalTag(updated);
        }
        catch (CdbServiceException | AlreadyExistsPojoException e) {
            log.info("Cannot save global tag {}: {}", dto, e);
        }
        catch (final NotExistsPojoException e) {
            log.info("Cannot update global tag {}: {}", dto, e);
        }
    }

    @Test
    public void testB_Tags() {
        final TagDto dto = DataGenerator.generateTagDto("MY-TEST-01","time");
        try {
            final TagDto saved = tagService.insertTag(dto);
            saved.description("this is an updated tag description");
            final TagDto updated = tagService.updateTag(saved);
            assertThat(updated.getDescription()).isNotEqualTo(dto.getDescription());
            updated.name("ANOTHER-UNEXISTING-TAG");
            updated.description("this should not be updated");
            tagService.updateTag(updated);
        }
        catch (CdbServiceException | AlreadyExistsPojoException e) {
            log.info("Cannot save global tag {}: {}", dto, e);
        }
        catch (final NotExistsPojoException e) {
            log.info("Cannot update global tag {}: {}", dto, e);
        }
    }

    @Test
    public void testC_Iovs() {
        final PayloadDto dto = DataGenerator.generatePayloadDto("afakehashp10", "",
                "some other info", "test");
        try {
            payloadService.insertPayload(dto);
        }
        catch (final CdbServiceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        final IovDto iovdto = DataGenerator.generateIovDto("afakehashp10", "MY-TEST-01", new BigDecimal(1000L));
        try {
            iovService.insertIov(iovdto);
        }
        catch (NotExistsPojoException | AlreadyExistsPojoException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            final List<IovDto> iovlist = iovService.findAllIovs(null, PageRequest.of(0,10));
            assertThat(iovlist.size()).isGreaterThan(0);
        }
        catch (final CdbServiceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    @Test
    public void testD_Directory() {
        directoryService.dumpTag("MY-TEST-01", null, "");
        final TagDto tobesaved = DataGenerator.generateTagDto("MY-TEST-02","time");
        final TagDto saved = directoryService.insertTag(tobesaved);
        assertThat(saved).isNotNull();
        final TagDto dto = directoryService.getTag("MY-TEST-02");
        assertThat(dto).isNotNull();
        final List<IovDto> iovlist = directoryService.listIovs("MY-TEST-01");
        assertThat(iovlist).isNotNull();
        final TagDto dtonotthere = directoryService.getTag("MY-TEST-NOT-THERE");
        assertThat(dtonotthere).isNull();
        final List<IovDto> iovlistempty = directoryService.listIovs("MY-TEST-NOT-THERE");
        assertThat(iovlistempty.isEmpty()).isTrue();
    }

    
}
