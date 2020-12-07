package hep.crest.server.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import hep.crest.swagger.model.IovDto;
import hep.crest.swagger.model.IovSetDto;
import hep.crest.swagger.model.PayloadDto;
import hep.crest.swagger.model.PayloadSetDto;
import hep.crest.swagger.model.TagDto;
import hep.crest.testutils.DataGenerator;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ContextConfiguration
@ActiveProfiles("sqlite")
public class TestCrestPayloadSqlite {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
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
    public void testA_payloadApi() throws Exception {
        final PayloadDto dto = DataGenerator.generatePayloadDto("afakehashp01", "some data",
                "some info", "test");
        log.info("Store payload : {}", dto);
        final ResponseEntity<PayloadDto> response = this.testRestTemplate
                .postForEntity("/crestapi/payloads", dto, PayloadDto.class);
        log.info("Received response: " + response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        final ResponseEntity<String> responsedup = this.testRestTemplate
                .postForEntity("/crestapi/payloads", dto, String.class);
        log.info("Received response on dup: " + responsedup);
        assertThat(responsedup.getStatusCode()).isEqualTo(HttpStatus.SEE_OTHER);

        final HttpHeaders headers = new HttpHeaders();
        headers.add("X-Crest-PayloadFormat", "JSON");
        final HttpEntity<?> entity = new HttpEntity<>(headers);

        final ResponseEntity<String> resp1 = this.testRestTemplate.exchange(
                "/crestapi/payloads/" + dto.getHash(), HttpMethod.GET, entity, String.class);
        {
            log.info("Retrieved payload {} ", dto.getHash());
            final String responseBody = resp1.getBody();
            assertThat(resp1.getStatusCode()).isEqualTo(HttpStatus.OK);
            PayloadSetDto ok;
            log.info("Response from server is: " + responseBody);
            ok = mapper.readValue(responseBody, PayloadSetDto.class);
            assertThat(ok.getSize()).isEqualTo(1L);
        }

        // Now do not set the header and retrieve the binary data only
        final ResponseEntity<String> resp2 = this.testRestTemplate.exchange(
                "/crestapi/payloads/" + dto.getHash(), HttpMethod.GET, null, String.class);
        {
            log.info("Retrieved binary payload {} ", dto.getHash());
            assertThat(resp2.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
        // Try another paylaod
        final PayloadDto dto10 = DataGenerator.generatePayloadDto("afakehashp10", "some other data",
                "some info", "test");
        log.info("Store payload : {}", dto10);
        final ResponseEntity<PayloadDto> response10 = this.testRestTemplate
                .postForEntity("/crestapi/payloads", dto10, PayloadDto.class);
        log.info("Received response: " + response10);
        assertThat(response10.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        
        // Upload payload using external file
        final HttpHeaders headers1 = new HttpHeaders();
        headers1.setContentType(MediaType.MULTIPART_FORM_DATA);
        final PayloadDto dto1 = DataGenerator.generatePayloadDto("afakehashp02", "",
                "some other info", "test");
        log.info("Store payload : {}", dto1);
        String jsondto = "";
        jsondto = mapper.writeValueAsString(dto1);
        log.info("Upload payload: use json body {}", jsondto);
        final MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
        map.add("file", new String("some other data".getBytes()));
        map.add("payload", jsondto);
        final HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(
                map, headers1);
        final ResponseEntity<String> resp3 = this.testRestTemplate
                .postForEntity("/crestapi/payloads/upload", request, String.class);

        log.info("Upload request gave response: {}", resp3);
        assertThat(resp3.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Now retrieve metadata only
        final ResponseEntity<String> resp4 = this.testRestTemplate.exchange(
                "/crestapi/payloads/" + dto1.getHash() + "/meta", HttpMethod.GET, null,
                String.class);
        {
            log.info("Retrieved meta payload {} ", dto1.getHash());
            assertThat(resp4.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

    }
    
    @Test
    public void testA_payloadfail() {
        final PayloadDto dto = DataGenerator.generatePayloadDto("afakehashp01", "some data",
                "some info", "test");
        dto.hash(null);
        log.info("Store bad payload : {}", dto);
        final ResponseEntity<String> response = this.testRestTemplate
                .postForEntity("/crestapi/payloads", dto, String.class);
        log.info("Received response: " + response);
        assertThat(response.getStatusCode()).isGreaterThanOrEqualTo(HttpStatus.OK);
        
        // Upload payload using external file
        final HttpHeaders headers1 = new HttpHeaders();
        headers1.setContentType(MediaType.MULTIPART_FORM_DATA);
        final MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
        map.add("file", new String("some other data".getBytes()));
        map.add("payload", null);
        final HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(
                map, headers1);
        final ResponseEntity<String> resp2 = this.testRestTemplate
                .postForEntity("/crestapi/payloads/upload", request, String.class);
        assertThat(resp2.getStatusCode()).isGreaterThanOrEqualTo(HttpStatus.OK);

        final String hash = "somenotexistinghash";
        log.info("Get payload with not existing hash: {}", hash);
        final HttpHeaders headers = new HttpHeaders();
        headers.add("X-Crest-PayloadFormat", "JSON");
        final HttpEntity<?> entity = new HttpEntity<>(headers);
        final ResponseEntity<String> resp3 = this.testRestTemplate.exchange(
                "/crestapi/payloads/" + hash, HttpMethod.GET, entity, String.class);
        log.info("Received response: " + resp3);
        assertThat(resp3.getStatusCode()).isGreaterThanOrEqualTo(HttpStatus.OK);

        log.info("Get payload meta with not existing hash: {}", hash);
        
        final ResponseEntity<String> resp4 = this.testRestTemplate.exchange(
                "/crestapi/payloads/" + hash+"/meta", HttpMethod.GET, entity, String.class);
        log.info("Received response: " + resp4);
        assertThat(resp4.getStatusCode()).isGreaterThanOrEqualTo(HttpStatus.OK);

    }
    

    @Test
    public void testA_payloadIovApi() throws Exception {
        final TagDto dto = DataGenerator.generateTagDto("SB-TAG-PYLD-01", "run");
        log.info("Store tag for payload request: {}", dto);
        final ResponseEntity<String> response = this.testRestTemplate
                .postForEntity("/crestapi/tags", dto, String.class);
        log.info("Received response: " + response);
        assertThat(response.getStatusCode()).isLessThanOrEqualTo(HttpStatus.SEE_OTHER);

        final byte[] bindata = new String("This is yet another fake payload").getBytes();

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        final String tagname = "SB-TAG-PYLD-01";
        final MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
        map.add("file", new String(bindata));
        map.add("since", "1000000");
        map.add("endtime", "0");
        map.add("tag", tagname);
        final HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(
                map, headers);
        final ResponseEntity<String> resp1 = this.testRestTemplate
                .postForEntity("/crestapi/payloads/store", request, String.class);

        log.info("Upload request gave response: {}", resp1);
        assertThat(resp1.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Upload batch using external files
        final IovDto iovdto1 = DataGenerator.generateIovDto("file:///tmp/pyld1.json", tagname, new BigDecimal(2000000L));
        final IovDto iovdto2 = DataGenerator.generateIovDto("file:///tmp/pyld2.json", tagname, new BigDecimal(2100000L));
        final IovSetDto setdto = new IovSetDto();
        setdto.format("json").size(2L);
        setdto.addResourcesItem(iovdto1).addResourcesItem(iovdto2);
        
        DataGenerator.generatePayloadData("/tmp/pyld1.json", "some content for file1");
        DataGenerator.generatePayloadData("/tmp/pyld2.json", "some content for file2 which will be different");
        final MultiValueMap<String, Object> map1 = new LinkedMultiValueMap<String, Object>();
        final FileSystemResource f1 = new FileSystemResource("/tmp/pyld1.json");
        final FileSystemResource f2 = new FileSystemResource("/tmp/pyld2.json");
        map1.add("files", f1);
        map1.add("files", f2);
        map1.add("endtime", "0");
        map1.add("tag", tagname);
        final String jsonset = mapper.writeValueAsString(setdto);
        map1.add("iovsetupload", jsonset);
        final HttpEntity<MultiValueMap<String, Object>> request1 = new HttpEntity<MultiValueMap<String, Object>>(
                map1, headers);
        final ResponseEntity<String> resp2 = this.testRestTemplate
                .postForEntity("/crestapi/payloads/uploadbatch", request1, String.class);
        assertThat(resp2.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Upload batch using payload inserted in iovdto instead of payload hash.
        final IovDto iovdto3 = DataGenerator.generateIovDto("This will become a payload", tagname, new BigDecimal(3000000L));
        final IovDto iovdto4 = DataGenerator.generateIovDto("This will become another payload", tagname, new BigDecimal(3100000L));
        final IovSetDto setdto2 = new IovSetDto();
        setdto2.format("txt").size(2L);
        setdto2.addResourcesItem(iovdto3).addResourcesItem(iovdto4);
        
        final MultiValueMap<String, Object> map2 = new LinkedMultiValueMap<String, Object>();
        map2.add("endtime", "0");
        map2.add("tag", tagname);
        final String jsonset2 = mapper.writeValueAsString(setdto2);
        map2.add("iovsetupload", jsonset2);
        final HttpEntity<MultiValueMap<String, Object>> request2 = new HttpEntity<MultiValueMap<String, Object>>(
                map2, headers);
        final ResponseEntity<String> resp3 = this.testRestTemplate
                .postForEntity("/crestapi/payloads/storebatch", request2, String.class);
        assertThat(resp3.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        
    }

}
