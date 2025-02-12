package hep.crest.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import hep.crest.server.swagger.model.GlobalTagDto;
import hep.crest.server.swagger.model.GlobalTagMapDto;
import hep.crest.server.swagger.model.TagDto;
import hep.crest.server.utils.RandomGenerator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration
@Slf4j
public class TestCrestMappings {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    @Qualifier("jacksonMapper")
    private ObjectMapper mapper;

    private static final RandomGenerator rnd = new RandomGenerator();

    public void initializeTag(String gtname) {
        TagDto dto = (TagDto) rnd.generate(TagDto.class);
        dto.name(gtname);
        log.info("Store tag : {} ", dto);
        final ResponseEntity<TagDto> response = testRestTemplate
                .postForEntity("/crestapi/tags", dto, TagDto.class);
        log.info("Received response: {}", response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    public void initializeGtag(String gtname) {
        GlobalTagDto dto = (GlobalTagDto) rnd.generate(GlobalTagDto.class);
        dto.name(gtname);
        dto.type("N");
        log.info("Store global tag : {} ", dto);
        final ResponseEntity<GlobalTagDto> response = testRestTemplate
                .postForEntity("/crestapi/globaltags", dto, GlobalTagDto.class);
        log.info("Received response: {}", response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }


    @Test
    public void testTagMappingsRest() {
        log.info("=======> testTagMappingsRest ");
        String tagname = "A-CRESTMAPPINGS-41";
        initializeGtag("A-TEST-GT-40");
        initializeTag(tagname);
        GlobalTagMapDto mapDto = new GlobalTagMapDto();
        mapDto.tagName(tagname)
                .globalTagName("A-TEST-GT-40").record("some-rec").label("TEST-4");
        log.info("Store global tag to tag mapping : {} ", mapDto);
        final ResponseEntity<GlobalTagMapDto> response = testRestTemplate
                .postForEntity("/crestapi/globaltagmaps", mapDto, GlobalTagMapDto.class);
        {
            log.info("Created global tag to tag mapping {} ", response.getBody());
            GlobalTagMapDto respb = response.getBody();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            log.info("Response from server is: " + respb);
            assertThat(respb).isNotNull();
            assertThat(respb.getTagName()).isEqualTo(tagname);
        }
        GlobalTagMapDto mapNfDto = new GlobalTagMapDto();
        mapNfDto.tagName("A-CRESTMAPPINGS-51").globalTagName("A-TEST-GT-40").record("some-rec-2").label("TEST-NF-4");
        final ResponseEntity<String> responsenotfound = testRestTemplate
                .postForEntity("/crestapi/globaltagmaps", mapNfDto, String.class);
        {
            log.info("Created global tag to not existing tag mapping {} ", responsenotfound.getBody());
            assertThat(responsenotfound.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
        mapNfDto.tagName(tagname).globalTagName("A-TEST-GT-50").record("some-rec-2").label("TEST" +
                "-NF-4");
        final ResponseEntity<String> responsenotfound2 = testRestTemplate
                .postForEntity("/crestapi/globaltagmaps", mapNfDto, String.class);
        {
            log.info("Created not existing global tag to tag mapping {} ", responsenotfound2.getBody());
            assertThat(responsenotfound2.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
        // Find mappings via gt tag  name
        final ResponseEntity<String> resptrace = testRestTemplate
                .exchange("/crestapi/globaltagmaps/A-TEST-GT-40" , HttpMethod.GET,
                        null, String.class);
        {
            log.info("Find global tag -> tag mappings {} ", resptrace.getBody());
            assertThat(resptrace.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
        // Find mappings via tag  name
        final HttpHeaders headers = new HttpHeaders();
        headers.add("X-Crest-MapMode", "BackTrace");
        final HttpEntity<?> entity = new HttpEntity<>(headers);

        final ResponseEntity<String> respbktrace = testRestTemplate
                .exchange("/crestapi/globaltagmaps/" + tagname , HttpMethod.GET,
                        entity, String.class);
        {
            log.info("Find tag -> global tag mappings {} ", respbktrace.getBody());
            assertThat(respbktrace.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        log.info("Delete mapping...{}", mapDto);
        final String url = "/crestapi/globaltagmaps/" + mapDto.getGlobalTagName()+"?record=some-rec";
        log.info("Removing mapping {}", url);
        this.testRestTemplate.delete(url);
    }
}
