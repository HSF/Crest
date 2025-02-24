package hep.crest.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hep.crest.server.converters.RunLumiMapper;
import hep.crest.server.converters.TagMapper;
import hep.crest.server.data.pojo.TagSynchroEnum;
import hep.crest.server.data.runinfo.pojo.RunLumiId;
import hep.crest.server.data.runinfo.pojo.RunLumiInfo;
import hep.crest.server.swagger.model.GenericMap;
import hep.crest.server.swagger.model.GlobalTagDto;
import hep.crest.server.swagger.model.GlobalTagMapDto;
import hep.crest.server.swagger.model.IovSetDto;
import hep.crest.server.swagger.model.RespPage;
import hep.crest.server.swagger.model.RunLumiInfoDto;
import hep.crest.server.swagger.model.RunLumiSetDto;
import hep.crest.server.swagger.model.StoreDto;
import hep.crest.server.swagger.model.StoreSetDto;
import hep.crest.server.swagger.model.TagDto;
import hep.crest.server.swagger.model.TagSetDto;
import hep.crest.server.utils.RandomGenerator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration
@Slf4j
public class TestCrestRunLumi {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    @Qualifier("jacksonMapper")
    private ObjectMapper mapper;
    /**
     * Mapper.
     */
    @Autowired
    private RunLumiMapper runLumiMapper;

    private static final RandomGenerator rnd = new RandomGenerator();

    public void initializeRunLumi() {
        RunLumiInfo runLumiInfo = (RunLumiInfo) rnd.generate(RunLumiInfo.class);
        RunLumiId id = new RunLumiId();
        runLumiInfo.setId(id);
        runLumiInfo.getId().setRunNumber(BigInteger.valueOf(9));
        runLumiInfo.getId().setLb(BigInteger.valueOf(1));
        RunLumiInfoDto dto = runLumiMapper.toDto(runLumiInfo);
        log.info("Creating run lumi info dto: " + dto);
        RunLumiSetDto setDto = new RunLumiSetDto();
        setDto.addresourcesItem(dto);
        setDto.size(1L);
        setDto.setFormat("RunLumiSetDto");
        log.info("Creating run lumi info set dto: " + setDto);
        final ResponseEntity<RunLumiSetDto> response = testRestTemplate
                .postForEntity("/crestapi/runinfo", setDto, RunLumiSetDto.class);
        log.info("Received response: {}", response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }


    @Test
    public void testRunLumiRest() {
        log.info("=======> testRunLumiRest ");
        initializeRunLumi();
        final ResponseEntity<RunLumiSetDto> response = testRestTemplate
                .getForEntity("/crestapi/runinfo?since=0&until=1000", RunLumiSetDto.class);
        {
            log.info("Retrieved run lumi " + response.getBody());
            final RunLumiSetDto responseBody = response.getBody();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(responseBody).isNotNull();
            assertThat(responseBody.getResources()).isNotNull();
            assertThat(responseBody.getResources().size()).isGreaterThan(0);
        }
    }
}
