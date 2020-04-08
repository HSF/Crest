/**
 * 
 */
package hep.crest.data.test;

import hep.crest.data.config.CrestProperties;
import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.exceptions.PayloadEncodingException;
import hep.crest.data.handlers.DateFormatterHandler;
import hep.crest.data.handlers.HashGenerator;
import hep.crest.data.repositories.IovDirectoryImplementation;
import hep.crest.data.repositories.TagDirectoryImplementation;
import hep.crest.data.test.tools.DataGenerator;
import hep.crest.data.utils.DirectoryUtilities;
import hep.crest.data.utils.RunIovConverter;
import hep.crest.swagger.model.IovDto;
import hep.crest.swagger.model.TagDto;
import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author formica
 *
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class ToolsTests {

    private static final Logger log = LoggerFactory.getLogger(ToolsTests.class);

    @Before
    public void setUp() {
        final Path bpath = Paths.get("/tmp/cdms");
        if (!bpath.toFile().exists()) {
            try {
                Files.createDirectories(bpath);
            }
            catch (final IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testDirectoryTools() throws Exception {
        final DirectoryUtilities dirutils = new DirectoryUtilities();
        assertThat(dirutils.getBasePath().startsWith("/tmp")).isTrue();

        final Path mtagpath = Paths.get(dirutils.getBasePath().toString(), "MANUAL-TAG");
        Files.createDirectories(mtagpath);
        log.info("Created tag directory manually : MANUAL-TAG");
        try {
            dirutils.getTagFilePath(dirutils.getBasePath().toString(), "MANUAL-TAG");
        }
        catch (final CdbServiceException e) {
            log.info("Cannot find the tag file for MANUAL-TAG");
        }
        try {
            dirutils.getIovFilePath(dirutils.getBasePath().toString(), "MANUAL-TAG");
        }
        catch (final CdbServiceException e) {
            log.info("Cannot find the iov file for MANUAL-TAG");
        }
        try {
            dirutils.createIfNotexistsIov(dirutils.getBasePath().toString(), "MANUAL-TAG-2");
        }
        catch (final CdbServiceException e) {
            log.info("Cannot find the iov file for MANUAL-TAG");
        }
        final Path base = dirutils.getBasePath("/tmp/cdms/pippo");
        assertThat(base).isNotNull();
        
        assertThat(dirutils.getTagfile()).isEqualTo("tag.json"); // Should be true
        assertThat(dirutils.getIovfile()).isEqualTo("iovs.json"); // Should be true
        dirutils.createIfNotexistsTag("MY-TAG");
        log.info("Created tag : MY-TAG");
        final Path tp = dirutils.getTagFilePath("MY-TAG");
        log.info("Retrieved file path for tag : {}", tp);
        assertThat(tp.endsWith("tag.json")).isTrue();

        dirutils.createIfNotexistsTag("MY-TAG-01");
        log.info("Created tag : MY-TAG-01");
        final List<String> dirs = dirutils.getTagDirectories();
        assertThat(dirs.size()).isGreaterThan(1);

        final Path basedir = dirutils.getBasePath();
        log.info("Base dir path is {}", basedir);

        dirutils.createIfNotexistsIov("MY-TAG");
        final Path ip = dirutils.getIovFilePath("MY-TAG");
        assertThat(ip.endsWith("iovs.json")).isTrue();

        final Path payloadpath = dirutils.getPayloadPath();
        final Path tagpath = dirutils.getTagPath("MY-TAG");
        log.info("Payload path is {}", payloadpath);
        log.info("Tag path is {}", tagpath);

        if (!dirutils.existsFile(payloadpath, "testhash.blob")) {
            final Path filepath = Paths.get(payloadpath.toString(), "testhash.blob");
            Files.createFile(filepath);
        }
        assertThat(dirutils.existsFile(payloadpath, "testhash.blob")).isTrue();

        dirutils.createTarFile(dirutils.getBasePath().toString(), "/tmp/cdms/alltagtar");
    }

    @Test
    public void testDirectoryImpl() throws Exception {
        final DirectoryUtilities dirutils = new DirectoryUtilities();
        assertThat(dirutils.getBasePath().startsWith("/tmp")).isTrue();
        assertThat(dirutils.getTagfile()).isEqualTo("tag.json"); // Should be true
        assertThat(dirutils.getIovfile()).isEqualTo("iovs.json"); // Should be true
        dirutils.createIfNotexistsTag("MY-NEW-TAG");
        log.info("Created tag : MY-NEW-TAG");
        final Path tp = dirutils.getTagFilePath("MY-NEW-TAG");
        log.info("Retrieved file path for tag : {}", tp);
        assertThat(tp.endsWith("tag.json")).isTrue();

        final TagDirectoryImplementation fstagrepository = new TagDirectoryImplementation(dirutils);
        final TagDto tag = DataGenerator.generateTagDto("MY-TAG-01", "time");
        fstagrepository.save(tag);

        final IovDirectoryImplementation fsiovrepository = new IovDirectoryImplementation(dirutils);
        final IovDto iov = DataGenerator.generateIovDto("mydirhash", "MY-TAG-01",
                new BigDecimal(1000L));
        fsiovrepository.save(iov);
        final List<IovDto> iovlist = fsiovrepository.findByTagName("MY-TAG-01");
        assertThat(iovlist.size()).isGreaterThan(0);
        try {
            final List<IovDto> iovemptylist = fsiovrepository.findByTagName("MY-TAG-02");
            assertThat(iovemptylist.size()).isEqualTo(0);
        }
        catch (final CdbServiceException e) {
            log.error("Cannot find tag MY-TAG-02");
        }
        final IovDto iov1 = DataGenerator.generateIovDto("mydirhash", "MY-TAG-01",
                new BigDecimal(1000L));
        iov1.tagName(null);
        IovDto saved1 = fsiovrepository.save(iov1);
        assertThat(saved1).isNull();

        fsiovrepository.saveAll("MY-TAG", null);
        fsiovrepository.findByTagName(null);
    }

    @Test
    public void testRunIovTools() throws Exception {
        final BigInteger nullbi = null;
        final long coolmaxdate = RunIovConverter.COOL_MAX_DATE;
        final long coolmaxrun = RunIovConverter.COOL_MAX_RUN;
        final BigDecimal cmdmaxdata = new BigDecimal(coolmaxdate);
        final BigDecimal cmdmaxrun = new BigDecimal(coolmaxrun);
        final BigDecimal runlumi = RunIovConverter.getCoolRunLumi("222222", "100");

        assertThat(RunIovConverter.getTime(nullbi)).isNull();
        // Test getRun(BigInteger)
        assertThat(RunIovConverter.getRun(nullbi)).isNull();
        assertThat(RunIovConverter.getRun(cmdmaxdata.toBigInteger())).isGreaterThan(0);
        assertThat(RunIovConverter.getRun(cmdmaxrun.toBigInteger())).isGreaterThan(0);
        assertThat(RunIovConverter.getRun(runlumi.toBigInteger())).isEqualTo(222222L);

        // Test getRun(Long)
        assertThat(RunIovConverter.getRun((Long) null)).isNull();
        assertThat(RunIovConverter.getRun(coolmaxrun)).isGreaterThan(0);
        assertThat(RunIovConverter.getRun(coolmaxdate)).isGreaterThan(0);
        // getCoolRun
        assertThat(RunIovConverter.getCoolRun("222222")).isGreaterThan(new BigDecimal(222222L));
        assertThat(RunIovConverter.getCoolRun("INF")).isGreaterThan(new BigDecimal(222222L));
        assertThat(RunIovConverter.getCoolRun(null)).isNull();

        // getLumi(BigInteger)
        assertThat(RunIovConverter.getLumi(nullbi)).isNull();
        assertThat(RunIovConverter.getLumi(cmdmaxdata.toBigInteger())).isGreaterThanOrEqualTo(0);
        assertThat(RunIovConverter.getLumi(runlumi.toBigInteger())).isEqualTo(100L);

        // getLumi(Long)
        assertThat(RunIovConverter.getLumi((Long) null)).isNull();

        // getTime(BigInteger)
        assertThat(RunIovConverter.getTime(cmdmaxdata.toBigInteger())).isGreaterThan(0);

        // getCoolRunLumi(String, String)
        assertThat(RunIovConverter.getCoolRunLumi(null, "100")).isNull();
        assertThat(RunIovConverter.getCoolRunLumi("1000", "100"))
                .isGreaterThan(new BigDecimal(1000000L));
        // getCoolRunLumi(Long, Long)
        assertThat(RunIovConverter.getCoolRunLumi(100L, null))
                .isGreaterThanOrEqualTo(new BigDecimal(100L));

        // getCoolTimeString(Long, iovbase)
        final String cooltimestr = RunIovConverter.getCoolTimeString(runlumi.longValue(), "run-lb");
        assertThat(cooltimestr).isNull();
        final Long nowms = Instant.now().getMillis();
        assertThat(RunIovConverter.getCoolTimeString(nowms, "time")).isNotNull();

        final String coolstr = RunIovConverter.getCoolTimeRunLumiString(runlumi.longValue(),
                "run-lb");
        assertThat(coolstr.startsWith("222")).isTrue();
        log.info("Created cool run string : {}", coolstr);
        final BigDecimal since = RunIovConverter.getCoolTime(1500000000L, "time");
        final String cooltstr = RunIovConverter.getCoolTimeRunLumiString(since.longValue(), "time");
        log.info("Created cool time string : {}", cooltstr);
        assertThat(cooltstr.length()).isGreaterThan(0);

        final String coolotherstr = RunIovConverter.getCoolTimeRunLumiString(since.longValue(),
                "event");
        log.info("Created cool time string : {}", coolotherstr);
        assertThat(coolotherstr.length()).isGreaterThan(0);

    }

    @Test
    public void testHandlerTools() throws Exception {
        DataGenerator.generatePayloadData("/tmp/cdms/payloaddatahash.blob", "now for hashing");
        final File f = new File("/tmp/cdms/payloaddatahash.blob");
        final BufferedInputStream ds = new BufferedInputStream(new FileInputStream(f));
        final String hash = HashGenerator.hash(ds);
        assertThat(hash).isNotNull();

        final byte[] barr = new String("Testing some byte array").getBytes();
        final String hash2 = HashGenerator.md5Java(barr);
        assertThat(hash2).isNotNull();

        final String hash3 = HashGenerator.md5Java("Testing some byte array");
        assertThat(hash3).isEqualTo(hash2);

        final OutputStream out = new FileOutputStream(
                new File("/tmp/cdms/payloaddatahash.blob.copy"));
        final BufferedInputStream ds2 = new BufferedInputStream(new FileInputStream(f));
        final String hash4 = HashGenerator.hashoutstream(ds2, out);
        assertThat(hash).isEqualTo(hash4);

        final String hash5 = HashGenerator.shaJava(barr);
        assertThat(hash5).isNotNull();

        String hash6 = HashGenerator.md5Spring(barr);
        assertThat(hash5).isNotNull();

        String hash7 = HashGenerator.md5Spring("Testing some byte array");
        assertThat(hash5).isNotNull();

        // Date format handler, used in serialization classes
        final DateFormatterHandler dh = new DateFormatterHandler();
        final DateTimeFormatter dtformat = dh.getLocformatter();
        final Timestamp ts = dh.format("2011-12-03T10:15:30+01:00");
        final Long tsmsec = ts.getTime();
        final String adate = dh.format(ts);
        log.info("Received date as string {}", adate);
        assertThat(adate.equals("2011-12-03T09:15:30Z")).isTrue(); // The date is in GMT this time.
        assertThat(tsmsec).isGreaterThan(0);
        assertThat(dtformat).isNotNull();

        DateFormatterHandler dfh = new DateFormatterHandler();
        dfh.setDatePATTERN("ISO_LOCAL_DATE_TIME");
        DateTimeFormatter df = dfh.getLocformatter();
        assertThat(df).isNotNull();
        dfh.setDatePATTERN("ISO_DATE_TIME");
        DateTimeFormatter df1 = dfh.getLocformatter();
        assertThat(df1).isNotNull();
    }

    @Test
    public void propertyTest() {

        final CrestProperties props = new CrestProperties();
        props.setApiname("api");
        props.setAuthenticationtype("BASIC");
        props.setDumpdir("/tmp");
        props.setSchemaname("CREST");
        props.setSecurity("none");
        props.setSynchro("all");
        props.setWebstaticdir("/tmp");
        assertThat(props.toString().length()).isGreaterThan(0);
        assertThat(props.getApiname()).isEqualTo("api");
        assertThat(props.getAuthenticationtype()).isEqualTo("BASIC");
        assertThat(props.getDumpdir()).isEqualTo("/tmp");
        assertThat(props.getSchemaname()).isEqualTo("CREST");
        assertThat(props.getSecurity()).isEqualTo("none");
        assertThat(props.getSynchro()).isEqualTo("all");
        assertThat(props.getWebstaticdir()).isEqualTo("/tmp");
    }


    @Test
    public void exceptionTest() {
        final NullPointerException np = new NullPointerException("null");
        final CdbServiceException es = new CdbServiceException("message");
        assertThat(es.getMessage()).contains("message");
        final CdbServiceException ees = new CdbServiceException("message", np);
        assertThat(ees.getCause()).isNotNull();
        final PayloadEncodingException e = new PayloadEncodingException("message");
        assertThat(e.getMessage()).contains("message");
        final PayloadEncodingException ee = new PayloadEncodingException(np);
        assertThat(ee.getCause()).isNotNull();
    }

}
