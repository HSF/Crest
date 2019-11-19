package hep.crest.server.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

import hep.crest.swagger.model.RunLumiInfoDto;
import hep.crest.swagger.model.RunLumiSetDto;
import hep.crest.testutils.DataGenerator;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ActiveProfiles("test")
public class TestMetadataApi {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    @Qualifier("jacksonMapper")
    private ObjectMapper mapper;

    @Test
    public void testA_runinfoApi() throws Exception {
        final RunLumiInfoDto dto = DataGenerator.generateRunLumiInfoDto(new BigDecimal(210000L),
                new BigDecimal(222222L), new BigDecimal(100));
        log.info("Store runlumi info : {} ", dto);
        final ResponseEntity<RunLumiInfoDto> response = this.testRestTemplate
                .postForEntity("/crestapi/runinfo", dto, RunLumiInfoDto.class);
        log.info("Received response: {}", response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        final ResponseEntity<String> resp = this.testRestTemplate.exchange(
                "/crestapi/runinfo?by=run>100,insertiontime>0,lb>40", HttpMethod.GET, null,
                String.class);

        {
            log.info("Retrieved run info list " + resp.getBody());
            final String responseBody = resp.getBody();
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            RunLumiSetDto ok;
            log.info("Response from server is: " + responseBody);
            ok = mapper.readValue(responseBody, RunLumiSetDto.class);
            assertThat(ok.getSize()).isGreaterThan(0);
        }

        final ResponseEntity<String> resp1 = this.testRestTemplate.exchange(
                "/crestapi/runinfo/list?from=19700101010000&to=20210130150000&format=time", HttpMethod.GET, null,
                String.class);

        {
            log.info("Retrieved run info list " + resp1.getBody());
            final String responseBody = resp1.getBody();
            assertThat(resp1.getStatusCode()).isEqualTo(HttpStatus.OK);
            RunLumiSetDto ok;
            log.info("Response from server is: " + responseBody);
            ok = mapper.readValue(responseBody, RunLumiSetDto.class);
            assertThat(ok.getSize()).isGreaterThanOrEqualTo(0);
        }
        final ResponseEntity<String> resp2 = this.testRestTemplate.exchange(
                "/crestapi/runinfo/list?from=200-0&to=2000000-100&format=run-lumi", HttpMethod.GET, null,
                String.class);

        {
            log.info("Retrieved run info list " + resp2.getBody());
            final String responseBody = resp2.getBody();
            assertThat(resp2.getStatusCode()).isEqualTo(HttpStatus.OK);
            RunLumiSetDto ok;
            log.info("Response from server is: " + responseBody);
            ok = mapper.readValue(responseBody, RunLumiSetDto.class);
            assertThat(ok.getSize()).isGreaterThanOrEqualTo(0);
        }

    }
    
    @Test
    public void testA_runinfofail() {
        final RunLumiInfoDto dto = DataGenerator.generateRunLumiInfoDto(new BigDecimal(210000L),
                new BigDecimal(222222L), new BigDecimal(100));
        dto.endtime(null).run(null);
        log.info("Store runlumi info : {} ", dto);
        final ResponseEntity<String> response = this.testRestTemplate
                .postForEntity("/crestapi/runinfo", dto, String.class);
        log.info("Received response: {}", response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        
        log.info("Find runlumi info without by : {} ");
        final ResponseEntity<String> resp = this.testRestTemplate.exchange(
                "/crestapi/runinfo?by=none", HttpMethod.GET, null,
                String.class);
        log.info("Received response: {}", resp);
        assertThat(resp.getStatusCode()).isGreaterThanOrEqualTo(HttpStatus.OK);
        log.info("Find runlumi info without by : {} ");
        
        final ResponseEntity<String> resp2 = this.testRestTemplate.exchange(
                "/crestapi/runinfo?by=some>0&sort=some:DESC", HttpMethod.GET, null,
                String.class);
        log.info("Received response: {}", resp2);
        assertThat(resp2.getStatusCode()).isGreaterThanOrEqualTo(HttpStatus.OK);

        final ResponseEntity<String> resp3 = this.testRestTemplate.exchange(
                "/crestapi/runinfo?by=run<1&sort=run:DESC", HttpMethod.GET, null,
                String.class);
        log.info("Received response: {}", resp3);
        assertThat(resp3.getStatusCode()).isGreaterThanOrEqualTo(HttpStatus.OK);
    }

    @Test
    public void testB_fsApi() throws Exception {

        final ResponseEntity<String> response = this.testRestTemplate.exchange(
                "/crestapi/fs/tar?tagname=SB-TAG-IOV-01", HttpMethod.POST, null, String.class);
        log.info("Received response: {}", response);
        assertThat(response.getStatusCode()).isGreaterThan(HttpStatus.OK);

    }

    
}
