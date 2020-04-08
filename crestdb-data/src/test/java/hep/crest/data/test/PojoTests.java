/**
 * 
 */
package hep.crest.data.test;

import hep.crest.data.pojo.GlobalTagMapId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

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

        assertThat(mapid).isNotEqualTo(mapid2);
        assertThat(mapid).isNotEqualTo(mapid3);
        assertThat(mapid.hashCode()).isNotZero();
    }
}
