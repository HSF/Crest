package hep.crest.server.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import hep.crest.swagger.model.IovDto;
import hep.crest.swagger.model.IovPayloadDto;
import hep.crest.swagger.model.IovSetDto;
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
		System.out.println("Use json body : " + jsondto);
		MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
		map.add("file", new String(bindata));
		map.add("payload", jsondto);
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
		ResponseEntity<String> response = this.testRestTemplate.postForEntity("/crestapi/payloads/upload", request,
				String.class);

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
		ResponseEntity<String> response = this.testRestTemplate.postForEntity("/crestapi/payloads/store", request,
				String.class);

		System.out.println("Upload request gave response: " + response);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
	}

	@Test
	public void testE_storeBatchPayloadWithIov() {
		try {
			String content1 = "This is a fake payload batch 1";
			Files.write(Paths.get("/tmp/batch1.txt"), content1.getBytes());
			String content2 = "This is a fake payload batch 2";
			Files.write(Paths.get("/tmp/batch2.txt"), content2.getBytes());
			IovSetDto sdto = new IovSetDto();
			sdto.addIovsListItem(new IovPayloadDto().payload("file:///tmp/batch1.txt").since(new BigDecimal(100)));
			sdto.addIovsListItem(new IovPayloadDto().payload("file:///tmp/batch2.txt").since(new BigDecimal(200)));
			sdto.niovs(2L);
			sdto.format("FILE");
			String rooturi = this.testRestTemplate.getRootUri();
			System.out.println("Root URI is "+rooturi);
			String json = jacksonMapper.writeValueAsString(sdto);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);
			String tagname = "SB_TAG-PYLD";
			File f1 = new File("/tmp/batch1.txt");
			File f2 = new File("/tmp/batch2.txt");
			FileDataBodyPart filePart1 = new FileDataBodyPart("file", f1);
			FileDataBodyPart filePart2 = new FileDataBodyPart("file", f2);
			List<FormDataBodyPart> bodyParts = new ArrayList<>();
			bodyParts.add(filePart1);
			bodyParts.add(filePart2);

			CloseableHttpClient client = HttpClients.createDefault();
			HttpPost post = new HttpPost(rooturi+"/crestapi/payloads/uploadbatch");

			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			
			//MultipartBodyBuilder builder = new MultipartBodyBuilder();
			builder.addBinaryBody("files", f1, ContentType.DEFAULT_BINARY, "/tmp/batch1.txt");
			builder.addBinaryBody("files", f2, ContentType.DEFAULT_BINARY, "/tmp/batch2.txt");
			builder.addTextBody("tag", tagname);
			builder.addTextBody("iovsetupload", json);
			builder.addTextBody("endtime", "0");
			
			org.apache.http.HttpEntity entity = builder.build();
			post.setEntity(entity);
			HttpResponse response = client.execute(post);

			System.out.println("Upload batch request gave response: " + response);

			assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.CREATED.value());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
