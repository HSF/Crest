/**
 *
 */
package hep.crest.server.utils;

import hep.crest.server.data.pojo.CrestFolders;
import hep.crest.server.data.pojo.GlobalTag;
import hep.crest.server.data.pojo.GlobalTagMap;
import hep.crest.server.data.pojo.GlobalTagMapId;
import hep.crest.server.data.pojo.Iov;
import hep.crest.server.data.pojo.IovId;
import hep.crest.server.data.pojo.Payload;
import hep.crest.server.data.pojo.Tag;
import hep.crest.server.data.runinfo.pojo.RunLumiId;
import hep.crest.server.data.runinfo.pojo.RunLumiInfo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Date;

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
        entity.setDescription("A test global tag " + name);
        entity.setRelease("rel-1");
        entity.setScenario("test");
        entity.setType('T');
        entity.setWorkflow("none");
        entity.setValidity(new BigDecimal(0L));
        entity.setSnapshotTime(snapshotTime);
        return entity;
    }

    public static Tag generateTag(String name, String ttype) {
        final Tag entity = new Tag();
        entity.setName(name).setDescription("A test tag " + name)
                .setEndOfValidity(BigInteger.valueOf(-1L))
                .setLastValidatedTime(BigInteger.valueOf(-1L))
                .setObjectType("type")
                .setSynchronization("synchro")
                .setTimeType(ttype);
        return entity;
    }

    public static GlobalTagMap generateMapping(GlobalTag gt, Tag at, GlobalTagMapId id) {
        final GlobalTagMap entity = new GlobalTagMap();
        entity.setId(id).setGlobalTag(gt).setTag(at);
        return entity;
    }

    public static Payload generatePayload(String hash, String objtype) {
        final Payload entity = new Payload();
        entity.setHash(hash);
        entity.setObjectType(objtype);
        entity.setVersion("v1");
        return entity;
    }

    public static Iov generateIov(String hash, String tagname, BigInteger since) {
        final IovId id =
                new IovId().setTagName(tagname).setSince(since).setInsertionTime(new Date());
        final Tag tag = new Tag().setName(tagname);
        final Iov entity = new Iov().setId(id).setTag(tag).setPayloadHash(hash);
        return entity;
    }

    public static RunLumiInfo generateRunLumiInfo(BigInteger run, BigInteger lb) {
        final RunLumiInfo entity = new RunLumiInfo();
        RunLumiId id = new RunLumiId();
        id.setRunNumber(run);
        id.setLb(lb);
        entity.setEndtime(BigInteger.valueOf(99L));
        entity.setStarttime(BigInteger.valueOf(1L));
        entity.setId(id);
        return entity;
    }


    public static CrestFolders generateFolder(String name, String fullpath, String schema) {
        final CrestFolders entity = new CrestFolders();
        entity.setSchemaName(schema);
        entity.setNodeFullpath(fullpath);
        entity.setNodeName(name);
        entity.setTagPattern(name + "-MY-TEST");
        entity.setNodeDescription("Some node");
        entity.setGroupRole("TEST");
        return entity;
    }

    public static void generatePayloadData(String filename, String content) {
        try {
            final File file = new File(filename);
            final FileWriter fileWriter = new FileWriter(file);
            fileWriter.write("This is ");
            fileWriter.write("a test");
            fileWriter.write(content);
            fileWriter.flush();
            fileWriter.close();
        }
        catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
