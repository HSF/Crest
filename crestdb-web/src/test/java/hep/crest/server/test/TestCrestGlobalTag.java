package hep.crest.server.test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hep.crest.server.swagger.api.ApiResponseMessage;
import hep.crest.swagger.model.GlobalTagDto;
import hep.crest.swagger.model.GlobalTagMapDto;
import hep.crest.swagger.model.GlobalTagMapSetDto;
import hep.crest.swagger.model.GlobalTagSetDto;
import hep.crest.swagger.model.TagDto;
import hep.crest.swagger.model.TagSetDto;
import hep.crest.testutils.DataGenerator;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ActiveProfiles("test")
public class TestCrestGlobalTag {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    @Qualifier("jacksonMapper")
    private ObjectMapper mapper;

    @Test
    public void testA_getAndRemoveGlobaltags() {

        final ResponseEntity<String> response = this.testRestTemplate
                .exchange("/crestapi/globaltags", HttpMethod.GET, null, String.class);
        // ResponseEntity<String> response =
        // this.testRestTemplate.getForEntity("/crestapi/globaltags", null);
        if (response.getStatusCode() == HttpStatus.OK) {
            GlobalTagSetDto ok;
            try {
                if (mapper == null) {
                    log.error("Cannot use mapper because it is null");
                }
                log.info("testA_getAndRemoveGlobaltags => Received response {}",
                        response.getBody());
                ok = mapper.readValue(response.getBody(), GlobalTagSetDto.class);
                final List<GlobalTagDto> gtaglist = ok.getResources();
                for (final GlobalTagDto globalTagDto : gtaglist) {
                    final String url = "/crestapi/admin/globaltags/" + globalTagDto.getName();
                    log.info("Removing global tag {}", url);
                    this.testRestTemplate.delete(url);
                }
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(ok.getSize()).isGreaterThanOrEqualTo(0);
            }
            catch (final JsonParseException e) {
                e.printStackTrace();
            }
            catch (final IOException e) {
                e.printStackTrace();
            }
        }
        if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
            try {
                final ApiResponseMessage err = mapper.readValue(response.getBody(),
                        ApiResponseMessage.class);
                log.info("Response error in global tag {}", err.getMessage());
            }
            catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testB_storeGlobaltags() throws Exception {
        final GlobalTagDto dto = DataGenerator.generateGlobalTagDto("A-GT-01");
        log.info("Store global tag : {} ", dto);
        final ResponseEntity<GlobalTagDto> response = this.testRestTemplate
                .postForEntity("/crestapi/globaltags", dto, GlobalTagDto.class);
        log.info("Received response: {}", response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        log.info("Try to store global tag again : {} ", dto);
        final ResponseEntity<String> response1 = this.testRestTemplate
                .postForEntity("/crestapi/globaltags", dto, String.class);
        log.info("Received response: {}", response1);
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.SEE_OTHER);

        log.info("Try to update global tag : {} ", dto);
        final GlobalTagDto body = dto;
        body.setDescription("Description has changed");
        body.validity(new BigDecimal(1990L));
        body.scenario("update");
        body.type("new");
        final HttpEntity<GlobalTagDto> updrequest = new HttpEntity<GlobalTagDto>(body);

        final ResponseEntity<String> respupd = this.testRestTemplate
                .exchange("/crestapi/admin/globaltags/" + dto.getName(), HttpMethod.PUT, updrequest, String.class);
        {
            log.info("Update global tag {} ", body.getName());
            final String responseBody = respupd.getBody();
            assertThat(respupd.getStatusCode()).isEqualTo(HttpStatus.OK);
            GlobalTagDto ok;
            log.info("Response from server is: " + responseBody);
            ok = mapper.readValue(responseBody, GlobalTagDto.class);
            assertThat(ok).isNotNull();
            assertThat(ok.getScenario()).isEqualTo("update");
        }
        final ResponseEntity<String> respupdfail = this.testRestTemplate
                .exchange("/crestapi/admin/globaltags/NOT-THERE", HttpMethod.PUT, updrequest, String.class);
        {
            log.info("Update global tag NOT-THERE ");
            assertThat(respupdfail.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        final ResponseEntity<String> resprmnotthere = this.testRestTemplate
                .exchange("/crestapi/admin/globaltags/NOT-THERE", HttpMethod.DELETE, null, String.class);
        {
            log.info("Remove global tag NOT-THERE ");
            assertThat(resprmnotthere.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        dto.name(null);
        log.info("Try to use null name in global tag again : {} ", dto);
        final ResponseEntity<String> response2 = this.testRestTemplate
                .postForEntity("/crestapi/globaltags", dto, String.class);
        log.info("Received response: {}", response2);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        final ResponseEntity<String> responsedelete = this.testRestTemplate.exchange(
                "/crestapi/admin/globaltags/A-GT-01", HttpMethod.DELETE, null, String.class);
        log.info("Received response on delete: {}", responsedelete);
        assertThat(responsedelete.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void testC_findGlobaltags() throws Exception {
        final GlobalTagDto dto = DataGenerator.generateGlobalTagDto("A-GT-02");
        log.info("Store global tag : {} ", dto);
        final ResponseEntity<GlobalTagDto> response = this.testRestTemplate
                .postForEntity("/crestapi/globaltags", dto, GlobalTagDto.class);
        log.info("Received response: {}", response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        final GlobalTagDto dto1 = DataGenerator.generateGlobalTagDto("B-GT-02");
        log.info("Store global tag : {} ", dto1);
        final ResponseEntity<GlobalTagDto> response1 = this.testRestTemplate
                .postForEntity("/crestapi/globaltags", dto1, GlobalTagDto.class);
        log.info("Received response: {}", response1);
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        final ResponseEntity<String> resp = this.testRestTemplate.exchange("/crestapi/globaltags",
                HttpMethod.GET, null, String.class);

        {
            log.info("Retrieved all global tags {} ", resp.getBody());
            final String responseBody = resp.getBody();
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            GlobalTagSetDto ok;
            log.info("Response from server is: " + responseBody);
            ok = mapper.readValue(responseBody, GlobalTagSetDto.class);
            assertThat(ok.getSize()).isGreaterThan(0);
        }

        final ResponseEntity<String> resp1 = this.testRestTemplate.exchange(
                "/crestapi/globaltags/" + dto1.getName(), HttpMethod.GET, null, String.class);
        {
            log.info("Retrieved global tag {} ", dto1.getName());
            final String responseBody = resp1.getBody();
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            GlobalTagSetDto ok;
            log.info("Response from server is: " + responseBody);
            ok = mapper.readValue(responseBody, GlobalTagSetDto.class);
            assertThat(ok.getSize()).isEqualTo(1);
        }
        // This should not find the GT
        final ResponseEntity<String> resp1null = this.testRestTemplate
                .exchange("/crestapi/globaltags/SOME-GT", HttpMethod.GET, null, String.class);
        {
            log.info("Retrieved global tag SOME-GT should return null");
            assertThat(resp1null.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        final ResponseEntity<String> resp2 = this.testRestTemplate
                .exchange("/crestapi/globaltags?by=name:GT,release:rel%,workflow:none", HttpMethod.GET, null, String.class);

        {
            log.info("Retrieved global tag list " + resp2.getBody());
            final String responseBody = resp2.getBody();
            assertThat(resp2.getStatusCode()).isEqualTo(HttpStatus.OK);
            GlobalTagSetDto ok;
            log.info("Response from server is: " + responseBody);
            ok = mapper.readValue(responseBody, GlobalTagSetDto.class);
            assertThat(ok.getSize()).isGreaterThan(0);
        }
        
        final ResponseEntity<String> resp2a = this.testRestTemplate
                .exchange("/crestapi/globaltags?by=scenario:test,validity>0,validity:0,validity<0", HttpMethod.GET, null, String.class);

        {
            log.info("Retrieved global tag list " + resp2a.getBody());
            final String responseBody = resp2a.getBody();
            assertThat(resp2a.getStatusCode()).isEqualTo(HttpStatus.OK);
            GlobalTagSetDto ok;
            log.info("Response from server is: " + responseBody);
            ok = mapper.readValue(responseBody, GlobalTagSetDto.class);
            assertThat(ok.getSize()).isEqualTo(0);
        }

        final ResponseEntity<String> resp2b = this.testRestTemplate
                .exchange("/crestapi/globaltags?by=scenario:test,insertionTime>0,insertionTime:0,insertionTime<0", HttpMethod.GET, null, String.class);

        {
            log.info("Retrieved global tag list " + resp2b.getBody());
            final String responseBody = resp2b.getBody();
            assertThat(resp2b.getStatusCode()).isEqualTo(HttpStatus.OK);
            GlobalTagSetDto ok;
            log.info("Response from server is: " + responseBody);
            ok = mapper.readValue(responseBody, GlobalTagSetDto.class);
            assertThat(ok.getSize()).isGreaterThanOrEqualTo(0);
        }


        // Create a tag
        final TagDto tagdto = DataGenerator.generateTagDto("B-TAGGT-02", "test");
        log.info("Store tag : {} ", tagdto);
        final ResponseEntity<TagDto> resptag = this.testRestTemplate.postForEntity("/crestapi/tags",
                tagdto, TagDto.class);
        log.info("Received response: {}", resptag);
        assertThat(resptag.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Associate the tag B-TAGGT-02 to the global tag B-GT-02
        final GlobalTagMapDto maptagdto = DataGenerator.generateMappingDto(tagdto.getName(),
                dto1.getName(), "B-TAGGT", "test");
        log.info("Store global tag map : {} ", maptagdto);
        final ResponseEntity<GlobalTagMapDto> respmaptag = this.testRestTemplate
                .postForEntity("/crestapi/globaltagmaps", maptagdto, GlobalTagMapDto.class);
        log.info("Received response: {}", respmaptag);
        assertThat(respmaptag.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        final ResponseEntity<String> resptags = this.testRestTemplate.exchange(
                "/crestapi/globaltags/" + dto1.getName() + "/tags?record=B-TAGGT&label=none",
                HttpMethod.GET, null, String.class);
        {
            log.info("Retrieved associated tags for global tag {}", dto1.getName());
            final String responseBody = resptags.getBody();
            assertThat(resptags.getStatusCode()).isEqualTo(HttpStatus.OK);
            TagSetDto ok;
            log.info("Response from server is: " + responseBody);
            ok = mapper.readValue(responseBody, TagSetDto.class);
            assertThat(ok.getSize()).isGreaterThan(0);
        }

        final ResponseEntity<String> resptags2 = this.testRestTemplate.exchange(
                "/crestapi/globaltags/" + dto1.getName() + "/tags?record=none&label=none",
                HttpMethod.GET, null, String.class);
        {
            log.info("Retrieved associated tags for global tag {}", dto1.getName());
            final String responseBody = resptags2.getBody();
            assertThat(resptags2.getStatusCode()).isEqualTo(HttpStatus.OK);
            TagSetDto ok;
            log.info("Response from server is: " + responseBody);
            ok = mapper.readValue(responseBody, TagSetDto.class);
            assertThat(ok.getSize()).isGreaterThan(0);
        }

    }

    @Test
    public void testD_globaltagsfail() {
        final ResponseEntity<String> resp = this.testRestTemplate
                .exchange("/crestapi/globaltags?by=name:fff", HttpMethod.GET, null, String.class);

        assertThat(resp.getStatusCode()).isGreaterThanOrEqualTo(HttpStatus.OK);

        final ResponseEntity<String> resp1 = this.testRestTemplate
                .exchange("/crestapi/globaltags?by=none", HttpMethod.GET, null, String.class);
        assertThat(resp1.getStatusCode()).isGreaterThanOrEqualTo(HttpStatus.OK);

        final ResponseEntity<String> resp2 = this.testRestTemplate.exchange(
                "/crestapi/globaltags?by=none&sort=fff:DESC", HttpMethod.GET, null, String.class);
        assertThat(resp2.getStatusCode()).isGreaterThanOrEqualTo(HttpStatus.OK);
    }

    @Test
    public void testC_findGlobaltagMaps() throws Exception {

        final GlobalTagDto dto = DataGenerator.generateGlobalTagDto("B-GTMAP-03");
        log.info("Store global tag : {} ", dto);
        final ResponseEntity<GlobalTagDto> response = this.testRestTemplate
                .postForEntity("/crestapi/globaltags", dto, GlobalTagDto.class);
        log.info("Received response: {}", response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Create a tag
        final TagDto tagdto = DataGenerator.generateTagDto("B-TAGGT-03", "test");
        log.info("Store tag : {} ", tagdto);
        final ResponseEntity<TagDto> resptag = this.testRestTemplate.postForEntity("/crestapi/tags",
                tagdto, TagDto.class);
        log.info("Received response: {}", resptag);
        assertThat(resptag.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Associate the tag B-TAGGT-02 to the global tag B-GT-02
        final GlobalTagMapDto maptagdto = DataGenerator.generateMappingDto(tagdto.getName(),
                dto.getName(), "B-TAGGT", "test");
        log.info("Store global tag map : {} ", maptagdto);
        final ResponseEntity<GlobalTagMapDto> respmaptag = this.testRestTemplate
                .postForEntity("/crestapi/globaltagmaps", maptagdto, GlobalTagMapDto.class);
        log.info("Received response: {}", respmaptag);
        assertThat(respmaptag.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Associate the tag B-TAGGT-02 to the global tag B-GT-02 again; it should fail
        final ResponseEntity<String> respmaptagalreadythere = this.testRestTemplate
                .postForEntity("/crestapi/globaltagmaps", maptagdto, String.class);
        log.info("Received response: {}", respmaptagalreadythere);
        assertThat(respmaptagalreadythere.getStatusCode()).isEqualTo(HttpStatus.SEE_OTHER);

        // Associate but use a null name for global tag: it should fail
        maptagdto.setGlobalTagName(null);
        final ResponseEntity<String> respmaptagfail = this.testRestTemplate
                .postForEntity("/crestapi/globaltagmaps", maptagdto, String.class);
        log.info("Received response: {}", respmaptagfail);
        assertThat(respmaptagfail.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        // Search for mappings
        final ResponseEntity<String> resptags = this.testRestTemplate.exchange(
                "/crestapi/globaltags/" + dto.getName() + "/tags?record=B-TAGGT&label=none",
                HttpMethod.GET, null, String.class);
        {
            log.info("Retrieved associated tags for global tag {}", dto.getName());
            final String responseBody = resptags.getBody();
            assertThat(resptags.getStatusCode()).isEqualTo(HttpStatus.OK);
            TagSetDto ok;
            log.info("Response from server is: " + responseBody);
            ok = mapper.readValue(responseBody, TagSetDto.class);
            assertThat(ok.getSize()).isGreaterThan(0);
        }
        final ResponseEntity<String> resptagsall = this.testRestTemplate.exchange(
                "/crestapi/globaltags/" + dto.getName() + "/tags?record=B-TAGGT&label=pippo",
                HttpMethod.GET, null, String.class);
        {
            log.info("Retrieved associated tags for global tag {} using bot record and label");
            assertThat(resptagsall.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        // Search for mapping on not existing global tag
        final ResponseEntity<String> resptags2 = this.testRestTemplate.exchange(
                "/crestapi/globaltags/NOT-THERE/tags",
                HttpMethod.GET, null, String.class);
        {
            log.info("Retrieved associated tags for global tag NOT-THERE...should fail");
            assertThat(resptags2.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        // Associate the tag B-TAGGT-02 to an not existing global tag
        final GlobalTagMapDto maptagdto1 = DataGenerator.generateMappingDto(tagdto.getName(),
                "NOT-THERE", "B-TAGGT", "test");
        log.info("Store global tag map : {} ", maptagdto1);
        final ResponseEntity<String> respmaptag1 = this.testRestTemplate
                .postForEntity("/crestapi/globaltagmaps", maptagdto1, String.class);
        log.info("Received response: {}", respmaptag1);
        assertThat(respmaptag1.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // Retrieve global tag maps
        final ResponseEntity<String> respmaptags = this.testRestTemplate.exchange(
                "/crestapi/globaltagmaps/" + dto.getName(), HttpMethod.GET, null, String.class);
        {
            log.info("Retrieved associated tags for global tag {}", dto.getName());
            final String responseBody = respmaptags.getBody();
            assertThat(respmaptags.getStatusCode()).isEqualTo(HttpStatus.OK);
            GlobalTagMapSetDto ok;
            log.info("Response from server is: " + responseBody);
            ok = mapper.readValue(responseBody, GlobalTagMapSetDto.class);
            assertThat(ok.getSize()).isGreaterThan(0);
        }

        // Retrieve deleted global tag maps
        final ResponseEntity<String> respdeletemaptags = this.testRestTemplate.exchange(
                "/crestapi/globaltagmaps/" + dto.getName()+"?label=test&tagname="+tagdto.getName(), 
                HttpMethod.DELETE, null, String.class);
        {
            log.info("Retrieved the deleted mapping tags for global tag {} and label {}", maptagdto.getGlobalTagName(), maptagdto.getLabel());
            final String responseBody = respdeletemaptags.getBody();
            assertThat(respdeletemaptags.getStatusCode()).isEqualTo(HttpStatus.OK);
            GlobalTagMapSetDto ok;
            log.info("Response from server is: " + responseBody);
            ok = mapper.readValue(responseBody, GlobalTagMapSetDto.class);
            assertThat(ok.getSize()).isGreaterThan(0);
        }

        // Retrieve global tag maps for not existing gtag
        final ResponseEntity<String> respmaptagsnull = this.testRestTemplate
                .exchange("/crestapi/globaltagmaps/NOT-THERE", HttpMethod.GET, null, String.class);
        assertThat(respmaptagsnull.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Retrieve global tag maps from tagname using backtrace
        final HttpHeaders headers = new HttpHeaders();
        headers.add("X-Crest-MapMode", "BackTrace");
        final HttpEntity<?> entity = new HttpEntity<>(headers);
        final ResponseEntity<String> respmapbktags = this.testRestTemplate.exchange(
                "/crestapi/globaltagmaps/" + tagdto.getName(), HttpMethod.GET, entity,
                String.class);
        {
            log.info("Retrieved associated tags for global tag {}", tagdto.getName());
            final String responseBody = respmapbktags.getBody();
            assertThat(respmapbktags.getStatusCode()).isEqualTo(HttpStatus.OK);
            GlobalTagMapSetDto ok;
            log.info("Response from server is: " + responseBody);
            ok = mapper.readValue(responseBody, GlobalTagMapSetDto.class);
            assertThat(ok.getSize()).isGreaterThan(0);
        }

    }

}
