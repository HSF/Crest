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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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

	@Autowired
	private ObjectMapper jacksonMapper;

	@Test
	public void testA_storeTags() {
		TagDto dto = new TagDto().description("test").name("SB_TAG-PYLD").endOfValidity(new BigDecimal(1))
				.lastValidatedTime(new BigDecimal(1)).payloadSpec("test").synchronization("BLK").timeType("run")
				.modificationTime(new Date()).insertionTime(new Date());
		System.out.println("Store request: " + dto);
		ResponseEntity<TagDto> response = this.testRestTemplate.postForEntity("/crestapi/tags", dto, TagDto.class);
		System.out.println("Received response: " + response);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
	}

	@Test
	public void testB_storePayload() {
		byte[] bindata = new String("This is a fake payload").getBytes();
		PayloadDto dto = new PayloadDto().insertionTime(new Date()).data(bindata).hash("AFAKEHASH").objectType("FAKE")
				.streamerInfo(bindata).version("1");
		System.out.println("Store payload request: " + dto);
		ResponseEntity<PayloadDto> response = this.testRestTemplate.postForEntity("/crestapi/payloads", dto,
				PayloadDto.class);
		System.out.println("Received response: " + response);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
	}

	
	@Test
	public void testC_storeIov() {
		IovDto dto = new IovDto().insertionTime(new Date()).since(new BigDecimal("100")).payloadHash("AFAKEHASH")
				.tagName("SB_TAG-PYLD");
		System.out.println("Store payload request: " + dto);
		ResponseEntity<IovDto> response = this.testRestTemplate.postForEntity("/crestapi/iovs", dto, IovDto.class);
		System.out.println("Received response: " + response);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
	}
	
	@Test
	public void testD_storePayload() {
		byte[] bindata = new String("This is another fake payload").getBytes();
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		PayloadDto dto = new PayloadDto().insertionTime(new Date()).hash("ANOTHERFAKEHASH").objectType("FAKE")
				.streamerInfo(bindata).version("1");
		String jsondto = "";
		try {
			jsondto = jacksonMapper.writeValueAsString(dto);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Use json body : "+jsondto);
		MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
		map.add("file", new String(bindata));
		map.add("payload", jsondto);
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
		ResponseEntity<String> response = this.testRestTemplate.postForEntity("/crestapi/payloads/upload", request, String.class);
		
		System.out.println("Upload request gave response: " + response);
		
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
	}

	@Test
	public void testD_storePayloadWithIov() {
		byte[] bindata = new String("This is yet another fake payload").getBytes();
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		String tagname = "SB_TAG-PYLD";
		MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
		map.add("file", new String(bindata));
		map.add("since", "1000000");
		map.add("endtime", "0");
		map.add("tag", tagname);
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
		ResponseEntity<String> response = this.testRestTemplate.postForEntity("/crestapi/payloads/store", request, String.class);
		
		System.out.println("Upload request gave response: " + response);
		
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
	}

}
