/**
 * 
 */
package hep.crest.data.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import hep.crest.data.handlers.HashGenerator;
import hep.crest.data.test.tools.DataGenerator;
import hep.crest.data.utils.DirectoryUtilities;
import hep.crest.data.utils.RunIovConverter;

/**
 * @author formica
 *
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class ToolsTests {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testDirectoryTools() throws Exception {
        final DirectoryUtilities dirutils = new DirectoryUtilities();
        assertThat(dirutils.getBasePath().startsWith("/tmp")).isTrue();
        assertThat(dirutils.getTagfile()).isEqualTo("tag.json"); // Should be true
        assertThat(dirutils.getIovfile()).isEqualTo("iovs.json"); // Should be true
        dirutils.createIfNotexistsTag("MY-TAG");
        log.info("Created tag : MY-TAG");
        final Path tp = dirutils.getTagFilePath("MY-TAG");
        log.info("Retrieved file path for tag : {}",tp);
        assertThat(tp.endsWith("tag.json")).isTrue();

        dirutils.createIfNotexistsTag("MY-TAG-01");
        log.info("Created tag : MY-TAG-01");
        final List<String> dirs = dirutils.getTagDirectories();
        assertThat(dirs.size()).isGreaterThan(1);

        final Path basedir = dirutils.getBasePath();
        log.info("Base dir path is {}",basedir);

        dirutils.createIfNotexistsIov("MY-TAG");
        final Path ip = dirutils.getIovFilePath("MY-TAG");
        assertThat(ip.endsWith("iovs.json")).isTrue();

        final Path payloadpath = dirutils.getPayloadPath();
        final Path tagpath = dirutils.getTagPath("MY-TAG");
        log.info("Payload path is {}",payloadpath);
        log.info("Tag path is {}",tagpath);
        
        if (!dirutils.existsFile(payloadpath, "testhash.blob")) {
            final Path filepath = Paths.get(payloadpath.toString(), "testhash.blob");
            Files.createFile(filepath);
        }
        assertThat(dirutils.existsFile(payloadpath, "testhash.blob")).isTrue();
        
        dirutils.createTarFile(tagpath.toString(), "/tmp/tagtar");
    }    
   
    @Test
    public void testRunIovTools() throws Exception {
        final BigInteger nullbi = null;
        final long coolmaxdate = RunIovConverter.COOL_MAX_DATE;
        final long coolmaxrun = RunIovConverter.COOL_MAX_RUN;
        final BigDecimal cmd = new BigDecimal(coolmaxdate);
        assertThat(RunIovConverter.getRun(nullbi)).isNull();
        assertThat(RunIovConverter.getTime(nullbi)).isNull();
        assertThat(RunIovConverter.getRun(coolmaxrun)).isGreaterThan(0);
        assertThat(RunIovConverter.getTime(cmd.toBigInteger())).isGreaterThan(0);
        final BigDecimal runlumi = RunIovConverter.getCoolRunLumi("222222","100");
        assertThat(RunIovConverter.getCoolRun("222222")).isGreaterThan(new BigDecimal(222222L));
        assertThat(RunIovConverter.getRun(runlumi.toBigInteger())).isEqualTo(222222L);
        assertThat(RunIovConverter.getLumi(runlumi.toBigInteger())).isEqualTo(100L);
        
        final String coolstr = RunIovConverter.getCoolTimeRunLumiString(runlumi.longValue(), "run-lb");
        assertThat(coolstr.startsWith("222")).isTrue();
        log.info("Created cool run string : {}",coolstr);
        final BigDecimal since = RunIovConverter.getCoolTime(1500000000L, "time");        
        final String cooltstr = RunIovConverter.getCoolTimeRunLumiString(since.longValue(), "time");
        log.info("Created cool time string : {}",cooltstr);
        assertThat(cooltstr.length()).isGreaterThan(0);

        final String coolotherstr = RunIovConverter.getCoolTimeRunLumiString(since.longValue(), "event");
        log.info("Created cool time string : {}",coolotherstr);
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
        
        final OutputStream out = new FileOutputStream(new File("/tmp/cdms/payloaddatahash.blob.copy"));
        final BufferedInputStream ds2 = new BufferedInputStream(new FileInputStream(f));
        final String hash4 = HashGenerator.hashoutstream(ds2, out);
        assertThat(hash).isEqualTo(hash4);

    }

    
}
