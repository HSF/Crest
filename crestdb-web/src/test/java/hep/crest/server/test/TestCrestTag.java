package hep.crest.server.test;

import static org.assertj.core.api.Assertions.assertThat;

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

import hep.crest.swagger.model.TagDto;
import hep.crest.swagger.model.TagSetDto;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ActiveProfiles("default")
public class TestCrestTag {
	@Autowired
	private TestRestTemplate testRestTemplate;

	@Test
	public void testA_getAndRemoveTags() {
		final ResponseEntity<TagSetDto> response = this.testRestTemplate.getForEntity("/crestapi/tags", TagSetDto.class);
        System.out.println("Found response " + response.getBody().toString());
		final TagSetDto tagset = response.getBody();
		for (final TagDto tagDto : tagset.getResources()) {
			final String url = "/crestapi/admin/tags/" + tagDto.getName();
			System.out.println("Removing tag " + url);
			this.testRestTemplate.delete(url);
		}
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody().getSize()).isGreaterThanOrEqualTo(0);
	}

//	@Test
//	public void testB_storeTags() {
//		final TagDto dto = new TagDto().description("test").name("SB_TAG").endOfValidity(new BigDecimal(1))
//				.lastValidatedTime(new BigDecimal(1)).payloadSpec("test").synchronization("BLK").timeType("run")
//				.modificationTime(new Date()).insertionTime(new Date());
//		System.out.println("Store request: " + dto);
//		final ResponseEntity<TagDto> response = this.testRestTemplate.postForEntity("/crestapi/tags", dto, TagDto.class);
//		System.out.println("Received response: " + response);
//		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
//		
//		final GlobalTagMapDto mapdto = new GlobalTagMapDto().globalTagName("MY_SB_TEST").label("label").record("1").tagName("SB_TAG");
//        System.out.println("Store request: "+mapdto);
//        final ResponseEntity<GlobalTagMapDto> response2 = this.testRestTemplate.postForEntity("/crestapi/globaltagmaps", mapdto, GlobalTagMapDto.class);
//        System.out.println("Received response: "+response2);
//        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.CREATED);
//	}

	@Test
	public void testC_getAllTags() {
		final ResponseEntity<TagSetDto> response = this.testRestTemplate.getForEntity("/crestapi/tags", TagSetDto.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody().getSize()).isGreaterThanOrEqualTo(0);
	}

}
