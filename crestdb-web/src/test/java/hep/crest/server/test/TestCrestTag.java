package hep.crest.server.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import hep.crest.swagger.model.GenericMap;
import hep.crest.swagger.model.GlobalTagMapDto;
import hep.crest.swagger.model.TagDto;
import hep.crest.swagger.model.TagMetaDto;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ActiveProfiles("default")
public class TestCrestTag {
	@Autowired
	private TestRestTemplate testRestTemplate;

	@Test
	public void testA_getAndRemoveTags() {
		ResponseEntity<TagDto[]> response = this.testRestTemplate.getForEntity("/crestapi/tags", TagDto[].class);
		TagDto[] taglist = response.getBody();
		for (TagDto tagDto : taglist) {
			String url = "/crestapi/admin/tags/" + tagDto.getName();
			System.out.println("Removing tag " + url);
			this.testRestTemplate.delete(url);
		}
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody().length).isGreaterThanOrEqualTo(0);
	}

	@Test
	public void testB_storeTags() {
		TagDto dto = new TagDto().description("test").name("SB_TAG").endOfValidity(new BigDecimal(1))
				.lastValidatedTime(new BigDecimal(1)).payloadSpec("test").synchronization("BLK").timeType("run")
				.modificationTime(new Date()).insertionTime(new Date());
		System.out.println("Store request: " + dto);
		ResponseEntity<TagDto> response = this.testRestTemplate.postForEntity("/crestapi/tags", dto, TagDto.class);
		System.out.println("Received response: " + response);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		
		GlobalTagMapDto mapdto = new GlobalTagMapDto().globalTagName("MY_SB_TEST").label("label").record("0").tagName("SB_TAG");
        System.out.println("Store request: "+mapdto);
        ResponseEntity<GlobalTagMapDto> response2 = this.testRestTemplate.postForEntity("/crestapi/globaltagmaps", mapdto, GlobalTagMapDto.class);
        System.out.println("Received response: "+response2);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.CREATED);
	}

	@Test
	public void testC_storeTagMeta() {
		TagMetaDto dto = new TagMetaDto().description("<some>description</some>").tagName("SB_TAG").channelInfo("[{\"0\": \"achannel\"},{ \"1\":\"another_chan\"}]"
				).payloadInfo("{ \"col1\": \"Int\"}").chansize(2).colsize(1);
		System.out.println("Store tag meta request: " + dto);
		ResponseEntity<TagMetaDto> response = this.testRestTemplate.postForEntity("/crestapi/tags/SB_TAG/meta", dto, TagMetaDto.class);
		System.out.println("Received response: " + response);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
	}
	
	@Test
	public void testC_updateTagMeta() {
		GenericMap updmeta = new GenericMap();
		updmeta.put("description", "another desc");
		final String url = String.format("/crestapi/tags/{id}/meta");
		//String url = "/crestapi/tags/SB_TAG/meta";
	    Map<String, String> param = new HashMap<String, String>();
	    param.put("id","SB_TAG");
	    HttpHeaders headers = new HttpHeaders();

	    HttpEntity<GenericMap> requestEntity = new HttpEntity<GenericMap>(updmeta,headers);
	    ResponseEntity<TagMetaDto> response = this.testRestTemplate.exchange(url, HttpMethod.PUT, requestEntity, TagMetaDto.class, param);
		System.out.println("Received response: " + response);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
	}

	@Test
	public void testD_getAllTags() {
		ResponseEntity<TagDto[]> response = this.testRestTemplate.getForEntity("/crestapi/tags", TagDto[].class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody().length).isGreaterThanOrEqualTo(0);
	}

}
