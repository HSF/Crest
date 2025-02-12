package hep.crest.server;

import hep.crest.server.controllers.PageRequestHelper;
import hep.crest.server.serializers.ArgTimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("default")
@Slf4j
public class ToolsTest {

    @Test
    public void testArgTime() {
        assertThat(ArgTimeUnit.valueOf("NUMBER")).isEqualTo(ArgTimeUnit.NUMBER);
        assertThat(ArgTimeUnit.valueOf("MS")).isEqualTo(ArgTimeUnit.MS);
        assertThat(ArgTimeUnit.valueOf("COOL")).isEqualTo(ArgTimeUnit.COOL);
        assertThat(ArgTimeUnit.valueOf("CUSTOM")).isEqualTo(ArgTimeUnit.CUSTOM);
        assertThat(ArgTimeUnit.valueOf("ISO")).isEqualTo(ArgTimeUnit.ISO);
    }

    @Test
    public void testPageRequestHelper() {
        PageRequestHelper prh = new PageRequestHelper();
        BigInteger t1 = prh.getTimeFromArg("1560000000000", ArgTimeUnit.MS, ArgTimeUnit.SEC, null);
        assertThat(t1).isEqualTo(new BigInteger("1560000000"));

        BigInteger t2 = prh.getTimeFromArg("1560000000", ArgTimeUnit.SEC, ArgTimeUnit.MS, null);
        assertThat(t2).isEqualTo(new BigInteger("1560000000000"));

        BigInteger t3 = prh.getTimeFromArg("20220217T105320Z", ArgTimeUnit.ISO,
                ArgTimeUnit.SEC, null);
        assertThat(t3).isEqualTo(new BigInteger("1645095200"));

        BigInteger t3b = prh.getTimeFromArg("20220217105320+0000", ArgTimeUnit.CUSTOM,
                ArgTimeUnit.SEC, "yyyMMddHHmmssZ");
        assertThat(t3b).isEqualTo(new BigInteger("1645095200"));

        BigInteger t4 = prh.getTimeFromArg("1234", ArgTimeUnit.RUN,
                ArgTimeUnit.COOL, null);
        BigInteger res = BigInteger.valueOf(1234);
        res = res.shiftLeft(32);
        assertThat(t4).isEqualTo(res);

        BigInteger t5 = prh.getTimeFromArg("100-200", ArgTimeUnit.RUN_LUMI,
                ArgTimeUnit.COOL, null);
        BigInteger res2 = BigInteger.valueOf(100);
        res2 = res2.shiftLeft(32);
        res2 = res2.or(BigInteger.valueOf(200));
        assertThat(t5).isEqualTo(res2);

        BigInteger cr = prh.getCoolRunLumi(100L, 200L);
        assertThat(cr).isEqualTo(t5);

        BigInteger cr2 = prh.getCoolRunLumi("100", "200");
        assertThat(cr2).isEqualTo(t5);

        BigInteger cr3 = prh.getCoolRunLumi("INF", "200");
        assertThat(cr3).isGreaterThan(new BigInteger("2147483647"));
    }

}
