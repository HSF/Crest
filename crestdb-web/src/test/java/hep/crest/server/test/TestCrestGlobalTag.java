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
import org.springframework.test.context.junit4.SpringRunner;

import hep.crest.swagger.model.GlobalTagDto;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestCrestGlobalTag {
    @Autowired
    private TestRestTemplate testRestTemplate;
   
    @Test
    public void testA_storeGlobaltags() {
    		GlobalTagDto dto = new GlobalTagDto().description("test").name("MY_SB_TEST").release("1").scenario("test").type("test").workflow("M").validity(new BigDecimal(0)).snapshotTime(new Date()).insertionTime(new Date());
        System.out.println("Store request: "+dto);
        ResponseEntity<GlobalTagDto> response = this.testRestTemplate.postForEntity("/crestapi/globaltags", dto, GlobalTagDto.class);
        System.out.println("Received response: "+response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }
    
    @Test
    public void testB_getAllGlobaltags() {
        ResponseEntity<GlobalTagDto[]> response = this.testRestTemplate.getForEntity("/crestapi/globaltags", GlobalTagDto[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().length).isGreaterThanOrEqualTo(0);
    }

}
