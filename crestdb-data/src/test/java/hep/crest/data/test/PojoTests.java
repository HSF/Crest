/**
 * 
 */
package hep.crest.data.test;

import hep.crest.data.pojo.GlobalTag;
import hep.crest.data.pojo.GlobalTagMap;
import hep.crest.data.pojo.GlobalTagMapId;
import hep.crest.data.pojo.IovId;
import hep.crest.data.pojo.Payload;
import hep.crest.data.pojo.Tag;
import hep.crest.data.pojo.TagMeta;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author formica
 *
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class PojoTests {

    private static final Logger log = LoggerFactory.getLogger(PojoTests.class);

    @Test
    public void testGlobalTagMap() throws Exception {
        GlobalTagMapId mapid = new GlobalTagMapId();
        mapid.setRecord("somerecord");
        mapid.setLabel("somelabel");
        mapid.setGlobalTagName("TGT-01");

        GlobalTagMapId mapid2 = new GlobalTagMapId();
        mapid2.setRecord("somerecord");
        mapid2.setLabel("somelabel");
        mapid2.setGlobalTagName("TGT-02");

        GlobalTagMapId mapid3 = new GlobalTagMapId();
        mapid3.setRecord("somerecord");
        mapid3.setLabel("somelabel2");
        mapid3.setGlobalTagName("TGT-01");

        assertThat(mapid).isNotEqualTo(mapid2).isNotEqualTo(mapid3);
        assertThat(mapid.hashCode()).isNotZero();

        mapid3.setRecord("somerecord2");
        mapid3.setLabel("somelabel");
        mapid3.setGlobalTagName("TGT-01");
        assertThat(mapid).isNotEqualTo(mapid3);
        assertThat(mapid3).isNotNull();

        Tag tag = new Tag("TAG-01");
        GlobalTag gtag = new GlobalTag("GT-01");
        GlobalTagMap map = new GlobalTagMap(mapid, tag, gtag);
        assertThat(map.getGlobalTag().getName()).isEqualTo("GT-01");

        assertThat(map).isNotNull();
    }

    @Test
    public void testIovId() throws Exception {
        IovId iovid = new IovId();
        Long now = Instant.now().toEpochMilli();
        iovid.setSince(new BigDecimal(now));
        iovid.setTagName("TEST-TAG-01");
        Date instime = iovid.getInsertionTime();
        iovid.setInsertionTime(new Date(now));
        IovId iovid1 = new IovId();
        iovid1.setSince(new BigDecimal(now));
        iovid1.setTagName("TEST-TAG-01");
        Date instime1 = iovid1.getInsertionTime();
        IovId iovid2 = new IovId();
        iovid2.setSince(new BigDecimal(now));
        iovid2.setTagName(null);

        assertThat(iovid.hashCode()).isNotZero();
        assertThat(iovid2).isNotNull().isNotEqualTo(iovid).isNotEqualTo(iovid1);
    }

    @Test
    public void testPayload() throws Exception {
        Payload pyld = new Payload();
        Long now = Instant.now().toEpochMilli();
        pyld.setSize(100);
        pyld.setHash("somehash");
        pyld.setInsertionTime(new Date(now));
        pyld.setObjectType("sometype");
        pyld.setVersion("someversion");
        assertThat(pyld.getSize()).isPositive();
        assertThat(pyld.getVersion().length()).isPositive();
        assertThat(pyld.getObjectType().length()).isPositive();

        assertThat(pyld.hashCode()).isNotZero();

        Payload pyld1 = new Payload("somehash","anotherobj", null, null, pyld.getInsertionTime());
        assertThat(pyld1).isNotNull();
    }

    @Test
    public void testTagMeta() throws Exception {
        TagMeta tagmeta = new TagMeta();
        Long now = Instant.now().toEpochMilli();
        tagmeta.setChansize(10);
        tagmeta.setTagName("PIPPO");
        tagmeta.setInsertionTime(new Date(now));
        tagmeta.setDescription("a test");
        tagmeta.setColsize(2);
        assertThat(tagmeta.getColsize()).isPositive();
        assertThat(tagmeta.getDescription().length()).isPositive();
        assertThat(tagmeta.getTagName().length()).isPositive();
        assertThat(tagmeta.hashCode()).isNotZero();
    }

}
