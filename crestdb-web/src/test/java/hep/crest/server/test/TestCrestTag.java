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
import hep.crest.swagger.model.TagDto;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestCrestTag {
    @Autowired
    private TestRestTemplate testRestTemplate;
   
    @Test
    public void testA_getAndRemoveTags() {
        ResponseEntity<TagDto[]> response = this.testRestTemplate.getForEntity("/crestapi/tags", TagDto[].class);
        TagDto[] taglist = response.getBody();
        for (TagDto tagDto : taglist) {
        		String url = "/crestapi/admin/tags/"+tagDto.getName();
        		System.out.println("Removing tag "+url);
            this.testRestTemplate.delete(url);
		}
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().length).isGreaterThanOrEqualTo(0);
    }

   @Test
    public void testB_storeTags() {
    		TagDto dto = new TagDto().description("test").name("SB_TAG").endOfValidity(new BigDecimal(1)).lastValidatedTime(new BigDecimal(1)).objectType("test").synchronization("BLK").timeType("run").modificationTime(new Date()).insertionTime(new Date());
        System.out.println("Store request: "+dto);
        ResponseEntity<TagDto> response = this.testRestTemplate.postForEntity("/crestapi/tags", dto, TagDto.class);
        System.out.println("Received response: "+response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }
    
    @Test
    public void testC_getAllTags() {
        ResponseEntity<TagDto[]> response = this.testRestTemplate.getForEntity("/crestapi/tags", TagDto[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().length).isGreaterThanOrEqualTo(0);
    }

}
