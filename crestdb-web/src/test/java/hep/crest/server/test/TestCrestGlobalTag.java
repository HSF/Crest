package hep.crest.server.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import hep.crest.server.swagger.api.ApiResponseMessage;
import hep.crest.swagger.model.GlobalTagDto;
import hep.crest.swagger.model.GlobalTagSetDto;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ActiveProfiles("default")
public class TestCrestGlobalTag {

	@Autowired
	private TestRestTemplate testRestTemplate;

	@Autowired
	@Qualifier("jacksonMapper")
	private ObjectMapper mapper;

	@Test
	public void testA_getAndRemoveGlobaltags() {

		ResponseEntity<String> response = this.testRestTemplate.exchange("/crestapi/globaltags", HttpMethod.GET, null, String.class);
		//ResponseEntity<String> response = this.testRestTemplate.getForEntity("/crestapi/globaltags", null);
		if (response.getStatusCode() == HttpStatus.OK) {
			GlobalTagSetDto ok;
			try {
				if (mapper == null) {
					System.out.println("Cannot use mapper because it is null");
				}
				System.out.println("testA_getAndRemoveGlobaltags => Received response "+response.getBody());
				ok = mapper.readValue(response.getBody(), GlobalTagSetDto.class);
				List<GlobalTagDto> gtaglist = ok.getResources();
				for (GlobalTagDto globalTagDto : gtaglist) {
					String url = "/crestapi/admin/globaltags/" + globalTagDto.getName();
					System.out.println("Removing global tag " + url);
					this.testRestTemplate.delete(url);
				}
				assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
				assertThat(ok.getSize()).isGreaterThanOrEqualTo(0);
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
			try {
				ApiResponseMessage err = mapper.readValue(response.getBody(), ApiResponseMessage.class);
				System.out.println("Response error in global tag " + err.getMessage());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Test
	public void testB_storeGlobaltags() {
		GlobalTagDto dto = new GlobalTagDto().description("test").name("MY_SB_TEST").release("1").scenario("test")
				.type("test").workflow("M").validity(new BigDecimal(0)).snapshotTime(new Date())
				.insertionTime(new Date());
		System.out.println("Store request: " + dto);
		ResponseEntity<GlobalTagDto> response = this.testRestTemplate.postForEntity("/crestapi/globaltags", dto,
				GlobalTagDto.class);
		System.out.println("Received response: " + response);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
	}

	@Test
	public void testC_getAllGlobaltags() {
		ResponseEntity<String> response = this.testRestTemplate.exchange("/crestapi/globaltags", HttpMethod.GET, null, String.class);
		if (response.getStatusCode() == HttpStatus.OK) {
			GlobalTagSetDto ok;
			try {
				if (mapper == null) {
					System.out.println("Cannot use mapper because it is null");
				}
				ok = mapper.readValue(response.getBody(), GlobalTagSetDto.class);
				List<GlobalTagDto> gtaglist = ok.getResources();
				for (GlobalTagDto globalTagDto : gtaglist) {
					String url = "/crestapi/admin/globaltags/" + globalTagDto.getName();
					System.out.println("Removing global tag " + url);
					this.testRestTemplate.delete(url);
				}
				assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
				assertThat(ok.getSize()).isGreaterThanOrEqualTo(0);
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
			try {
				ApiResponseMessage err = mapper.readValue(response.getBody(), ApiResponseMessage.class);
				System.out.println("Response error in global tag " + err.getMessage());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
