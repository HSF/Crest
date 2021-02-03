package hep.crest.server.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.pojo.Tag;
import hep.crest.server.exceptions.AlreadyExistsPojoException;
import hep.crest.server.exceptions.NotExistsPojoException;
import hep.crest.server.services.TagService;
import hep.crest.swagger.model.TagDto;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ActiveProfiles("test")
public class TestCrestTag {

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
    public void test_TagService() {
        final TagDto dto = DataGenerator.generateTagDto("SVC-TAG-01", "test");
      
        try {
            Tag entity = mapperFacade.map(dto, Tag.class);
            final Tag saved = tagservice.insertTag(entity);
            assertThat(saved).isNotNull();
        }
        catch (AlreadyExistsPojoException e) {
            log.info("got exception of type {}",e.getClass());
        }
        try {
            tagservice.exists(null);
        }
        catch (final CdbServiceException e) {
            log.info("got exception of type {}",e.getClass());
        }
        try {
            final Tag dtonull = tagservice.findOne(null);
            assertThat(dtonull).isNull();
        }
        catch (final NotExistsPojoException e) {
            log.info("got exception of type {}",e.getClass());
        }
        final List<String> ids = new ArrayList<>();
        ids.add("SVC-TAG-01");
        tagservice.findAllTags(ids);
    }

    @Test
    public void testA_getAndRemoveTags() {
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
        assertThat(response.getBody().getSize()).isNotNegative();
    }

    @Test
    public void testB_storeTags() {
        final TagDto dto = DataGenerator.generateTagDto("B-TAG-02", "test");
        log.info("Store tag : {} ", dto);
        final ResponseEntity<TagDto> response = this.testRestTemplate
                .postForEntity("/crestapi/tags", dto, TagDto.class);
        log.info("Received response: {}", response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        log.info("Try to store tag again : {} ", dto);
        final ResponseEntity<String> response1 = this.testRestTemplate
                .postForEntity("/crestapi/tags", dto, String.class);
        log.info("Received response: {}", response1);
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.SEE_OTHER);

        dto.name(null);
        log.info("Try to use null name in tag again : {} ", dto);
        final ResponseEntity<String> response2 = this.testRestTemplate
                .postForEntity("/crestapi/tags", dto, String.class);
        log.info("Received response: {}", response2);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        
        final ResponseEntity<String> responsedelete = this.testRestTemplate
                .exchange("/crestapi/admin/tags/B-TAG-02", HttpMethod.DELETE, null, String.class);
        log.info("Received response on delete: {}", responsedelete);
        assertThat(responsedelete.getStatusCode()).isEqualTo(HttpStatus.OK);

    }

    @Test
    public void testC_getAllTags() {
        final ResponseEntity<TagSetDto> response = this.testRestTemplate
                .getForEntity("/crestapi/tags", TagSetDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getSize()).isNotNegative();
    }

    @Test
    public void testC_findTags() throws Exception {

        // Successfull create new tag
        final TagDto dto = DataGenerator.generateTagDto("B-TAG-03", "test");
        log.info("Store tag : {} ", dto);
        final ResponseEntity<TagDto> response = this.testRestTemplate
                .postForEntity("/crestapi/tags", dto, TagDto.class);
        log.info("Received response: {}", response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Successfull create new tag to remove it
        final TagDto dtorm = DataGenerator.generateTagDto("B-TAG-RM", "test");
        log.info("Store tag : {} ", dtorm);
        final ResponseEntity<TagDto> responserm = this.testRestTemplate
                .postForEntity("/crestapi/tags", dtorm, TagDto.class);
        log.info("Received response: {}", responserm);
        assertThat(responserm.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        final ResponseEntity<String> resprm = this.testRestTemplate
                .exchange("/crestapi/admin/tags/B-TAG-RM", HttpMethod.DELETE, null, String.class);
        {
            log.info("Remove tag B-TAG-RM ");
            assertThat(resprm.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        // Successfull create new tag
        final TagDto dto1 = DataGenerator.generateTagDto("B-TAG-04", "test");
        log.info("Store tag : {} ", dto1);
        final ResponseEntity<TagDto> response1 = this.testRestTemplate
                .postForEntity("/crestapi/tags", dto1, TagDto.class);
        log.info("Received response: {}", response1);
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Successfull list tags
        final ResponseEntity<String> resp = this.testRestTemplate.exchange("/crestapi/tags",
                HttpMethod.GET, null, String.class);

        {
            log.info("Retrieved all tags {} ", resp.getBody());
            final String responseBody = resp.getBody();
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            TagSetDto ok;
            log.info("Response from server is: " + responseBody);
            ok = mapper.readValue(responseBody, TagSetDto.class);
            assertThat(ok.getSize()).isPositive();
        }

        // Successfull find one tag resource
        final ResponseEntity<String> resp1 = this.testRestTemplate
                .exchange("/crestapi/tags/" + dto1.getName(), HttpMethod.GET, null, String.class);
        {
            log.info("Retrieved tag {} ", dto1.getName());
            final String responseBody = resp1.getBody();
            assertThat(resp1.getStatusCode()).isEqualTo(HttpStatus.OK);
            TagSetDto ok;
            log.info("Response from server is: " + responseBody);
            ok = mapper.readValue(responseBody, TagSetDto.class);
            assertThat(ok.getSize()).isEqualTo(1);
        }

        // Update a tag
        final TagDto body = dto1;
        body.setDescription("another description updated");
        body.endOfValidity(new BigDecimal(1000L));
        body.synchronization("blkp");
        body.payloadSpec("newspec");
        final HttpEntity<TagDto> updrequest = new HttpEntity<TagDto>(body);

        final ResponseEntity<String> respupd = this.testRestTemplate
                .exchange("/crestapi/tags/" + dto1.getName(), HttpMethod.PUT, updrequest, String.class);
        {
            log.info("Update tag {} ", body.getName());
            final String responseBody = respupd.getBody();
            assertThat(respupd.getStatusCode()).isEqualTo(HttpStatus.OK);
            TagDto ok;
            log.info("Response from server is: " + responseBody);
            ok = mapper.readValue(responseBody, TagDto.class);
            assertThat(ok).isNotNull();
            assertThat(ok.getSynchronization()).isEqualTo("blkp");
        }
        final ResponseEntity<String> respupdnull = this.testRestTemplate
                .exchange("/crestapi/tags/" + dto1.getName(), HttpMethod.PUT, null, String.class);
        assertThat(respupdnull.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        final ResponseEntity<String> respupdnotexist = this.testRestTemplate
                .exchange("/crestapi/tags/NOT-THERE", HttpMethod.PUT, null, String.class);
        assertThat(respupdnotexist.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        final ResponseEntity<String> resp1null = this.testRestTemplate
                .exchange("/crestapi/tags/SOME-T", HttpMethod.GET, null, String.class);
        {
            log.info("Retrieved tag SOME-T should return null");
            assertThat(resp1null.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        final ResponseEntity<String> resprmnotthere = this.testRestTemplate
                .exchange("/crestapi/admin/tags/NOT-THERE", HttpMethod.DELETE, null, String.class);
        {
            log.info("Remove tag NOT-THERE ");
            assertThat(resprmnotthere.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        final ResponseEntity<String> resp2 = this.testRestTemplate
                .exchange("/crestapi/tags?by=name:TAG,insertiontime>0", HttpMethod.GET, null, String.class);

        {
            log.info("Retrieved tag list " + resp2.getBody());
            final String responseBody = resp2.getBody();
            assertThat(resp2.getStatusCode()).isEqualTo(HttpStatus.OK);
            TagSetDto ok;
            log.info("Response from server is: " + responseBody);
            ok = mapper.readValue(responseBody, TagSetDto.class);
            assertThat(ok.getSize()).isPositive();
        }

        final ResponseEntity<String> resp2a = this.testRestTemplate
                .exchange("/crestapi/tags?by=name:PIPPO,insertiontime<0", HttpMethod.GET, null, String.class);

        {
            log.info("Retrieved null tag list {} with status {}", resp2a.getBody(), resp2a.getStatusCode());
            final String responseBody = resp2a.getBody();
            assertThat(resp2a.getStatusCode()).isGreaterThanOrEqualTo(HttpStatus.OK);
        }

        // This should trigger a runtime exception on server side. The field used in the SORT expression does not exists.
        final ResponseEntity<String> resp2b = this.testRestTemplate
                .exchange("/crestapi/tags?by=name:PIPPO,insertiontime:0&sort=PIPPO:DESC", HttpMethod.GET, null, String.class);

        {
            log.info("Retrieved bad sql tag list {} with status {}", resp2b.getBody(), resp2b.getStatusCode());
            final String responseBody = resp2b.getBody();
            assertThat(resp2b.getStatusCode()).isGreaterThanOrEqualTo(HttpStatus.NOT_FOUND);
        }

    }

}
