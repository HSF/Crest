package hep.crest.server.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import hep.crest.swagger.model.IovDto;
import hep.crest.swagger.model.PayloadDto;
import hep.crest.swagger.model.TagDto;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ActiveProfiles("default") 
public class TestCrestPayload {
    @Autowired
    private TestRestTemplate testRestTemplate;
   
    @Test
    public void testA_storeTags() {
    		TagDto dto = new TagDto().description("test").name("SB_TAG-PYLD").endOfValidity(new BigDecimal(1)).lastValidatedTime(new BigDecimal(1)).objectType("test").synchronization("BLK").timeType("run").modificationTime(new Date()).insertionTime(new Date());
        System.out.println("Store request: "+dto);
        ResponseEntity<TagDto> response = this.testRestTemplate.postForEntity("/crestapi/tags", dto, TagDto.class);
        System.out.println("Received response: "+response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }
     
    @Test
    public void testB_storePayload() {
    		byte[] bindata = new String("This is a fake payload").getBytes();
    		PayloadDto dto = new PayloadDto().insertionTime(new Date()).data(bindata).hash("AFAKEHASH").objectType("FAKE").streamerInfo(bindata).version("1");
        System.out.println("Store payload request: "+dto);
        ResponseEntity<PayloadDto> response = this.testRestTemplate.postForEntity("/crestapi/payloads", dto, PayloadDto.class);
        System.out.println("Received response: "+response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    public void testC_storeIov() {
    		IovDto dto = new IovDto().insertionTime(new Date()).since(new BigDecimal("100")).payloadHash("AFAKEHASH").tagName("SB_TAG-PYLD");
        System.out.println("Store payload request: "+dto);
        ResponseEntity<IovDto> response = this.testRestTemplate.postForEntity("/crestapi/iovs", dto, IovDto.class);
        System.out.println("Received response: "+response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

}
