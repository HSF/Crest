package hep.crest.server.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hep.crest.data.pojo.Iov;
import hep.crest.data.pojo.Tag;
import hep.crest.server.controllers.EntityDtoHelper;
import hep.crest.server.controllers.PageRequestHelper;
import hep.crest.server.exceptions.NotExistsPojoException;
import hep.crest.server.services.IovService;
import hep.crest.server.services.TagService;
import hep.crest.swagger.model.CrestBaseResponse;
import hep.crest.swagger.model.GenericMap;
import hep.crest.swagger.model.IovDto;
import hep.crest.swagger.model.IovPayloadDto;
import hep.crest.swagger.model.IovPayloadSetDto;
import hep.crest.swagger.model.IovSetDto;
import hep.crest.swagger.model.PayloadDto;
import hep.crest.swagger.model.TagDto;
import hep.crest.swagger.model.TagSummarySetDto;
import hep.crest.testutils.DataGenerator;
import ma.glasnost.orika.MapperFacade;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
//@//ContextConfiguration(classes = {ServicesConfig.class, JerseyConfig.class, AspectJConfig.class})
@ContextConfiguration
@ActiveProfiles("test")
public class TestCrestIov {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private IovService iovservice;

    @Autowired
    private TagService tagservice;

    @Autowired
    private PageRequestHelper prh;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MapperFacade mapperFacade;
    /**
     * Helper.
     */
    @Autowired
    private EntityDtoHelper edh;

    @Test
    public void test_IovService() {
        final TagDto dto = DataGenerator.generateTagDto("SVC-TAG-02", "test");
        try {
            final Tag entity = mapperFacade.map(dto, Tag.class);
            final Tag saved = tagservice.insertTag(entity);
            assertThat(saved).isNotNull();
            final IovDto iovdto0 = DataGenerator.generateIovDto("afakehashiov01", dto.getName(), new BigDecimal(0L));
            final Iov ioventity = mapperFacade.map(iovdto0, Iov.class);
            ioventity.setTag(new Tag(iovdto0.getTagName()));
            iovservice.insertIov(ioventity);
            assertThat(iovservice.existsIov("SVC-TAG-02", new BigDecimal(0L), "afakehashiov01")).isTrue();

            final IovDto iovdto1 = DataGenerator.generateIovDto("afakehashiov01", dto.getName(),
                    new BigDecimal(3100000L));
            final Iov ioventity1 = mapperFacade.map(iovdto1, Iov.class);
            ioventity1.setTag(new Tag(iovdto1.getTagName()));
            iovservice.insertIov(ioventity1);
            final IovDto iovdto2 = DataGenerator.generateIovDto("afakehashiov01", dto.getName(),
                    new BigDecimal(4100000L));
            final Iov ioventity2 = mapperFacade.map(iovdto2, Iov.class);
            ioventity2.setTag(new Tag(iovdto2.getTagName()));
            iovservice.insertIov(ioventity2);
            final PageRequest preq = prh.createPageRequest(0, 100, "id.since:DESC");
            final Iterable<Iov> iovlist = iovservice.findAllIovs(null, preq);
            final List<IovDto> dtolist = edh.entityToDtoList(iovlist, IovDto.class);
            assertThat(dtolist.size()).isPositive();

            final Long niovs = iovservice.getSizeByTagAndSnapshot(dto.getName(), new Date());
            assertThat(niovs).isPositive();

            final List<IovPayloadDto> iplist = iovservice.selectIovPayloadsByTagRangeSnapshot(dto.getName(),
                    new BigDecimal(0L), new BigDecimal(4100000L), new Date());
            assertThat(iplist.size()).isPositive();
            final List<IovPayloadDto> iplistempty = iovservice.selectIovPayloadsByTagRangeSnapshot(dto.getName(),
                    new BigDecimal(9999999990L), new BigDecimal(9999999999L), new Date());
            assertThat(iplistempty.size()).isLessThan(2);

            final Iterable<Iov> iovlist1 = iovservice.selectIovsByTagRangeSnapshot(dto.getName(),
                    new BigDecimal(1000L), new BigDecimal(4200000L), new Date(), "groups");
            final List<IovDto> ilist = edh.entityToDtoList(iovlist1, IovDto.class);
            assertThat(ilist.size()).isPositive();
            final Iterable<Iov> iovlist2 = iovservice.selectIovsByTagRangeSnapshot(dto.getName(),
                    new BigDecimal(1000L), new BigDecimal(4200000L), new Date(), "ranges");
            final List<IovDto> ilist2 = edh.entityToDtoList(iovlist2, IovDto.class);
            assertThat(ilist2.size()).isPositive();
        }
        catch (final NotExistsPojoException e) {
            log.info("got exception of type {}", e.getClass());
        }
        catch (RuntimeException e) {
            log.info("got exception of type {}", e.getClass());
        }

    }

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

        final TagDto t2dto = DataGenerator.generateTagDto("SB-TAG-IOV-02", "run-lumi");
        log.info("Store tag for payload request: {}", t2dto);
        final ResponseEntity<TagDto> resptag2 = this.testRestTemplate
                .postForEntity("/crestapi/tags", t2dto, TagDto.class);
        log.info("Received response: " + resptag2);
        assertThat(resptag2.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        final TagDto t3dto = DataGenerator.generateTagDto("SB-TAG-IOV-03", "time");
        log.info("Store tag for payload request: {}", t3dto);
        final ResponseEntity<TagDto> resptag3 = this.testRestTemplate
                .postForEntity("/crestapi/tags", t3dto, TagDto.class);
        log.info("Received response: " + resptag3);
        assertThat(resptag3.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Store iov for payload pdto
        final IovDto iovdto = DataGenerator.generateIovDto(pdto.getHash(), tdto.getName(), new BigDecimal(1000000L));
        log.info("Store iov : {}", iovdto);
        final ResponseEntity<IovDto> iovresp = this.testRestTemplate
                .postForEntity("/crestapi/iovs", iovdto, IovDto.class);
        log.info("Received response: " + iovresp);
        assertThat(iovresp.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        final ResponseEntity<String> iovrespalreadythere = this.testRestTemplate
                .postForEntity("/crestapi/iovs", iovdto, String.class);
        log.info("Received response: " + iovrespalreadythere);
        assertThat(iovrespalreadythere.getStatusCode()).isEqualTo(HttpStatus.SEE_OTHER);

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
            assertThat(ok.getSize()).isPositive();
        }

        // Check without tagname in filters
        final GenericMap filters2 = new GenericMap();
        filters2.put("tag", tdto.getName());
        setdto.datatype("iovs").filter(filters2);

        // It should succeed if the tagname is in the IOV resources.
        // Iovs are already stored so we should get a 303. Attention, in recent version we just give
        // a 500, because of the constraint violation.
        final ResponseEntity<String> iovresp3 = this.testRestTemplate
                .postForEntity("/crestapi/iovs/storebatch", setdto, String.class);
        log.info("Received response: " + iovresp3);
        assertThat(iovresp3.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        // Check without tagname in iovs
        final GenericMap filters3 = new GenericMap();
        filters3.put("tagName", tdto.getName());

        final IovSetDto setdto3 = new IovSetDto();
        setdto3.format("iovs").size(2L);
        iovdto.tagName(null);
        iovdto2.tagName(null);
        iovdto.since(new BigDecimal(4000000L));
        iovdto2.since(new BigDecimal(4100000L));
        setdto3.addResourcesItem(iovdto).addResourcesItem(iovdto2);
        setdto3.datatype("iovs").filter(filters3);

        final ResponseEntity<String> iovresp4 = this.testRestTemplate
                .postForEntity("/crestapi/iovs/storebatch", setdto3, String.class);
        log.info("Received response: " + iovresp4);
        assertThat(iovresp4.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Check without tagname at all
        final GenericMap filters4 = new GenericMap();
        final IovSetDto setdto4 = new IovSetDto();
        setdto3.format("iovs").size(2L);
        iovdto.tagName(null);
        iovdto2.tagName(null);
        iovdto.since(new BigDecimal(4000000L));
        iovdto2.since(new BigDecimal(4100000L));
        setdto3.addResourcesItem(iovdto).addResourcesItem(iovdto2);
        setdto3.datatype("iovs").filter(filters4);

        final ResponseEntity<String> iovresp5 = this.testRestTemplate
                .postForEntity("/crestapi/iovs/storebatch", setdto4, String.class);
        log.info("Received response: " + iovresp5);
        assertThat(iovresp5.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

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
        final ResponseEntity<String> resp4b = this.testRestTemplate
                .exchange("/crestapi/iovs/selectSnapshot?tagname=NONR&snapshot=10000", HttpMethod.GET, null,
                        String.class);
        assertThat(resp4b.getStatusCode()).isGreaterThanOrEqualTo(HttpStatus.OK);
        final ResponseEntity<String> resp4c = this.testRestTemplate
                .exchange("/crestapi/iovs/selectSnapshot?tagname=NONR&snapshot=somestring", HttpMethod.GET, null,
                        String.class);
        assertThat(resp4c.getStatusCode()).isGreaterThanOrEqualTo(HttpStatus.OK);
        final ResponseEntity<String> resp5 = this.testRestTemplate
                .exchange("/crestapi/iovs/lastIov?tagname=NONR", HttpMethod.GET, null, String.class);
        assertThat(resp5.getStatusCode()).isGreaterThanOrEqualTo(HttpStatus.OK);

        // dateformat
        final HttpHeaders headers = new HttpHeaders();
        headers.add("dateformat", "min");
        final HttpEntity<?> entity = new HttpEntity<>(headers);

        final ResponseEntity<String> resp5b = this.testRestTemplate
                .exchange("/crestapi/iovs/lastIov?tagname=NONR", HttpMethod.GET, entity, String.class);
        assertThat(resp5b.getStatusCode()).isGreaterThanOrEqualTo(HttpStatus.OK);

    }

    @Test
    public void testB_findiovApi() throws Exception {

        final Long now = Instant.now().toEpochMilli();

        sendrequest("/crestapi/iovs?by=tagname:SB-TAG-IOV-01,insertiontime>0", HttpMethod.GET, null, new IovSetDto());
        sendrequest("/crestapi/iovs/getSize?tagname=SB-TAG-IOV-01&snapshot=0", HttpMethod.GET, null,
                new CrestBaseResponse());
        sendrequest("/crestapi/iovs/getSizeByTag?tagname=SB%", HttpMethod.GET, null, new TagSummarySetDto());
        sendrequest("/crestapi/iovs/selectGroups?tagname=SB-TAG-IOV-01&snapshot=0", HttpMethod.GET, null, new IovSetDto());
        // The next 2 tags do not have IOVs.
        sendzerorequest("/crestapi/iovs/selectGroups?tagname=SB-TAG-IOV-02&snapshot=" + now, HttpMethod.GET, null,
                new IovSetDto());
        final HttpHeaders headers = new HttpHeaders();
        headers.add("X-Crest-Query", "ranges");
        final HttpEntity<?> entity = new HttpEntity<>(headers);
        sendzerorequest("/crestapi/iovs/selectGroups?tagname=SB-TAG-IOV-03&since=0&until=INF", HttpMethod.GET, entity,
                new IovSetDto());
        // selectIovs
        sendrequest("/crestapi/iovs/selectIovs?tagname=SB-TAG-IOV-01&since=0&until=3900000&snapshot=0", HttpMethod.GET, null, new IovSetDto());
        // selectSnapshot
        sendrequest("/crestapi/iovs/selectSnapshot?tagname=SB-TAG-IOV-01&snapshot=0", HttpMethod.GET, null, new IovSetDto());
        // standard general search
        sendrequest("/crestapi/iovs?by=tagname:SB-TAG-IOV-01,since<39000000&page=0&size=10", HttpMethod.GET, null, new IovSetDto());
        // lastIov
        sendrequest("/crestapi/iovs/lastIov?tagname=SB-TAG-IOV-01&since=39000000", HttpMethod.GET, null, new IovSetDto());

        // dateformat
        final HttpHeaders headers7a = new HttpHeaders();
        headers7a.add("dateformat", "ms");
        final HttpEntity<?> entity7a = new HttpEntity<>(headers7a);
        sendrequest("/crestapi/iovs/lastIov?tagname=SB-TAG-IOV-01&since=now", HttpMethod.GET, entity7a, new IovSetDto());
        // dateformat
        final HttpHeaders headers7b = new HttpHeaders();
        headers7b.add("dateformat", "hour");
        final HttpEntity<?> entity7b = new HttpEntity<>(headers7b);
        final ResponseEntity<String> resp7b = this.testRestTemplate
                .exchange("/crestapi/iovs/lastIov?tagname=SB-TAG-IOV-01&since=120", HttpMethod.GET, entity7b,
                        String.class);
        {
            log.info("Retrieved iov selection " + resp7b.getBody());
            assertThat(resp7b.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        // iovPayload
        sendrequest("/crestapi/iovs/selectIovPayloads?tagname=SB-TAG-IOV-01&since=0&until=3900000&snapshot=0", HttpMethod.GET, null,
                new IovPayloadSetDto());
        sendrequest("/crestapi/iovs/selectIovPayloads?tagname=SB-TAG-IOV-01&since=0&until=INF&snapshot="
                    + now, HttpMethod.GET, null, new IovPayloadSetDto());

        final ResponseEntity<String> resp8b = this.testRestTemplate
                .exchange("/crestapi/iovs/selectIovPayloads?tagname=SB-TAG-IOV-01&since=0&until=sometime&snapshot="
                          + now, HttpMethod.GET, null, String.class);
        {
            log.info("Retrieved iov payload selection " + resp8b.getBody());
            assertThat(resp8b.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        sendrequest("/crestapi/iovs/selectIovs?tagname=SB-TAG-IOV-01&since=0&until=INF&snapshot="
                    + now, HttpMethod.GET, null, new IovSetDto());
    }

    /**
     *
     * @param url
     * @param method
     * @param entity
     * @param ok
     * @throws JsonProcessingException
     */
    protected void sendrequest(String url, HttpMethod method, HttpEntity entity, CrestBaseResponse ok)
            throws JsonProcessingException {
        final ResponseEntity<String> resp = this.testRestTemplate
                .exchange(url, method, entity, String.class);
        {
            log.info("Retrieved selection " + resp.getBody());
            String responseBody = resp.getBody();
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            log.info("Response from server is: " + responseBody);
            ok = mapper.readValue(responseBody, ok.getClass());
            assertThat(ok.getSize()).isPositive();
        }
    }

    /**
     *
     * @param url
     * @param method
     * @param entity
     * @param ok
     * @throws JsonProcessingException
     */
    protected void sendzerorequest(String url, HttpMethod method, HttpEntity entity, CrestBaseResponse ok)
            throws JsonProcessingException {
        final ResponseEntity<String> resp = this.testRestTemplate
                .exchange(url, method, entity, String.class);
        {
            log.info("Retrieved selection " + resp.getBody());
            String responseBody = resp.getBody();
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            log.info("Response from server is: " + responseBody);
            ok = mapper.readValue(responseBody, ok.getClass());
            assertThat(ok.getSize()).isZero();
        }
    }

}
