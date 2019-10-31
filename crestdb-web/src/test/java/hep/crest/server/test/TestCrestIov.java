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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

import hep.crest.swagger.model.CrestBaseResponse;
import hep.crest.swagger.model.GenericMap;
import hep.crest.swagger.model.IovDto;
import hep.crest.swagger.model.IovSetDto;
import hep.crest.swagger.model.PayloadDto;
import hep.crest.swagger.model.TagDto;
import hep.crest.swagger.model.TagSummarySetDto;
import hep.crest.testutils.DataGenerator;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestCrestIov {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private ObjectMapper mapper;

    @Test
    public void testA_iovApi() throws Exception {
        final PayloadDto pdto = DataGenerator.generatePayloadDto("afakehashiov01", "some iov data",
                "some info", "txt");
        log.info("Store payload : {}", pdto);
        final ResponseEntity<PayloadDto> response = this.testRestTemplate
                .postForEntity("/crestapi/payloads", pdto, PayloadDto.class);
        log.info("Received response: " + response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        final PayloadDto pdto2 = DataGenerator.generatePayloadDto("afakehashiov02", "some iov data for another payload",
                "some info", "txt");
        log.info("Store payload : {}", pdto2);
        final ResponseEntity<PayloadDto> response2 = this.testRestTemplate
                .postForEntity("/crestapi/payloads", pdto2, PayloadDto.class);
        log.info("Received response: " + response2);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        final TagDto tdto = DataGenerator.generateTagDto("SB-TAG-IOV-01", "run");
        log.info("Store tag for payload request: {}", tdto);
        final ResponseEntity<TagDto> resptag = this.testRestTemplate
                .postForEntity("/crestapi/tags", tdto, TagDto.class);
        log.info("Received response: " + resptag);
        assertThat(resptag.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Store iov for payload pdto
        final IovDto iovdto = DataGenerator.generateIovDto(pdto.getHash(), tdto.getName(), new BigDecimal(1000000L));
        log.info("Store iov : {}", iovdto);
        final ResponseEntity<IovDto> iovresp = this.testRestTemplate
                .postForEntity("/crestapi/iovs", iovdto, IovDto.class);
        log.info("Received response: " + iovresp);
        assertThat(iovresp.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Upload batch iovs
        iovdto.setSince(new BigDecimal(2000000L)); // change the since to have a new iov...
        final IovDto iovdto2 = DataGenerator.generateIovDto(pdto2.getHash(), tdto.getName(), new BigDecimal(2100000L));
        final IovSetDto setdto = new IovSetDto();
        setdto.format("iovs").size(2L);
        final GenericMap filters = new GenericMap();
        filters.put("tagName", tdto.getName());
        setdto.datatype("iovs").filter(filters);
        setdto.addResourcesItem(iovdto).addResourcesItem(iovdto2);
 
        final ResponseEntity<String> iovresp2 = this.testRestTemplate
                .postForEntity("/crestapi/iovs/storebatch", setdto, String.class);
        log.info("Received response: " + iovresp2);
        assertThat(iovresp2.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        {
            final String responseBody = iovresp2.getBody();
            IovSetDto ok;
            log.info("Response from server is: " + responseBody);
            ok = mapper.readValue(responseBody, IovSetDto.class);
            assertThat(ok.getSize()).isGreaterThan(0);
        }

        // Check without tagname in filters
        final GenericMap filters2 = new GenericMap();
        filters2.put("tag", tdto.getName());
        setdto.datatype("iovs").filter(filters2);
 
        final ResponseEntity<String> iovresp3 = this.testRestTemplate
                .postForEntity("/crestapi/iovs/storebatch", setdto, String.class);
        log.info("Received response: " + iovresp3);
        assertThat(iovresp3.getStatusCode()).isEqualTo(HttpStatus.NOT_ACCEPTABLE);
     
        // Check without tagname in iovs
        final GenericMap filters3= new GenericMap();
        filters3.put("tagName", tdto.getName());

        final IovSetDto setdto3 = new IovSetDto();
        setdto3.format("iovs").size(2L);
        iovdto.tagName(null); iovdto2.tagName(null);
        iovdto.since(new BigDecimal(4000000L));
        iovdto2.since(new BigDecimal(4100000L));
        setdto3.addResourcesItem(iovdto).addResourcesItem(iovdto2);
        setdto3.datatype("iovs").filter(filters3);

        final ResponseEntity<String> iovresp4 = this.testRestTemplate
                .postForEntity("/crestapi/iovs/storebatch", setdto3, String.class);
        log.info("Received response: " + iovresp4);
        assertThat(iovresp4.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        
    }

    @Test
    public void testA_iovfail() {
        // Store iov for payload pdto
        final IovDto iovdto = DataGenerator.generateIovDto(null, "SOME-TAG", new BigDecimal(1000000L));
        log.info("Store bad iov : {}", iovdto);
        final ResponseEntity<String> iovresp = this.testRestTemplate
                .postForEntity("/crestapi/iovs", iovdto, String.class);
        log.info("Received response: " + iovresp);
        assertThat(iovresp.getStatusCode()).isGreaterThanOrEqualTo(HttpStatus.OK);
        
        final ResponseEntity<String> resp = this.testRestTemplate
                .exchange("/crestapi/iovs?by=tagname:NONE-01,insertiontime<0", HttpMethod.GET, null, String.class);
        assertThat(resp.getStatusCode()).isGreaterThanOrEqualTo(HttpStatus.OK);  
        final ResponseEntity<String> resp1 = this.testRestTemplate
                .exchange("/crestapi/iovs?by=insertiontime<0", HttpMethod.GET, null, String.class);
        assertThat(resp1.getStatusCode()).isGreaterThanOrEqualTo(HttpStatus.OK);  
        final ResponseEntity<String> resp2 = this.testRestTemplate
                .exchange("/crestapi/iovs/selectGroups?tagname=NONR", HttpMethod.GET, null, String.class);
        assertThat(resp2.getStatusCode()).isGreaterThanOrEqualTo(HttpStatus.OK);  
        final ResponseEntity<String> resp3 = this.testRestTemplate
                .exchange("/crestapi/iovs/selectIovs?tagname=NONR", HttpMethod.GET, null, String.class);
        assertThat(resp3.getStatusCode()).isGreaterThanOrEqualTo(HttpStatus.OK);  
        final ResponseEntity<String> resp4 = this.testRestTemplate
                .exchange("/crestapi/iovs/selectSnapshot?tagname=NONR", HttpMethod.GET, null, String.class);
        assertThat(resp4.getStatusCode()).isGreaterThanOrEqualTo(HttpStatus.OK);  
        final ResponseEntity<String> resp5 = this.testRestTemplate
                .exchange("/crestapi/iovs/lastIov?tagname=NONR", HttpMethod.GET, null, String.class);
        assertThat(resp5.getStatusCode()).isGreaterThanOrEqualTo(HttpStatus.OK);  

    }
 
    @Test
    public void testB_findiovApi() throws Exception {

        final ResponseEntity<String> resp = this.testRestTemplate
                .exchange("/crestapi/iovs?by=tagname:SB-TAG-IOV-01,insertiontime>0", HttpMethod.GET, null, String.class);

        {
            log.info("Retrieved iov list " + resp.getBody());
            final String responseBody = resp.getBody();
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            IovSetDto ok;
            log.info("Response from server is: " + responseBody);
            ok = mapper.readValue(responseBody, IovSetDto.class);
            assertThat(ok.getSize()).isGreaterThan(0);
        }

        final ResponseEntity<String> resp1 = this.testRestTemplate
                .exchange("/crestapi/iovs/getSize?tagname=SB-TAG-IOV-01&snapshot=0", HttpMethod.GET, null, String.class);

        {
            log.info("Retrieved iov size " + resp1.getBody());
            final String responseBody = resp1.getBody();
            assertThat(resp1.getStatusCode()).isEqualTo(HttpStatus.OK);
            CrestBaseResponse ok;
            log.info("Response from server is: " + responseBody);
            ok = mapper.readValue(responseBody, CrestBaseResponse.class);
            assertThat(ok.getSize()).isGreaterThan(0);
        }

        final ResponseEntity<String> resp2 = this.testRestTemplate
                .exchange("/crestapi/iovs/getSizeByTag?tagname=SB%", HttpMethod.GET, null, String.class);

        {
            log.info("Retrieved iov size " + resp2.getBody());
            final String responseBody = resp2.getBody();
            assertThat(resp2.getStatusCode()).isEqualTo(HttpStatus.OK);
            TagSummarySetDto ok;
            log.info("Response from server is: " + responseBody);
            ok = mapper.readValue(responseBody, TagSummarySetDto.class);
            assertThat(ok.getSize()).isGreaterThan(0);
        }
        
        final ResponseEntity<String> resp3 = this.testRestTemplate
                .exchange("/crestapi/iovs/selectGroups?tagname=SB-TAG-IOV-01&snapshot=0", HttpMethod.GET, null, String.class);

        {
            log.info("Retrieved iov groups " + resp3.getBody());
            final String responseBody = resp3.getBody();
            assertThat(resp3.getStatusCode()).isEqualTo(HttpStatus.OK);
            IovSetDto ok;
            log.info("Response from server is: " + responseBody);
            ok = mapper.readValue(responseBody, IovSetDto.class);
            assertThat(ok.getSize()).isGreaterThan(0);
        }
        
        final ResponseEntity<String> resp4 = this.testRestTemplate
                .exchange("/crestapi/iovs/selectIovs?tagname=SB-TAG-IOV-01&since=0&until=3900000&snapshot=0", HttpMethod.GET, null, String.class);

        {
            log.info("Retrieved iov selection " + resp4.getBody());
            final String responseBody = resp4.getBody();
            assertThat(resp4.getStatusCode()).isEqualTo(HttpStatus.OK);
            IovSetDto ok;
            log.info("Response from server is: " + responseBody);
            ok = mapper.readValue(responseBody, IovSetDto.class);
            assertThat(ok.getSize()).isGreaterThan(0);
        }

        final ResponseEntity<String> resp5 = this.testRestTemplate
                .exchange("/crestapi/iovs/selectSnapshot?tagname=SB-TAG-IOV-01&snapshot=0", HttpMethod.GET, null, String.class);

        {
            log.info("Retrieved iov selection " + resp5.getBody());
            final String responseBody = resp5.getBody();
            assertThat(resp5.getStatusCode()).isEqualTo(HttpStatus.OK);
            IovSetDto ok;
            log.info("Response from server is: " + responseBody);
            ok = mapper.readValue(responseBody, IovSetDto.class);
            assertThat(ok.getSize()).isGreaterThan(0);
        }

        final ResponseEntity<String> resp6 = this.testRestTemplate
                .exchange("/crestapi/iovs?by=tagname:SB-TAG-IOV-01,since<39000000&page=0&size=10", HttpMethod.GET, null, String.class);

        {
            log.info("Retrieved iov selection " + resp6.getBody());
            final String responseBody = resp6.getBody();
            assertThat(resp6.getStatusCode()).isEqualTo(HttpStatus.OK);
            IovSetDto ok;
            log.info("Response from server is: " + responseBody);
            ok = mapper.readValue(responseBody, IovSetDto.class);
            assertThat(ok.getSize()).isGreaterThan(0);
        }

        final ResponseEntity<String> resp7 = this.testRestTemplate
                .exchange("/crestapi/iovs/lastIov?tagname=SB-TAG-IOV-01&since=39000000", HttpMethod.GET, null, String.class);

        {
            log.info("Retrieved iov selection " + resp7.getBody());
            final String responseBody = resp7.getBody();
            assertThat(resp7.getStatusCode()).isEqualTo(HttpStatus.OK);
            IovSetDto ok;
            log.info("Response from server is: " + responseBody);
            ok = mapper.readValue(responseBody, IovSetDto.class);
            assertThat(ok.getSize()).isGreaterThan(0);
        }


    }

}
