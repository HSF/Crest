/**
 * 
 */
package hep.crest.data.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import hep.crest.data.pojo.GlobalTag;
import hep.crest.data.pojo.GlobalTagMap;
import hep.crest.data.pojo.GlobalTagMapId;
import hep.crest.data.pojo.Iov;
import hep.crest.data.pojo.IovId;
import hep.crest.data.pojo.Payload;
import hep.crest.data.pojo.Tag;
import hep.crest.data.runinfo.pojo.RunLumiInfo;
import hep.crest.data.security.pojo.CrestFolders;
import hep.crest.data.security.pojo.CrestRoles;
import hep.crest.data.security.pojo.CrestUser;
import hep.crest.data.test.tools.DataGenerator;
import hep.crest.swagger.model.FolderDto;
import hep.crest.swagger.model.FolderSetDto;
import hep.crest.swagger.model.GenericMap;
import hep.crest.swagger.model.GlobalTagDto;
import hep.crest.swagger.model.GlobalTagMapDto;
import hep.crest.swagger.model.GlobalTagMapSetDto;
import hep.crest.swagger.model.GlobalTagSetDto;
import hep.crest.swagger.model.GroupDto;
import hep.crest.swagger.model.HTTPResponse;
import hep.crest.swagger.model.IovDto;
import hep.crest.swagger.model.IovPayloadDto;
import hep.crest.swagger.model.IovPayloadSetDto;
import hep.crest.swagger.model.IovSetDto;
import hep.crest.swagger.model.PayloadDto;
import hep.crest.swagger.model.PayloadSetDto;
import hep.crest.swagger.model.PayloadTagInfoDto;
import hep.crest.swagger.model.RunLumiInfoDto;
import hep.crest.swagger.model.RunLumiSetDto;
import hep.crest.swagger.model.TagDto;
import hep.crest.swagger.model.TagMetaDto;
import hep.crest.swagger.model.TagMetaSetDto;
import hep.crest.swagger.model.TagSetDto;
import hep.crest.swagger.model.TagSummaryDto;
import hep.crest.swagger.model.TagSummarySetDto;
import ma.glasnost.orika.MapperFacade;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author formica
 *
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class PojoDtoConverterTests {

    @Autowired
    @Qualifier("mapper")
    private MapperFacade mapper;

    private static final Logger log = LoggerFactory.getLogger(PojoDtoConverterTests.class);

    @Test
    public void testGlobalTagConverter() throws Exception {
        final GlobalTag entity = DataGenerator.generateGlobalTag("GT-02");
        final GlobalTagDto dto = mapper.map(entity, GlobalTagDto.class);
        assertThat(entity.getName()).isEqualTo(dto.getName());
        assertThat(entity.toString().length()).isPositive();
        assertThat(dto.toString().length()).isPositive();

        final Instant now = Instant.now();
        final Date it = new Date(now.toEpochMilli());

        final GlobalTagDto dto1 = DataGenerator.generateGlobalTagDto("GT-02", it);
        assertThat(dto1.getDescription()).isEqualTo(dto.getDescription());
        assertThat(dto1).isNotEqualTo(dto); // Should be true
        assertThat(dto1.hashCode()).isNotZero();
    }

    @Test
    public void testTagConverter() throws Exception {
        final Tag entity = DataGenerator.generateTag("MT-02", "run");
        final TagDto dto = mapper.map(entity, TagDto.class);
        assertThat(entity.getName()).isEqualTo(dto.getName());
        assertThat(entity.toString().length()).isPositive();
        assertThat(dto.toString().length()).isPositive();
        assertThat(dto.hashCode()).isNotZero();

        final Tag entity1 = DataGenerator.generateTag("MT-02", "run");
        final TagDto dto1 = mapper.map(entity1, TagDto.class);
        assertThat(dto1).isNotNull().isEqualTo(dto);
    }

    @Test
    public void testMapsConverter() throws Exception {
        final GlobalTag gtag = DataGenerator.generateGlobalTag("MY-TEST-GT-03");
        final Tag tag1 = DataGenerator.generateTag("MY-TEST-02", "time");
        final GlobalTagMapId id1 = new GlobalTagMapId();
        id1.setGlobalTagName(gtag.getName());
        id1.setLabel("MY-TEST");
        id1.setRecord("aaa");

        final GlobalTagMap map1 = DataGenerator.generateMapping(gtag, tag1, id1);
        final GlobalTagMapDto dto = mapper.map(map1, GlobalTagMapDto.class);
        assertThat(dto.toString().length()).isPositive();
        assertThat(dto.getGlobalTagName()).isEqualTo(gtag.getName());

        final GlobalTagMapId id2 = new GlobalTagMapId(gtag.getName(), "aaa", "MY-TEST");
        assertThat(id2).isEqualTo(id1);
        assertThat(id2.hashCode()).isNotZero();

    }

    @Test
    public void testIovConverter() throws Exception {
        final IovDto dto = DataGenerator.generateIovDto("MYHASH", "MT-02", new BigDecimal(1000L));
        final Iov entity = mapper.map(dto, Iov.class);
        final IovId id = entity.getId();
        assertThat(id.getSince()).isEqualTo(new BigDecimal(1000L));
        log.info("Id of iov is {}", id);

        final Iov geniov = DataGenerator.generateIov("MYHASH", "MT-02", new BigDecimal(1000L));
        log.info("Generated iov {}", geniov);
        final IovId genid = geniov.getId();
        assertThat(genid).isNotNull();

        assertThat(entity.getPayloadHash()).isEqualTo(dto.getPayloadHash());
        assertThat(entity.toString().length()).isPositive();
        assertThat(dto.toString().length()).isPositive();
        assertThat(dto.hashCode()).isNotZero();
        assertThat(entity.hashCode()).isNotZero();
    }

    @Test
    public void testPayloadConverter() throws Exception {
        final Instant now = Instant.now();
        final Date time = new Date(now.toEpochMilli());

        final PayloadDto dto = DataGenerator.generatePayloadDto("myhash1", "mydata", "mystreamer",
                "test", time);
        final Payload entity = DataGenerator.generatePayload("myhash1", "test");
        assertThat(entity.getHash()).isEqualTo(dto.getHash());
        assertThat(entity.toString().length()).isPositive();
        assertThat(dto.toString().length()).isPositive();
        assertThat(dto.hashCode()).isNotZero();
        assertThat(entity.hashCode()).isNotZero();
    }

    @Test
    public void testGlobalTagSetsConverter() throws Exception {
        final Instant now = Instant.now();
        final Date it = new Date(now.toEpochMilli());

        final GlobalTagDto dto1 = DataGenerator.generateGlobalTagDto("MY-GTAG-01", it);
        final GlobalTagDto dto2 = DataGenerator.generateGlobalTagDto("MY-GTAG-02", it);
        final GlobalTagDto dto1bis = DataGenerator.generateGlobalTagDto("MY-GTAG-01", it);
        log.info("compare {} with {}", dto1, dto1bis);
        assertThat(dto1).isEqualTo(dto1bis);
        final GlobalTagSetDto setdto = new GlobalTagSetDto();
        setdto.datatype("globaltags");
        setdto.format("JSON");
        setdto.addResourcesItem(dto1).addResourcesItem(dto2);
        assertThat(setdto.toString().length()).isPositive();
        assertThat(setdto.hashCode()).isNotZero();
        final List<GlobalTagDto> resources = setdto.getResources();
        for (final GlobalTagDto gtDto : resources) {
            if (gtDto.getName().equals("MY-GTAG-01")) {
                assertThat(gtDto).isEqualTo(dto1);
            }
        }
        final GlobalTagSetDto setdto2 = new GlobalTagSetDto();
        setdto2.datatype("globaltags");
        setdto2.format("JSON");
        setdto2.setResources(resources);
        assertThat(setdto2).isEqualTo(setdto);
    }

    @Test
    public void testGlobalTagMapSetsConverter() throws Exception {
        final GlobalTagMapDto dto1 = DataGenerator.generateMappingDto("MY-GTAG-01", "T-01", "T",
                "a");
        final GlobalTagMapDto dto2 = DataGenerator.generateMappingDto("MY-GTAG-01", "S-02", "S",
                "b");
        assertThat(dto1.hashCode()).isNotZero();
        final GlobalTagMapDto dto1bis = DataGenerator.generateMappingDto("MY-GTAG-01", "T-01", "T",
                "a");
        assertThat(dto1).isEqualTo(dto1bis);

        final GlobalTagMapSetDto setdto = new GlobalTagMapSetDto();
        setdto.datatype("maps");
        setdto.format("JSON");
        setdto.addResourcesItem(dto1).addResourcesItem(dto2);
        assertThat(setdto.toString().length()).isPositive();
        assertThat(setdto.hashCode()).isNotZero();
        final List<GlobalTagMapDto> resources = setdto.getResources();
        for (final GlobalTagMapDto gtmapDto : resources) {
            if (gtmapDto.getGlobalTagName().equals("MY-GTAG-01")) {
                assertThat(gtmapDto).isEqualTo(dto1);
            }
        }
        final GlobalTagMapSetDto setdto2 = new GlobalTagMapSetDto();
        setdto2.datatype("maps");
        setdto2.format("JSON");
        setdto2.setResources(resources);
        assertThat(setdto2).isEqualTo(setdto);
    }

    @Test
    public void testIovSetsConverter() throws Exception {
        final IovDto dto1 = DataGenerator.generateIovDto("MYHASH1", "MT-02", new BigDecimal(1000L));
        final IovDto dto2 = DataGenerator.generateIovDto("MYHASH2", "MT-02", new BigDecimal(2000L));
        final IovSetDto setdto = new IovSetDto();
        setdto.datatype("iovs");
        setdto.format("JSON");
        setdto.addResourcesItem(dto1).addResourcesItem(dto2);
        assertThat(setdto.toString().length()).isPositive();
        assertThat(setdto.hashCode()).isNotZero();
        final List<IovDto> resources = setdto.getResources();
        for (final IovDto iovDto : resources) {
            if (iovDto.getPayloadHash().equals("MYHASH1")) {
                assertThat(iovDto).isEqualTo(dto1);
            }
        }
        final IovSetDto setdto2 = new IovSetDto();
        setdto2.datatype("iovs");
        setdto2.format("JSON");
        setdto2.setResources(resources);
        assertThat(setdto2).isEqualTo(setdto);
    }

    @Test
    public void testTagSetsConverter() throws Exception {
        final TagDto dto1 = DataGenerator.generateTagDto("MY-TAG-01", "time");
        final TagDto dto2 = DataGenerator.generateTagDto("MY-TAG-02", "time");
        final TagSetDto setdto = new TagSetDto();
        setdto.datatype("tags");
        setdto.format("JSON");
        setdto.addResourcesItem(dto1).addResourcesItem(dto2);
        assertThat(setdto.toString().length()).isPositive();
        assertThat(setdto.hashCode()).isNotZero();

        final List<TagDto> resources = setdto.getResources();
        for (final TagDto tagDto : resources) {
            if (tagDto.getName().equals("MY-TAG-01")) {
                assertThat(tagDto).isEqualTo(dto1);
            }
        }
        final TagSetDto setdto2 = new TagSetDto();
        setdto2.datatype("tags");
        setdto2.format("JSON");
        setdto2.setResources(resources);
        assertThat(setdto2).isEqualTo(setdto);
    }

    @Test
    public void testTagSummarySetsConverter() throws Exception {
        final TagSummaryDto dto1 = DataGenerator.generateTagSummaryDto("MY-TAG-01", 10L);
        final TagSummaryDto dto2 = DataGenerator.generateTagSummaryDto("MY-TAG-02", 20L);

        final TagSummarySetDto setdto = new TagSummarySetDto();
        setdto.datatype("tags");
        setdto.format("JSON");
        setdto.addResourcesItem(dto1).addResourcesItem(dto2);
        assertThat(setdto.toString().length()).isPositive();
        assertThat(setdto.hashCode()).isNotZero();
    }

    @Test
    public void testRunInfoConverter() throws Exception {
        final Date start = new Date();
        final Date end = new Date(start.getTime()+3600000);
        final RunLumiInfoDto dto1 = DataGenerator.generateRunLumiInfoDto(new BigDecimal(start.getTime()), new BigDecimal(end.getTime()), new BigDecimal(100L));

        assertThat(dto1.toString().length()).isPositive();
        assertThat(dto1.hashCode()).isNotZero();
        final RunLumiInfo entity = mapper.map(dto1, RunLumiInfo.class);
        assertThat(dto1.getRunNumber()).isEqualTo(entity.getRunNumber());
        assertThat(entity.toString().length()).isPositive();
        assertThat(entity.hashCode()).isNotZero();
        
        final RunLumiSetDto setdto = new RunLumiSetDto();
        setdto.datatype("RunLumiSetDto");
        final GenericMap filterm = new GenericMap();
        filterm.put("run", "1000");
        assertThat(filterm.containsKey("run")).isTrue();
        
        setdto.filter(filterm);
        setdto.addResourcesItem(dto1);
        setdto.format("RunInfo");
        
        final RunLumiSetDto setdto2 = new RunLumiSetDto();
        setdto2.datatype("RunLumiSetDto");
        setdto2.filter(filterm);
        setdto2.format("RunInfo");
        setdto2.addResourcesItem(dto1);
        assertThat(setdto2).isEqualTo(setdto);
        assertThat(setdto.toString()).isNotNull();
        setdto2.filter(setdto.getFilter());
        setdto2.format(setdto.getFormat());
    }

    @Test
    public void testRunInfoSetConverter() throws Exception {
        final RunLumiInfoDto dto1 = DataGenerator.generateRunLumiInfoDto(new BigDecimal(2000L),
                new BigDecimal(33333L), new BigDecimal(200L));

        assertThat(dto1.toString().length()).isPositive();
        assertThat(dto1.hashCode()).isNotZero();
        final RunLumiSetDto setdto = new RunLumiSetDto();
        setdto.datatype("runs").format("json");
        setdto.addResourcesItem(dto1);
        setdto.size(1L);
        assertThat(setdto.toString().length()).isPositive();

        final RunLumiSetDto setdto1 = new RunLumiSetDto();
        setdto1.datatype("runs").format("json");
        setdto1.addResourcesItem(dto1);
        setdto1.size(1L);

        assertThat(setdto).isEqualTo(setdto1);
        assertThat(setdto.hashCode()).isNotZero();

    }

    @Test
    public void testFolderConverter() throws Exception {
        final FolderDto dto1 = DataGenerator.generateFolderDto("T0BLOB", "/MDT/T0BLOB",
                "COOLOFL_MDT");

        assertThat(dto1.toString().length()).isPositive();
        assertThat(dto1.hashCode()).isNotZero();
        final CrestFolders entity = mapper.map(dto1, CrestFolders.class);
        assertThat(dto1.getNodeFullpath()).isEqualTo(entity.getNodeFullpath());
        assertThat(entity.toString().length()).isPositive();
        assertThat(entity.hashCode()).isNotZero();
        
        dto1.setGroupRole("somerole");
        dto1.setNodeDescription("some node desc");
        dto1.setSchemaName("some_schema");
        dto1.setTagPattern("some_anode");
        dto1.setNodeName("anode");
        dto1.setNodeFullpath("/some/anode");
  
        final FolderDto dto2 = DataGenerator.generateFolderDto("T0BLOB", "/MDT/T0BLOB",
                "COOLOFL_MDT");
        assertThat(dto1).isNotEqualTo(dto2);
        
    }

    @Test
    public void testFolderSetConverter() throws Exception {
        final FolderDto dto1 = DataGenerator.generateFolderDto("T0BLOB", "/MDT/T0BLOB",
                "COOLOFL_MDT");

        assertThat(dto1.toString().length()).isPositive();
        assertThat(dto1.hashCode()).isNotZero();
        final FolderSetDto setdto = new FolderSetDto();
        setdto.datatype("folders").format("json");
        setdto.addResourcesItem(dto1);
        setdto.size(1L);
        assertThat(setdto.toString().length()).isPositive();

        final FolderSetDto setdto1 = new FolderSetDto();
        setdto1.datatype("folders").format("json");
        setdto1.addResourcesItem(dto1);
        setdto1.size(1L);

        assertThat(setdto).isEqualTo(setdto1);
        assertThat(setdto.hashCode()).isNotZero();
    }

    @Test
    public void testPayloadDtoSetsConverter() throws Exception {
        final Instant now = Instant.now();
        final Date time = new Date(now.toEpochMilli());
        final String data = "datastr";
        final String sinfo = "streaminfo";
        final PayloadDto dto1 = DataGenerator.generatePayloadDto("somehash", data, sinfo, "test",
                time);
        final PayloadDto dto1bis = DataGenerator.generatePayloadDto("somehash", data, sinfo, "test",
                time);
        log.info("compare {} with {} having hash {} and {}", dto1, dto1bis, dto1.hashCode(),
                dto1bis.hashCode());
        assertThat(dto1.getHash()).isEqualTo(dto1bis.getHash());
        final PayloadSetDto setdto = new PayloadSetDto();
        setdto.datatype("payloads");
        setdto.format("JSON");
        setdto.addResourcesItem(dto1);
        assertThat(setdto.toString().length()).isPositive();
        assertThat(setdto.hashCode()).isNotZero();
        final List<PayloadDto> resources = setdto.getResources();
        for (final PayloadDto gtDto : resources) {
            if (gtDto.getHash().equals("somehash")) {
                assertThat(gtDto).isEqualTo(dto1);
            }
        }
    }

    @Test
    public void testIovPayloadSetsConverter() throws Exception {
        final Instant now = Instant.now();
        final Date time = new Date(now.toEpochMilli());
        final String data = "datastr";
        final String sinfo = "streaminfo";
        final IovDto dto1 = DataGenerator.generateIovDto("MYHASH3", "MT-02", new BigDecimal(3000L));
        final IovDto dto2 = DataGenerator.generateIovDto("MYHASH4", "MT-02", new BigDecimal(4000L));
        final PayloadDto pdto1 = DataGenerator.generatePayloadDto("MYHASH3", data, sinfo, "test",
                time);
        final PayloadDto pdto2 = DataGenerator.generatePayloadDto("MYHASH4", data, sinfo, "test",
                time);

        final IovPayloadDto ipdto1 = new IovPayloadDto().objectType(pdto1.getObjectType())
                .payloadHash(dto1.getPayloadHash()).since(dto1.getSince()).size(pdto1.getSize())
                .version(pdto1.getVersion());
        final IovPayloadDto ipdto2 = new IovPayloadDto().objectType(pdto2.getObjectType())
                .payloadHash(dto2.getPayloadHash()).since(dto2.getSince()).size(pdto2.getSize())
                .version(pdto2.getVersion());

        final IovPayloadSetDto psetdto = new IovPayloadSetDto();
        psetdto.addResourcesItem(ipdto1).addResourcesItem(ipdto2);
        psetdto.datatype("IovPayloadSetDto");
        psetdto.format("iovpayloaddto");
        
        assertThat(psetdto.getResources()).isNotNull();
        assertThat(ipdto1).isNotEqualTo(ipdto2);
        
        final List<IovPayloadDto> plist = new ArrayList<>();
        plist.add(ipdto1);
        plist.add(ipdto2);
        final IovPayloadSetDto psetdto1 = new IovPayloadSetDto();
        psetdto1.resources(plist);
        psetdto1.setDatatype("IovPayloadSetDto");
        psetdto1.setFormat("iovpayloaddto");
        
        assertThat(psetdto).isEqualTo(psetdto1);
        assertThat(psetdto.toString().length()).isPositive();
    }

    @Test
    public void testTagMetaDtoSetsConverter() throws Exception {
        final Instant now = Instant.now();
        final Date time = new Date(now.toEpochMilli());
        final String data = "{ \"key\" : \"value\" }";
        final TagMetaDto dto1 = DataGenerator.generateTagMetaDto("A_TAG", data, time);
        final TagMetaDto dto1bis = DataGenerator.generateTagMetaDto("A_TAG", data, time);
        log.info("compare {} with {} having hash code {} and {}", dto1, dto1bis, dto1.hashCode(),
                dto1bis.hashCode());
        assertThat(dto1.getTagName().equals(dto1bis.getTagName())).isTrue();
        final TagMetaSetDto setdto = new TagMetaSetDto();
        setdto.datatype("TagMetaSetDto");
        setdto.format("tagmetas");
        setdto.addResourcesItem(dto1);
        assertThat(setdto.toString().length()).isPositive();
        assertThat(setdto.hashCode()).isNotZero();
        final List<TagMetaDto> resources = setdto.getResources();
        for (final TagMetaDto gtDto : resources) {
            if (gtDto.getTagName().equals("A_TAG")) {
                assertThat(gtDto).isEqualTo(dto1);
            }
        }
    }

    @Test
    public void testOtherDtos() throws Exception {
        final List<BigDecimal> groups = new ArrayList<>();
        groups.add(new BigDecimal(10L));
        groups.add(new BigDecimal(100L));
        final GroupDto dto = new GroupDto();
        dto.groups(groups);
        assertThat(dto.getGroups().size()).isPositive();

        final HTTPResponse resp = new HTTPResponse();
        resp.action("test");
        resp.code(200);
        resp.message("a successful test");
        resp.id("ahash");
        assertThat(resp.toString().length()).isPositive();

        final PayloadTagInfoDto ptdto = new PayloadTagInfoDto();
        ptdto.avgvolume(1.0F);
        ptdto.niovs(10);
        ptdto.tagname("A-TAG");
        ptdto.totvolume(1.2F);
        assertThat(ptdto.toString().length()).isPositive();
        ptdto.setAvgvolume(1.1F);
        ptdto.setNiovs(11);
        ptdto.setTagname("A-TAG-01");
        ptdto.setTotvolume(1.3F);
        assertThat(ptdto.toString().length()).isPositive();
        assertThat(ptdto.hashCode()).isNotZero();
        
        final PayloadTagInfoDto ptdto2 = new PayloadTagInfoDto();
        ptdto2.avgvolume(ptdto.getAvgvolume());
        ptdto2.tagname(ptdto.getTagname());
        ptdto2.totvolume(ptdto.getTotvolume());
        ptdto2.niovs(ptdto.getNiovs());
        assertThat(ptdto2).isEqualTo(ptdto);
        
        final CrestUser user = new CrestUser("user", "password");
        user.setId("someid");
        user.setUsername("anothername");
        assertThat(user.toString().length()).isPositive();
        user.setPassword("anewpass");
        assertThat(user.getId()).isEqualTo("someid");
        assertThat(user.getUsername()).isEqualTo("anothername");
        assertThat(user.getPassword()).isEqualTo("anewpass");
        final CrestUser usr1 = new CrestUser();
        assertThat(usr1).isNotNull();
        
        final CrestRoles role = new CrestRoles("roleid", "admin");
        role.setRole("guest");
        assertThat(role.toString().length()).isPositive();
        role.setId("anotherroleid");
        assertThat(role.getId()).isEqualTo("anotherroleid");
        assertThat(role.getRole()).isEqualTo("guest");
        final CrestRoles rol1 = new CrestRoles();
        assertThat(rol1).isNotNull();
     }

    @Test
    public void testDeserializer() {
        final ObjectMapper locmapper = new ObjectMapper();
//        final SimpleModule module = new SimpleModule();
//
//        module.addDeserializer(Timestamp.class, new TimestampDeserializer());
//        module.addDeserializer(byte[].class, new ByteArrayDeserializer());
        
        final String json = "{ \"data\" : \"VGhpcyBpcyBhIG5vcm1hbCB0ZXh0Cg==\", " +
                "\"instime\" : \"2011-12-03T10:15:30+01:00\", " +
                "\"insdate\" : \"2020-12-03T22:15:30+01:00\", " +
                "\"name\" : \"MyTest\"}";
        
        try {
            log.info("Try to deserialize json {}", json);
            final TestItem m = locmapper.readValue(json, TestItem.class);
            assertThat(m.getName()).isEqualTo("MyTest");
            
            final String jsonout = locmapper.writeValueAsString(m);
            log.info("Serialized object is {}", jsonout);
            assertThat(jsonout).contains("MyTest");
        }
        catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
