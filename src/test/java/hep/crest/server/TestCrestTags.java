package hep.crest.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hep.crest.server.data.pojo.TagSynchroEnum;
import hep.crest.server.swagger.model.GlobalTagDto;
import hep.crest.server.swagger.model.GlobalTagMapDto;
import hep.crest.server.swagger.model.IovSetDto;
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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration
@Slf4j
public class TestCrestTags {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    @Qualifier("jacksonMapper")
    private ObjectMapper mapper;

    private static final RandomGenerator rnd = new RandomGenerator();

    public void initializeTag(String gtname) {
        TagDto dto = (TagDto) rnd.generate(TagDto.class);
        dto.name(gtname);
        dto.synchronization(TagSynchroEnum.NONE.type());
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
    public void testTagRest() {
        log.info("=======> testTagRest ");
        String tagname = "A-CRESTTAG-50";
        initializeGtag("A-TEST-GT-50");
        initializeTag(tagname);
        GlobalTagMapDto mapDto = new GlobalTagMapDto();
        mapDto.tagName(tagname)
                .globalTagName("A-TEST-GT-50").record("some-rec").label("TEST-5");
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
        String urltag = "/crestapi/tags?name=" + tagname
                + "&objectType=0&timeType=time&description=1000000&size=1000&page=0";
        final ResponseEntity<TagSetDto> resptags = this.testRestTemplate
                .exchange(urltag, HttpMethod.GET, null,
                        TagSetDto.class);
        {
            log.info("Retrieved tags " + resptags.getBody());
            final TagSetDto responseBody = resptags.getBody();
            assertThat(resptags.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(responseBody).isNotNull();
            assertThat(responseBody.getResources()).isNotNull();
            assertThat(responseBody.getResources().size()).isEqualTo(0);
        }

        String url = "/crestapi/globaltags/A-TEST-GT-50/tags";
        final ResponseEntity<TagSetDto> respft = this.testRestTemplate
                .exchange(url, HttpMethod.GET, null,
                        TagSetDto.class);
        {
            log.info("Retrieved global tag associated tags " + respft.getBody());
            final TagSetDto responseBody = respft.getBody();
            assertThat(respft.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(responseBody).isNotNull();
            assertThat(responseBody.getResources()).isNotNull();
            assertThat(responseBody.getResources().size()).isEqualTo(1);
        }

        // Now fill the tag with IOVs
        StoreSetDto storeSetDto = new StoreSetDto();
        StoreDto sdto = new StoreDto();
        sdto.data("A_FAKE_PAYLOAD_TO_TEST");
        sdto.since(1000L);
        sdto.streamerInfo("A_FAKE_STREAMER_INFO");
        storeSetDto.addresourcesItem(sdto);
        StoreDto sdto1 = new StoreDto();
        sdto1.data("ANOTHER_FAKE_PAYLOAD_TO_TEST");
        sdto1.since(2000L);
        sdto1.streamerInfo("A_FAKE_STREAMER_INFO");
        storeSetDto.addresourcesItem(sdto1);
        // Store the payloads into the tag

        try {
            final ResponseEntity<String> response2 = uploadJson(tagname, storeSetDto, "ascii", "none",
                    "1.0", "0");
            {
                log.info("Created payloads for tag {} ", response2.getBody());
                assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                // Verify IOV loading
                final ResponseEntity<IovSetDto> response3 = testRestTemplate
                        .getForEntity("/crestapi/iovs?tagname=" + tagname, IovSetDto.class);
                assertThat(response3.getStatusCode()).isEqualTo(HttpStatus.OK);
                response3.getBody().getResources().forEach(iov -> {
                    log.info("Found iov: {}", iov);
                });
                // Override IOV
                // Now fill the tag with IOVs
                StoreSetDto storeSetDto2 = new StoreSetDto();
                StoreDto sdto2 = new StoreDto();
                sdto2.data("A_FAKE_PAYLOAD_TO_TEST_OVERRIDE");
                sdto2.since(1000L);
                sdto2.streamerInfo("A_FAKE_STREAMER_INFO");
                storeSetDto2.addresourcesItem(sdto2);
                final ResponseEntity<String> response4 = uploadJson(tagname, storeSetDto2,
                        "ascii", "none", "1.0", "0");
                assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                // Verify IOV loading
                final ResponseEntity<IovSetDto> response5 = testRestTemplate
                        .getForEntity("/crestapi/iovs?tagname=" + tagname, IovSetDto.class);
                response5.getBody().getResources().forEach(iov -> {
                            log.info("Found iov: {}", iov);
                        }
                );
                // Now store again the previous storeset
                final ResponseEntity<String> response6 = uploadJson(tagname, storeSetDto, "ascii"
                        , "none",
                        "1.0", "0");
                assertThat(response6.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                final ResponseEntity<IovSetDto> response7 = testRestTemplate
                        .getForEntity("/crestapi/iovs?tagname=" + tagname, IovSetDto.class);
                response7.getBody().getResources().forEach(iov -> {
                            log.info("Found iov: {}", iov);
                        }
                );
                checkIovs(tagname);
                checkPayloadInfo(tagname);
            }
        }
        catch (JsonProcessingException e) {
            log.error("Error in processing json: ", e);
        }
    }

    public void checkIovs(String tagname) {
        String url = "/crestapi/iovs?tagname=" + tagname + "&snapshot=0" + "&since=0"
                + "&until=1000000" + "&size=1000" + "&page=0";
        final ResponseEntity<IovSetDto> response = testRestTemplate
                .getForEntity(url, IovSetDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        IovSetDto iovs = response.getBody();
        assertThat(iovs).isNotNull();
        assertThat(iovs.getResources()).isNotNull();
        response.getBody().getResources().forEach(iov -> {
            log.info("Found iov: {}", iov);
            checkPayload(iov.getPayloadHash());
        });

        // Now query using payload hash
        url = "/crestapi/iovs?tagname=" + tagname + "&hash=somefakehash";
        final ResponseEntity<IovSetDto> response2 = testRestTemplate.getForEntity(url, IovSetDto.class);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
        IovSetDto iovs2 = response2.getBody();
        assertThat(iovs2).isNotNull();
        assertThat(iovs2.getResources()).isNotNull();
        assertThat(iovs2.getResources().size()).isEqualTo(0);
    }

    public void checkPayload(String hash) {
        String url = "/crestapi/payloads?hash=" + hash;
        final ResponseEntity<String> response = testRestTemplate
                .getForEntity(url, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        url = "/crestapi/payloads/data?hash=" + hash;
        final ResponseEntity<String> response2 = testRestTemplate
                .getForEntity(url, String.class);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
    }


    public void checkPayloadInfo(String tagname) {
        String url = "/crestapi/monitoring/payloads?tagname=" + tagname;
        final ResponseEntity<String> response = testRestTemplate
                .getForEntity(url, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        {
            log.info("Received response: {}", response);
            assertThat(response.getBody()).isNotNull();
        }
    }

    public ResponseEntity<String> uploadJson(String tag, StoreSetDto storesetDto,
                                             String objectType, String compressionType,
                                             String version, String endtime) throws JsonProcessingException {
        // Prepare the multipart request
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        byte[] data = mapper.writeValueAsBytes(storesetDto);
        // Add the StoreSetDto as a file part
        ByteArrayResource storesetResource = new ByteArrayResource(data) {
            @Override
            public String getFilename() {
                return "storeset.json"; // Filename for the file part
            }
        };
        body.add("storeset", storesetResource);

        // Add other form parameters
        body.add("tag", tag);
        body.add("objectType", objectType);
        body.add("compressionType", compressionType);
        body.add("version", version);
        body.add("endtime", endtime);

        // Set headers for multipart/form-data
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // Create the request entity
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // Send the request
        //
        return testRestTemplate.exchange(
                "/crestapi/payloads", // Replace with the actual URL
                HttpMethod.PUT,
                requestEntity,
                String.class
        );
    }
}
