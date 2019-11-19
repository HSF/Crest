package hep.crest.server.test;

import static org.assertj.core.api.Assertions.assertThat;

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

import hep.crest.swagger.model.FolderDto;
import hep.crest.swagger.model.FolderSetDto;
import hep.crest.testutils.DataGenerator;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ActiveProfiles("test")
public class TestCrestFolders {
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    @Qualifier("jacksonMapper")
    private ObjectMapper mapper;


    @Test
    public void testA_folderApi() throws Exception {
        
        final FolderDto dto = DataGenerator.generateFolderDto("RTBLOB", "/MDT/API/RTBLOB",
                "COOLOFL_MDT");
        log.info("Store folder : {} ", dto);
        final ResponseEntity<FolderDto> response = this.testRestTemplate
                .postForEntity("/crestapi/folders", dto, FolderDto.class);
        log.info("Received response: {}", response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Try with the same
        final FolderDto dto1 = DataGenerator.generateFolderDto("APIRTBLOB", "/MDT/API/RTBLOB",
                "COOLOFL_MDT");
        log.info("Store folder with same nodefullpath : {} ", dto1);
        final ResponseEntity<String> response1 = this.testRestTemplate
                .postForEntity("/crestapi/folders", dto1, String.class);
        log.info("Received response: {}", response1);
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.SEE_OTHER);
        
        // Retrieve folder list
        final ResponseEntity<String> resp = this.testRestTemplate
                .exchange("/crestapi/folders?by=nodeFullpath:RT,tagPattern:%", HttpMethod.GET, null, String.class);

        {
            log.info("Retrieved global tag list " + resp.getBody());
            final String responseBody = resp.getBody();
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            FolderSetDto ok;
            log.info("Response from server is: " + responseBody);
            ok = mapper.readValue(responseBody, FolderSetDto.class);
            assertThat(ok.getSize()).isGreaterThan(0);
        }

    }

}
