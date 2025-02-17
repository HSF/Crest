package hep.crest.server;

import hep.crest.server.controllers.PageRequestHelper;
import hep.crest.server.converters.CustomMapper;
import hep.crest.server.converters.DateFormatterHandler;
import hep.crest.server.converters.HashGenerator;
import hep.crest.server.converters.PayloadHandler;
import hep.crest.server.data.utils.RunIovConverter;
import hep.crest.server.serializers.ArgTimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

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

    @Test
    public void converterTest() {
        // Test the conversion of a string to a BigInteger
        CustomMapper cm = new CustomMapper();
        Timestamp now = Timestamp.from(Instant.now());
        OffsetDateTime nowdt = cm.asOffsetDateTime(now);
        Long nowdtl = nowdt.toEpochSecond();
        Long nowt = now.getTime()/1000;
        assertThat(nowdtl).isEqualTo(nowt);
        Timestamp fromdt = cm.asTimestamp(nowdt);
        assertThat(fromdt).isEqualTo(now);
        byte[] b = cm.stringToByteArray("test");
        assertThat(b).isNotNull();
        String s = cm.byteArrayToString(b);
        assertThat(s).isEqualTo("test");

        OffsetDateTime nullodt = cm.asOffsetDateTime(null);
        assertThat(nullodt).isNull();
        Timestamp nullts = cm.asTimestamp(null);
        assertThat(nullts).isNull();
        Date nulld = cm.toDate(null);
        assertThat(nulld).isNull();
        BigInteger nullbi = cm.bigDecimalToBigInt(null);
        assertThat(nullbi).isNull();

        DateFormatterHandler dfh = new DateFormatterHandler();
        String timestr = "2011-12-03T10:15:30+01:00";
        Timestamp tsf = dfh.format(timestr);
        assertThat(tsf).isNotNull();
        String tsfstr = dfh.format(tsf);
        assertThat(tsfstr).contains("2011-12-03T09:15:30"); // is in UTC
        dfh.setDatePATTERN("yyyy-MM-dd'T'HH:mm:ss");
        DateTimeFormatter dtf = dfh.getLocformatter();
        assertThat(dtf).isNotNull();

        dfh.setDatePATTERN("ISO_LOCAL_DATE_TIME");
        DateTimeFormatter dtf2 = dfh.getLocformatter();
        assertThat(dtf2).isNotNull();


        String payload = "test";
        InputStream is = new ByteArrayInputStream(payload.getBytes());
        byte[] b2 = PayloadHandler.getBytesFromInputStream(is);
        assertThat(b2).isNotNull();
        String hash =
                PayloadHandler.saveToFileGetHash(is, "testfile");
        assertThat(hash).isNotNull();
        String hash2 = PayloadHandler.getHashFromStream(new BufferedInputStream(is));
        assertThat(hash2).isNotNull();

        PayloadHandler.saveStreamToFile(is, "testfile2");
        File f = new File("testfile2");
        assertThat(f.exists()).isTrue();

        String hash3 = HashGenerator.md5Java("test");
        assertThat(hash3).isNotNull();
        String hash3r = HashGenerator.md5Java("test".getBytes());
        assertThat(hash3r).isNotNull();
        assertThat(hash3).isEqualTo(hash3r);
        String hash4 = HashGenerator.shaJava("test".getBytes());
        assertThat(hash4).isNotNull();
        String hash5 = HashGenerator.md5Spring("test");
        assertThat(hash5).isNotNull();
        String hash6 = HashGenerator.md5Spring("test".getBytes());
        assertThat(hash6).isNotNull();
        assertThat(hash5).isEqualTo(hash6);

        BigDecimal cooltime1 = RunIovConverter.getCoolTime(40000L,"run-lumi");
        BigDecimal coolrun1 = RunIovConverter.getCoolRun("40000");
        assertThat(cooltime1).isEqualTo(coolrun1);

        Long run1 = RunIovConverter.getRun(cooltime1.toBigInteger());
        assertThat(run1).isEqualTo(40000L);

        BigDecimal coolrunlumi1 = RunIovConverter.getCoolRunLumi(39000L, 10L);
        BigDecimal coolrunlumi2 = RunIovConverter.getCoolRunLumi("39000", "10");
        assertThat(coolrunlumi1).isEqualTo(coolrunlumi2);

        Long lb1 = RunIovConverter.getLumi(coolrunlumi1.toBigInteger());
        assertThat(lb1).isEqualTo(10L);

        BigDecimal runinf = RunIovConverter.getCoolRun("inf");
        assertThat(runinf).isGreaterThan(new BigDecimal("2147483647"));

        runinf = RunIovConverter.getCoolRunLumi("inf", "10");
        assertThat(runinf).isGreaterThan(new BigDecimal("2147483647"));

        BigInteger time1 =
                BigInteger.valueOf(1739640284L*1000)
                        .multiply(RunIovConverter.TO_NANOSECOND.toBigInteger());
        Long cooltime3 =
                RunIovConverter.getTime(time1);
        assertThat(cooltime3).isEqualTo(1739640284000L);
    }

}
