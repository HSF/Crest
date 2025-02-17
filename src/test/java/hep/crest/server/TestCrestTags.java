package hep.crest.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hep.crest.server.converters.HashGenerator;
import hep.crest.server.data.pojo.Iov;
import hep.crest.server.data.pojo.TagMeta;
import hep.crest.server.data.pojo.TagSynchroEnum;
import hep.crest.server.services.IovService;
import hep.crest.server.services.TagMetaService;
import hep.crest.server.swagger.model.GenericMap;
import hep.crest.server.swagger.model.GlobalTagDto;
import hep.crest.server.swagger.model.GlobalTagMapDto;
import hep.crest.server.swagger.model.IovDto;
import hep.crest.server.swagger.model.IovSetDto;
import hep.crest.server.swagger.model.StoreDto;
import hep.crest.server.swagger.model.StoreSetDto;
import hep.crest.server.swagger.model.TagDto;
import hep.crest.server.swagger.model.TagMetaDto;
import hep.crest.server.swagger.model.TagSetDto;
import hep.crest.server.swagger.model.TagSummarySetDto;
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

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration
@Slf4j
public class TestCrestTags {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private IovService iovService;
    @Autowired
    private TagMetaService tagMetaService;

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
        // Try to insert the same
        final ResponseEntity<String> responseconflict = testRestTemplate
                .postForEntity("/crestapi/tags", dto, String.class);
        log.info("Received response: {}", responseconflict);
        assertThat(responseconflict.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    public void testTagMeta(String tname) {
        TagMetaDto dto = (TagMetaDto) rnd.generate(TagMetaDto.class);
        dto.tagName(tname);
        dto.tagInfo("some info on the payload content");
        log.info("Store tag meta : {} ", dto);
        final ResponseEntity<TagMetaDto> response = testRestTemplate
                .postForEntity("/crestapi/tags/" + tname + "/meta", dto, TagMetaDto.class);
        log.info("Received response: {}", response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Now get the meta
        TagMeta meta = tagMetaService.find(tname);
        assertThat(meta).isNotNull();
        // Update tag meta
        meta.setChansize(1000);
        meta.setColsize(2);
        TagMeta updmeta = tagMetaService.updateTagMeta(meta);
        assertThat(updmeta).isNotNull();
        TagMeta mne = new TagMeta();
        mne.setTagName("notexists");
        try {
            TagMeta notexists = tagMetaService.updateTagMeta(mne);
        }
        catch (Exception e) {
            log.info("Caught exception: ", e);
        }

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
        testTagMeta(tagname);
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
                updateTag(tagname);
                removeTag(tagname, "A-TEST-GT-50");
            }
        }
        catch (JsonProcessingException e) {
            log.error("Error in processing json: ", e);
        }
    }

    @Test
    public void testTagPayloadRest() {
        log.info("=======> testTagPayloadRest ");
        String tagname = "A-CRESTTAG-60";
        initializeGtag("A-TEST-GT-60");
        initializeTag(tagname);
        GlobalTagMapDto mapDto = new GlobalTagMapDto();
        mapDto.tagName(tagname)
                .globalTagName("A-TEST-GT-60").record("some-rec").label("TEST-6");
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

        // Now fill the tag with IOVs
        StoreSetDto storeSetDto = new StoreSetDto();
        StoreDto sdto = new StoreDto();
        sdto.data("A_BATCH_FAKE_PAYLOAD_TO_TEST");
        sdto.since(9000L);
        sdto.streamerInfo("BATCH_FAKE_STREAMER_INFO");
        storeSetDto.addresourcesItem(sdto);
        StoreDto sdto1 = new StoreDto();
        sdto1.data("ANOTHER_BATCH_FAKE_PAYLOAD_TO_TEST");
        sdto1.since(10000L);
        sdto1.streamerInfo("ANOTHER_BATCH_FAKE_STREAMER_INFO");
        storeSetDto.addresourcesItem(sdto1);
        // Store the payloads into the tag
        try {
            final ResponseEntity<String> response2 = uploadPayload(tagname, storeSetDto, "ascii", "none",
                    "1.0", "0");
            {
                log.info("Created payloads batch for tag {} ", response2.getBody());
                assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                // Verify IOV loading
                final ResponseEntity<IovSetDto> response3 = testRestTemplate
                        .getForEntity("/crestapi/iovs?tagname=" + tagname, IovSetDto.class);
                assertThat(response3.getStatusCode()).isEqualTo(HttpStatus.OK);
                response3.getBody().getResources().forEach(iov -> {
                    log.info("Found iov: {}", iov);
                });
                copyIovs(tagname, storeSetDto);
            }
        }
        catch (JsonProcessingException e) {
            log.error("Error in processing json: ", e);
        }
    }


    public void copyIovs(String tagname, StoreSetDto storeSetDto) {
        IovSetDto iovSetDto = new IovSetDto();
        List<IovDto> iovDtoList = new ArrayList<>();
        try {
            for (StoreDto storeDto : storeSetDto.getResources()) {
                IovDto iovDto = new IovDto();
                String hash = HashGenerator.sha256Hash(storeDto.getData().getBytes());
                iovDto.payloadHash(hash);
                iovDto.since(storeDto.getSince());
                iovDto.setTagName("COPY-TAG");
                iovDtoList.add(iovDto);
            }
            iovSetDto.resources(iovDtoList);
            iovSetDto.format("IovSetDto");
            iovSetDto.size((long)iovDtoList.size());
            // Now store the list of iovs in a new tag
            initializeTag("COPY-TAG");
            String url = "/crestapi/iovs";
            final ResponseEntity<String> response2 = testRestTemplate.exchange(
                    url, HttpMethod.POST, new HttpEntity<>(iovSetDto), String.class);
            log.info("Created iovs for tag COPY-TAG ");
            assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            // Add one iov only
            IovDto iovDto = iovSetDto.getResources().get(0);
            iovDto.setTagName("COPY-TAG");
            iovDto.setSince(2000000L);
            // Store it
            String url2 = "/crestapi/iovs";
            final ResponseEntity<String> response3 = testRestTemplate.exchange(
                    url2, HttpMethod.PUT, new HttpEntity<>(iovDto), String.class);
            assertThat(response3.getStatusCode()).isEqualTo(HttpStatus.CREATED);

            // Now get size by tag
            String urltag = "/crestapi/iovs/size?tagname=COPY-TAG";
            final ResponseEntity<String> resptags = this.testRestTemplate
                    .exchange(urltag, HttpMethod.GET, null,
                            String.class);
            {
                log.info("Retrieved iovs size " + resptags.getBody());
                assertThat(resptags.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(resptags.getBody()).isNotNull();
                TagSummarySetDto responseBody = (TagSummarySetDto) mapper.readValue(
                        resptags.getBody(), TagSummarySetDto.class);
                assertThat(responseBody.getResources().size()).isEqualTo(1);
            }

            String urlrange = "/crestapi/iovs?tagname=COPY-TAG&since=9900&until=1000000&snapshot=10"
                    + "&method=AT";
            final ResponseEntity<IovSetDto> resprange = this.testRestTemplate
                    .exchange(urlrange, HttpMethod.GET, null,
                            IovSetDto.class);
            {
                log.info("Retrieved iovs snapshot 10 AT 9900" + resprange.getBody());
                assertThat(resprange.getStatusCode()).isEqualTo(HttpStatus.OK);
            }

            // Select groups
            String urlgroup = "/crestapi/iovs?tagname=COPY-TAG&since=0&until=1000000"
                    + "&method=GROUPS";
            final ResponseEntity<String> respgroup = this.testRestTemplate
                    .exchange(urlgroup, HttpMethod.GET, null,
                            String.class);
            {
                log.info("Retrieved iovs snapshot 10 GROUPS " + respgroup.getBody());
                assertThat(respgroup.getStatusCode()).isEqualTo(HttpStatus.OK);
            }

            // Select iovs and payloads meta
            String urlmeta = "/crestapi/iovs/infos?tagname=COPY-TAG&since=0&until=10000000";
            final ResponseEntity<String> respmeta = this.testRestTemplate
                    .exchange(urlmeta, HttpMethod.GET, null,
                            String.class);
            {
                log.info("Retrieved iovs payloads " + respmeta.getBody());
                assertThat(respmeta.getStatusCode()).isEqualTo(HttpStatus.OK);
            }

        }
        catch (NoSuchAlgorithmException e) {
            log.error("Error in processing json: ", e);
        }
        catch (JsonMappingException e) {
            throw new RuntimeException(e);
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException(e);
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

        Iov last = iovService.latest(tagname);
        assertThat(last).isNotNull();
        assertThat(last.getPayloadHash()).isNotNull();
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

    public void updateTag(String tagname) {
        String url = "/crestapi/tags/" + tagname;
        GenericMap dto = new GenericMap();
        dto.put("synchronization", TagSynchroEnum.NONE.type());
        dto.put("description", "A new description");
        dto.put("payloadSpec", "ascii2");
        testRestTemplate.put(url, dto);
        url = "/crestapi/tags/" + tagname;
        final ResponseEntity<TagSetDto> response2 = testRestTemplate
                .getForEntity(url, TagSetDto.class);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
        TagSetDto tags = response2.getBody();
        assertThat(tags).isNotNull();
        assertThat(tags.getResources()).isNotNull();

        url = "/crestapi/tags/notfound";
        dto = new GenericMap();
        dto.put("synchronization", TagSynchroEnum.NONE.type());
        dto.put("description", "A new description");
        dto.put("payloadSpec", "ascii2");
        testRestTemplate.put(url, dto);

    }

    public void removeTag(String tagname, String globaltagname) {
        String url = "/crestapi/admin/tags/" + tagname;
        ResponseEntity<String> resp = testRestTemplate
                .exchange(url, HttpMethod.DELETE, null, String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        url = "/crestapi/globaltagmaps/" + globaltagname + "?label=TEST-5&tagname=" + tagname;
        ResponseEntity<String> resp2 = testRestTemplate
                .exchange(url, HttpMethod.DELETE, null, String.class);
        assertThat(resp2.getStatusCode()).isEqualTo(HttpStatus.OK);
        url = "/crestapi/admin/tags/" + tagname;
        ResponseEntity<String> resp3 = testRestTemplate
                .exchange(url, HttpMethod.DELETE, null, String.class);
        assertThat(resp3.getStatusCode()).isEqualTo(HttpStatus.OK);
        url = "/crestapi/tags/" + tagname;
        ResponseEntity<String> resp4 = testRestTemplate
                .exchange(url, HttpMethod.GET, null, String.class);
        assertThat(resp4.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
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


    public ResponseEntity<String> uploadPayload(String tag, StoreSetDto storesetDto,
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
        headers.set("X-Crest-PayloadFormat", "JSON");
        // Create the request entity
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // Send the request
        //
        testRestTemplate.exchange(
                "/crestapi/payloads", // Replace with the actual URL
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        // Prepare the multipart request with an external data file
        byte[] dataf = mapper.writeValueAsBytes("This is my file with data");
        // Add the StoreSetDto as a file part
        ByteArrayResource fileresource = new ByteArrayResource(data) {
            @Override
            public String getFilename() {
                return "data.txt"; // Filename for the file part
            }
        };
        StoreSetDto storesetDto2 = new StoreSetDto();
        StoreDto sdto = new StoreDto();
        sdto.data("file://data.txt");
        sdto.since(999L);
        sdto.streamerInfo("A_FAKE_STREAMER_INFO");
        storesetDto2.addresourcesItem(sdto);
        MultiValueMap<String, Object> body2 = new LinkedMultiValueMap<>();
        body2.add("storeset", storesetDto2);
        body2.add("files", fileresource);
        // Add other form parameters
        body2.add("tag", tag);

        // Set headers for multipart/form-data
        HttpHeaders headers2 = new HttpHeaders();
        headers2.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers2.set("X-Crest-PayloadFormat", "FILE");
        // Create the request entity
        HttpEntity<MultiValueMap<String, Object>> requestEntity2 = new HttpEntity<>(body2,
                headers2);
        // Send the request
        //
        return testRestTemplate.exchange(
                "/crestapi/payloads", // Replace with the actual URL
                HttpMethod.POST,
                requestEntity2,
                String.class
        );

    }

}
