package hep.crest.server.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import hep.crest.swagger.model.CrestBaseResponse;
import hep.crest.swagger.model.RunLumiInfoDto;
import hep.crest.swagger.model.RunLumiSetDto;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

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
        final Date start = new Date();
        final Date end = new Date(start.getTime()+3600000);
        final RunLumiInfoDto dto = DataGenerator.generateRunLumiInfoDto(new BigDecimal(start.getTime()), new BigDecimal(end.getTime()), new BigDecimal(100L));
        final CrestBaseResponse setdto = new RunLumiSetDto().addResourcesItem(dto).size(1L);
        log.info("Store run info set : {} ", setdto);
        final ResponseEntity<RunLumiSetDto> response = this.testRestTemplate
                .postForEntity("/crestapi/runinfo", setdto, RunLumiSetDto.class);
        log.info("Received response: {}", response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        log.info("Retrieve runs");
        final ResponseEntity<String> resp = this.testRestTemplate.exchange(
                "/crestapi/runinfo?by=runnumber>99,starttime>0", HttpMethod.GET, null,
                String.class);

        {
            log.info("Retrieved run info list " + resp.getBody());
            final String responseBody = resp.getBody();
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            RunLumiSetDto ok;
            log.info("Response from server is: " + responseBody);
            ok = mapper.readValue(responseBody, RunLumiSetDto.class);
            assertThat(ok.getSize()).isPositive();
        }

        log.info("Retrieve runs in a range in time");
        final ResponseEntity<String> resp1 = this.testRestTemplate.exchange(
                "/crestapi/runinfo/select?from=19700101010000&to=20210130150000&format=iso&mode=daterange", HttpMethod.GET, null,
                String.class);

        {
            log.info("Retrieved run info list " + resp1.getBody());
            final String responseBody = resp1.getBody();
            assertThat(resp1.getStatusCode()).isEqualTo(HttpStatus.OK);
            RunLumiSetDto ok;
            log.info("Response from server is: " + responseBody);
            ok = mapper.readValue(responseBody, RunLumiSetDto.class);
            assertThat(ok.getSize()).isNotNegative();
        }

        log.info("Retrieve runs as run-lumi range");
        final ResponseEntity<String> resp2 = this.testRestTemplate.exchange(
                "/crestapi/runinfo/select?from=200&to=2000000&format=number&mode=runrange", HttpMethod.GET, null,
                String.class);

        {
            log.info("Retrieved run info list " + resp2.getBody());
            final String responseBody = resp2.getBody();
            assertThat(resp2.getStatusCode()).isEqualTo(HttpStatus.OK);
            RunLumiSetDto ok;
            log.info("Response from server is: " + responseBody);
            ok = mapper.readValue(responseBody, RunLumiSetDto.class);
            assertThat(ok.getSize()).isNotNegative();
        }

    }
    
    @Test
    public void testA_runinfofail() {
        final Date start = new Date();
        final Date end = new Date(start.getTime()+3600000);
        final RunLumiInfoDto dto = DataGenerator.generateRunLumiInfoDto(new BigDecimal(start.getTime()), new BigDecimal(end.getTime()), new BigDecimal(100L));

        dto.runNumber(null);
        log.info("Store run info : {} ", dto);
        final CrestBaseResponse setdto = new RunLumiSetDto().addResourcesItem(dto).size(1L);
        log.info("Store run info set : {} ", setdto);
 
        final ResponseEntity<String> response = this.testRestTemplate
                .postForEntity("/crestapi/runinfo", setdto, String.class);
        log.info("Received response: {}", response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        
        log.info("Find run info with by=none ");
        final ResponseEntity<String> resp = this.testRestTemplate.exchange(
                "/crestapi/runinfo?by=none", HttpMethod.GET, null,
                String.class);
        log.info("Received response: {}", resp);
        assertThat(resp.getStatusCode()).isGreaterThanOrEqualTo(HttpStatus.OK);

        log.info("Find run info with not existing by and sort : by=some>0&sort=some:DESC");
        final ResponseEntity<String> resp2 = this.testRestTemplate.exchange(
                "/crestapi/runinfo?by=some>0&sort=some:DESC", HttpMethod.GET, null,
                String.class);
        log.info("Received response: {}", resp2);
        assertThat(resp2.getStatusCode()).isGreaterThanOrEqualTo(HttpStatus.OK);

        final ResponseEntity<String> resp3 = this.testRestTemplate.exchange(
                "/crestapi/runinfo?by=runnunmber<1&sort=runNumber:DESC", HttpMethod.GET, null,
                String.class);
        log.info("Received response: {}", resp3);
        assertThat(resp3.getStatusCode()).isGreaterThanOrEqualTo(HttpStatus.OK);
    }
}
