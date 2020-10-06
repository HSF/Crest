package hep.crest.server.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.pojo.Tag;
import hep.crest.server.exceptions.AlreadyExistsPojoException;
import hep.crest.server.services.TagService;
import hep.crest.swagger.model.TagDto;
import hep.crest.swagger.model.TagMetaDto;
import hep.crest.swagger.model.TagSetDto;
import hep.crest.testutils.DataGenerator;
import ma.glasnost.orika.MapperFacade;
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
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ContextConfiguration
@ActiveProfiles("test")
public class TestCrestMetaTag {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private TagService tagservice;
    
    @Autowired
    @Qualifier("jacksonMapper")
    private ObjectMapper mapper;
    @Autowired
    private MapperFacade mapperFacade;

    @Test
    public void test_TagMetaService() {
        Instant now = Instant.now();
        final TagDto dto = DataGenerator.generateTagDto("SVC-TAG-01", "test");
        
        try {
            Tag entity = mapperFacade.map(dto, Tag.class);
            final Tag saved = tagservice.insertTag(entity);
            assertThat(saved).isNotNull();
        }
        catch (AlreadyExistsPojoException e) {
            log.info("got exception of type {}",e.getClass());
        }

        final TagMetaDto metadto = DataGenerator.generateTagMetaDto("SVC-TAG-01", "test meta info", new Date(now.toEpochMilli()));
      
        try {
            final TagMetaDto saved = tagservice.insertTagMeta(metadto);
            assertThat(saved).isNotNull();
        }
        catch (AlreadyExistsPojoException e) {
            log.info("got exception of type {}",e.getClass());
        }
        catch (CdbServiceException e) {
            log.info("got exception of type {}",e.getClass());
        }
    }

    @Test
    public void testA_getAndRemoveTagAndMetas() {
        final ResponseEntity<TagSetDto> response = this.testRestTemplate
                .getForEntity("/crestapi/tags", TagSetDto.class);
        log.info("Found response {}", response.getBody().toString());
        final TagSetDto tagset = response.getBody();
        for (final TagDto tagDto : tagset.getResources()) {
            final String url = "/crestapi/admin/tags/" + tagDto.getName();
            log.info("Removing tag {}", url);
            this.testRestTemplate.delete(url);
        }
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getSize()).isGreaterThanOrEqualTo(0);
    }

    @Test
    public void testB_storeTagsAndMeta() {
        final TagDto dto = DataGenerator.generateTagDto("B-TAG-02", "test");
        log.info("Store tag : {} ", dto);
        final ResponseEntity<TagDto> response = this.testRestTemplate
                .postForEntity("/crestapi/tags", dto, TagDto.class);
        log.info("Received response: {}", response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        final TagMetaDto metadto = DataGenerator.generateTagMetaDto("B-TAG-02", "test meta info", new Date());
        log.info("Store tag meta : {} ", metadto);
        final ResponseEntity<TagMetaDto> response1 = this.testRestTemplate
                .postForEntity("/crestapi/tags/"+dto.getName()+"/meta", metadto, TagMetaDto.class);
        log.info("Received response: {}", response1);
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    }

}
