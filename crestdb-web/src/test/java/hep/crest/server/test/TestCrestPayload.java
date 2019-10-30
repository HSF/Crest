package hep.crest.server.test;

import static org.assertj.core.api.Assertions.assertThat;

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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.ObjectMapper;

import hep.crest.swagger.model.PayloadDto;
import hep.crest.swagger.model.TagDto;
import hep.crest.testutils.DataGenerator;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestCrestPayload {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private ObjectMapper mapper;

    @Test
    public void testA_payloadApi() throws Exception {
        final PayloadDto dto = DataGenerator.generatePayloadDto("afakehashp01", "some data",
                "some info", "test");
        log.info("Store payload : {}", dto);
        final ResponseEntity<PayloadDto> response = this.testRestTemplate
                .postForEntity("/crestapi/payloads", dto, PayloadDto.class);
        log.info("Received response: " + response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        final HttpHeaders headers = new HttpHeaders();
        headers.add("X-Crest-PayloadFormat", "JSON");
        final HttpEntity<?> entity = new HttpEntity<>(headers);

        final ResponseEntity<String> resp1 = this.testRestTemplate.exchange(
                "/crestapi/payloads/" + dto.getHash(), HttpMethod.GET, entity, String.class);
        {
            log.info("Retrieved payload {} ", dto.getHash());
            final String responseBody = resp1.getBody();
            assertThat(resp1.getStatusCode()).isEqualTo(HttpStatus.OK);
            PayloadDto ok;
            log.info("Response from server is: " + responseBody);
            ok = mapper.readValue(responseBody, PayloadDto.class);
            assertThat(ok.getHash()).isEqualTo("afakehashp01");
        }

        // Now do not set the header and retrieve the binary data only
        final ResponseEntity<String> resp2 = this.testRestTemplate.exchange(
                "/crestapi/payloads/" + dto.getHash(), HttpMethod.GET, null, String.class);
        {
            log.info("Retrieved binary payload {} ", dto.getHash());
            assertThat(resp2.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        // Upload payload using external file
        final HttpHeaders headers1 = new HttpHeaders();
        headers1.setContentType(MediaType.MULTIPART_FORM_DATA);
        final PayloadDto dto1 = DataGenerator.generatePayloadDto("afakehashp02", "",
                "some other info", "test");
        log.info("Store payload : {}", dto1);
        String jsondto = "";
        jsondto = mapper.writeValueAsString(dto1);
        log.info("Upload payload: use json body {}", jsondto);
        final MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
        map.add("file", new String("some other data".getBytes()));
        map.add("payload", jsondto);
        final HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(
                map, headers1);
        final ResponseEntity<String> resp3 = this.testRestTemplate
                .postForEntity("/crestapi/payloads/upload", request, String.class);

        log.info("Upload request gave response: {}", resp3);
        assertThat(resp3.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    }

    @Test
    public void testA_payloadIovApi() {
        final TagDto dto = DataGenerator.generateTagDto("SB-TAG-PYLD-01", "run");
        log.info("Store tag for payload request: {}", dto);
        final ResponseEntity<TagDto> response = this.testRestTemplate
                .postForEntity("/crestapi/tags", dto, TagDto.class);
        log.info("Received response: " + response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    //
    // @Test
    // public void testB_storePayload() {
    // final byte[] bindata = new String("This is a fake payload").getBytes();
    // final PayloadDto dto = new PayloadDto().insertionTime(new
    // Date()).data(bindata).hash("AFAKEHASH").objectType("FAKE")
    // .streamerInfo(bindata).version("1");
    // System.out.println("Store payload request: " + dto);
    // final ResponseEntity<PayloadDto> response =
    // this.testRestTemplate.postForEntity("/crestapi/payloads", dto,
    // PayloadDto.class);
    // System.out.println("Received response: " + response);
    // assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    // }
    //
    // @Test
    // public void testC_storeIov() {
    // final IovDto dto = new IovDto().insertionTime(new Date()).since(new
    // BigDecimal("100")).payloadHash("AFAKEHASH")
    // .tagName("SB_TAG-PYLD");
    // System.out.println("Store iov request: " + dto);
    // final ResponseEntity<IovDto> response =
    // this.testRestTemplate.postForEntity("/crestapi/iovs", dto, IovDto.class);
    // System.out.println("Received response: " + response);
    // assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    // }
    //
    // @Test
    // public void testD_storePayload() {
    // final byte[] bindata = new String("This is another fake payload").getBytes();
    //
    // final HttpHeaders headers = new HttpHeaders();
    // headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    // final PayloadDto dto = new PayloadDto().insertionTime(new
    // Date()).hash("ANOTHERFAKEHASH").objectType("FAKE")
    // .streamerInfo(bindata).version("1");
    // String jsondto = "";
    // try {
    // jsondto = jacksonMapper.writeValueAsString(dto);
    // } catch (final JsonProcessingException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    // System.out.println("Upload payload: use json body " + jsondto);
    // final MultiValueMap<String, String> map = new LinkedMultiValueMap<String,
    // String>();
    // map.add("file", new String(bindata));
    // map.add("payload", jsondto);
    // final HttpEntity<MultiValueMap<String, String>> request = new
    // HttpEntity<MultiValueMap<String, String>>(map, headers);
    // final ResponseEntity<String> response =
    // this.testRestTemplate.postForEntity("/crestapi/payloads/upload", request,
    // String.class);
    //
    // System.out.println("Upload request gave response: " + response);
    //
    // assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    // }
    //
    // @Test
    // public void testD_storePayloadWithIov() {
    // final byte[] bindata = new String("This is yet another fake
    // payload").getBytes();
    //
    // final HttpHeaders headers = new HttpHeaders();
    // headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    // final String tagname = "SB_TAG-PYLD";
    // final MultiValueMap<String, String> map = new LinkedMultiValueMap<String,
    // String>();
    // map.add("file", new String(bindata));
    // map.add("since", "1000000");
    // map.add("endtime", "0");
    // map.add("tag", tagname);
    // final HttpEntity<MultiValueMap<String, String>> request = new
    // HttpEntity<MultiValueMap<String, String>>(map, headers);
    // final ResponseEntity<String> response =
    // this.testRestTemplate.postForEntity("/crestapi/payloads/store", request,
    // String.class);
    //
    // System.out.println("Upload request gave response: " + response);
    //
    // assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    // }
    //
    // @Test
    // public void testE_storeBatchPayloadWithIov() {
    // try {
    // final String content1 = "This is a fake payload batch 1";
    // Files.write(Paths.get("/tmp/batch1.txt"), content1.getBytes());
    // final String content2 = "This is a fake payload batch 2";
    // Files.write(Paths.get("/tmp/batch2.txt"), content2.getBytes());
    // final IovSetDto sdto = new IovSetDto();
    // sdto.addResourcesItem(new
    // IovDto().payloadHash("file:///tmp/batch1.txt").since(new BigDecimal(100)));
    // sdto.addResourcesItem(new
    // IovDto().payloadHash("file:///tmp/batch2.txt").since(new BigDecimal(200)));
    // sdto.size(2L);
    // sdto.format("FILE");
    // final String rooturi = this.testRestTemplate.getRootUri();
    // System.out.println("Root URI is "+rooturi);
    // final String json = jacksonMapper.writeValueAsString(sdto);
    // final HttpHeaders headers = new HttpHeaders();
    // headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    // final String tagname = "SB_TAG-PYLD";
    // final File f1 = new File("/tmp/batch1.txt");
    // final File f2 = new File("/tmp/batch2.txt");
    // final FileDataBodyPart filePart1 = new FileDataBodyPart("file", f1);
    // final FileDataBodyPart filePart2 = new FileDataBodyPart("file", f2);
    // final List<FormDataBodyPart> bodyParts = new ArrayList<>();
    // bodyParts.add(filePart1);
    // bodyParts.add(filePart2);
    //
    // final CloseableHttpClient client = HttpClients.createDefault();
    // final HttpPost post = new HttpPost(rooturi+"/crestapi/payloads/uploadbatch");
    //
    // final MultipartEntityBuilder builder = MultipartEntityBuilder.create();
    // builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
    //
    // //MultipartBodyBuilder builder = new MultipartBodyBuilder();
    // builder.addBinaryBody("files", f1, ContentType.DEFAULT_BINARY,
    // "/tmp/batch1.txt");
    // builder.addBinaryBody("files", f2, ContentType.DEFAULT_BINARY,
    // "/tmp/batch2.txt");
    // builder.addTextBody("tag", tagname);
    // builder.addTextBody("iovsetupload", json);
    // builder.addTextBody("endtime", "0");
    //
    // final org.apache.http.HttpEntity entity = builder.build();
    // post.setEntity(entity);
    // final HttpResponse response = client.execute(post);
    //
    // System.out.println("Upload batch request gave response: " + response);
    //
    // assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.CREATED.value());
    // } catch (final IOException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    //
    // }

}
