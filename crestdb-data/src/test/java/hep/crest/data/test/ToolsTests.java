/**
 * 
 */
package hep.crest.data.test;

import hep.crest.data.config.CrestProperties;
import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.exceptions.PayloadEncodingException;
import hep.crest.data.handlers.DateFormatterHandler;
import hep.crest.data.handlers.HashGenerator;
import hep.crest.data.pojo.Iov;
import hep.crest.data.pojo.Tag;
import hep.crest.data.repositories.IovDirImpl;
import hep.crest.data.repositories.TagDirImpl;
import hep.crest.data.test.tools.DataGenerator;
import hep.crest.data.utils.DirectoryUtilities;
import hep.crest.data.utils.RunIovConverter;
import hep.crest.swagger.model.IovDto;
import hep.crest.swagger.model.TagDto;
import ma.glasnost.orika.MapperFacade;
import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private MapperFacade mapper;

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
        assertThat(dirutils.getBasePath()).startsWithRaw(Paths.get("/tmp"));

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
        assertThat(dirutils.getBasePath()).startsWithRaw(Paths.get("/tmp"));
        assertThat(dirutils.getTagfile()).isEqualTo("tag.json"); // Should be true
        assertThat(dirutils.getIovfile()).isEqualTo("iovs.json"); // Should be true
        dirutils.createIfNotexistsTag("MY-NEW-TAG");
        log.info("Created tag : MY-NEW-TAG");
        final Path tp = dirutils.getTagFilePath("MY-NEW-TAG");
        log.info("Retrieved file path for tag : {}", tp);
        assertThat(tp).endsWithRaw(Paths.get("tag.json"));

        final TagDirImpl fstagrepository = new TagDirImpl(dirutils, mapper);
        final TagDto tag = DataGenerator.generateTagDto("MY-TAG-01", "time");
        Tag entity = mapper.map(tag, Tag.class);
        fstagrepository.save(entity);
        try {
            tag.name(null);
            fstagrepository.save(entity);
        } catch (final RuntimeException e) {
            log.info("Cannot store tag");
        }
        
        final IovDirImpl fsiovrepository = new IovDirImpl(dirutils, mapper);
        final IovDto iov = DataGenerator.generateIovDto("mydirhash", "MY-TAG-01",
                new BigDecimal(1000L));
        Iov ientity = mapper.map(iov, Iov.class);
        ientity.getId().setTagName(iov.getTagName());
        fsiovrepository.save(ientity);
        final List<Iov> iovlist = fsiovrepository.findByIdTagName("MY-TAG-01");
        assertThat(iovlist.size()).isPositive();
        try {
            final List<Iov> iovemptylist = fsiovrepository.findByIdTagName("MY-TAG-02");
            assertThat(iovemptylist.size()).isZero();
        }
        catch (final CdbServiceException e) {
            log.error("Cannot find tag MY-TAG-02");
        }
        final IovDto iov1 = DataGenerator.generateIovDto("mydirhash", "MY-TAG-01",
                new BigDecimal(1000L));
        iov1.tagName(null);
        Iov ientity1 = mapper.map(iov1, Iov.class);
        ientity1.getId().setTagName(iov1.getTagName());
        final Iov saved1 = fsiovrepository.save(ientity1);
        assertThat(saved1).isNull();

        fsiovrepository.saveAll("MY-TAG", null);
        fsiovrepository.findByIdTagName(null);
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
        assertThat(RunIovConverter.getRun(cmdmaxdata.toBigInteger())).isPositive();
        assertThat(RunIovConverter.getRun(cmdmaxrun.toBigInteger())).isPositive();
        assertThat(RunIovConverter.getRun(runlumi.toBigInteger())).isEqualTo(222222L);

        // Test getRun(Long)
        assertThat(RunIovConverter.getRun((Long) null)).isNull();
        assertThat(RunIovConverter.getRun(coolmaxrun)).isPositive();
        assertThat(RunIovConverter.getRun(coolmaxdate)).isPositive();
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
        assertThat(RunIovConverter.getTime(cmdmaxdata.toBigInteger())).isPositive();

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
        assertThat(cooltstr.length()).isPositive();

        final String coolotherstr = RunIovConverter.getCoolTimeRunLumiString(since.longValue(),
                "event");
        log.info("Created cool time string : {}", coolotherstr);
        assertThat(coolotherstr.length()).isPositive();

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

        final String hash6 = HashGenerator.md5Spring(barr);
        assertThat(hash5).isNotNull();

        final String hash7 = HashGenerator.md5Spring("Testing some byte array");
        assertThat(hash5).isNotNull();

        // Date format handler, used in serialization classes
        final DateFormatterHandler dh = new DateFormatterHandler();
        final DateTimeFormatter dtformat = dh.getLocformatter();
        final Timestamp ts = dh.format("2011-12-03T10:15:30+01:00");
        final Long tsmsec = ts.getTime();
        final String adate = dh.format(ts);
        log.info("Received date as string {}", adate);
        assertThat(adate.equals("2011-12-03T09:15:30Z")).isTrue(); // The date is in GMT this time.
        assertThat(tsmsec).isPositive();
        assertThat(dtformat).isNotNull();

        final DateFormatterHandler dfh = new DateFormatterHandler();
        dfh.setDatePATTERN("ISO_LOCAL_DATE_TIME");
        final DateTimeFormatter df = dfh.getLocformatter();
        assertThat(df).isNotNull();
        dfh.setDatePATTERN("ISO_DATE_TIME");
        final DateTimeFormatter df1 = dfh.getLocformatter();
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
        assertThat(props.toString().length()).isPositive();
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
