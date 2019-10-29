/**
 * 
 */
package hep.crest.data.test.tools;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

import hep.crest.data.pojo.GlobalTag;
import hep.crest.data.pojo.GlobalTagMap;
import hep.crest.data.pojo.GlobalTagMapId;
import hep.crest.data.pojo.Payload;
import hep.crest.data.pojo.Tag;
import hep.crest.swagger.model.FolderDto;
import hep.crest.swagger.model.GlobalTagDto;
import hep.crest.swagger.model.GlobalTagMapDto;
import hep.crest.swagger.model.IovDto;
import hep.crest.swagger.model.PayloadDto;
import hep.crest.swagger.model.RunLumiInfoDto;
import hep.crest.swagger.model.TagDto;
import hep.crest.swagger.model.TagSummaryDto;

/**
 * @author formica
 *
 */
public class DataGenerator {

    public static GlobalTag generateGlobalTag(String name) {
        final Instant now = Instant.now();
        final Date snapshotTime = new Date(now.toEpochMilli());
        final GlobalTag entity = new GlobalTag();
        entity.setName(name);
        entity.setDescription("A test global tag "+name);
        entity.setRelease("rel-1");
        entity.setScenario("test");
        entity.setType('T');
        entity.setWorkflow("none");
        entity.setValidity(new BigDecimal(0L));
        entity.setSnapshotTime(snapshotTime);
        return entity;
    }
    
    public static GlobalTagDto generateGlobalTagDto(String name) {
        final Instant now = Instant.now();
        final Date snapshotTime = new Date(now.toEpochMilli());
        final GlobalTagDto entity = new GlobalTagDto();
        entity.name(name);
        entity.description("A test global tag "+name);
        entity.release("rel-1");
        entity.scenario("test");
        entity.type("T");
        entity.workflow("none");
        entity.validity(new BigDecimal(0L));
        entity.snapshotTime(snapshotTime);
        return entity;
    }

    public static Tag generateTag(String name, String ttype) {
        final Tag entity = new Tag();
        entity.setName(name);
        entity.setDescription("A test tag "+name);
        entity.setEndOfValidity(new BigDecimal(-1L));
        entity.setLastValidatedTime(new BigDecimal(-1L));
        entity.setObjectType("type");
        entity.setSynchronization("synchro");
        entity.setTimeType(ttype);
        return entity;
    }
    
    public static TagDto generateTagDto(String name, String ttype) {
        final TagDto entity = new TagDto();
        entity.name(name);
        entity.description("A test tag "+name);
        entity.endOfValidity(new BigDecimal(-1L));
        entity.lastValidatedTime(new BigDecimal(-1L));
        entity.synchronization("synchro");
        entity.timeType(ttype);
        return entity;
    }

    public static GlobalTagMap generateMapping(GlobalTag gt, Tag at, GlobalTagMapId id) {
        final GlobalTagMap entity = new GlobalTagMap();
        entity.setId(id);
        entity.setGlobalTag(gt);
        entity.setTag(at);
        return entity;
    }
    
    public static GlobalTagMapDto generateMappingDto(String tagName, String gtagName, String record, String label) {
        final GlobalTagMapDto entity = new GlobalTagMapDto();
        entity.tagName(tagName);
        entity.globalTagName(gtagName);
        entity.record(record);
        entity.label(label);
        return entity;
    }

    public static Payload generatePayload(String hash, String objtype) {
        final Payload entity = new Payload();
        entity.setHash(hash);
        entity.setObjectType(objtype);
        entity.setVersion("v1");
        return entity;
    }

    public static IovDto generateIovDto(String hash, String tagname, BigDecimal since) {
        final IovDto dto = new IovDto();
        dto.payloadHash(hash).tagName(tagname).since(since);
        return dto;
    }

    public static PayloadDto generatePayloadDto(String hash, String payloaddata, String stinfo, String objtype) {
        final PayloadDto dto = new PayloadDto();
        final byte[] bindata = new String(payloaddata).getBytes();
        final byte[] binstinfo = new String(stinfo).getBytes();
        dto.insertionTime(new Date()).data(bindata).hash(hash).objectType(objtype)
                .streamerInfo(binstinfo).version("v1");
        return dto;
    }

    public static RunLumiInfoDto generateRunLumiInfoDto(BigDecimal since, BigDecimal run, BigDecimal lb) {
        final RunLumiInfoDto dto = new RunLumiInfoDto();
        dto.since(since).lb(lb).run(run);
        dto.starttime(new BigDecimal(0L)).endtime(new BigDecimal(99L));
        return dto;
    }
    
    public static TagSummaryDto generateTagSummaryDto(String name, Long niovs) {
        final TagSummaryDto dto = new TagSummaryDto();
        dto.tagname(name).niovs(niovs);
        return dto;
    }

    public static FolderDto generateFolderDto(String name, String fullpath, String schema) {
        final FolderDto dto = new FolderDto();
        dto.schemaName(schema);
        dto.nodeFullpath(fullpath);
        dto.nodeName(name);
        dto.tagPattern("MY-TEST");
        dto.nodeDescription("Some node");
        dto.groupRole("TEST");
        return dto;
    }

}
