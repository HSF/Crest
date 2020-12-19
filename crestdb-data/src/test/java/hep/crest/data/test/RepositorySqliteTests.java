package hep.crest.data.test;


import hep.crest.data.handlers.PayloadHandler;
import hep.crest.data.pojo.Tag;
import hep.crest.data.repositories.PayloadDataBaseCustom;
import hep.crest.data.repositories.PayloadDataSQLITEImpl;
import hep.crest.data.repositories.TagMetaDataBaseCustom;
import hep.crest.data.repositories.TagMetaSQLITEImpl;
import hep.crest.data.repositories.TagRepository;
import hep.crest.data.test.tools.DataGenerator;
import hep.crest.swagger.model.PayloadDto;
import hep.crest.swagger.model.TagMetaDto;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
@TestPropertySource(locations = "classpath:application-sqlite.yml")
@ActiveProfiles("sqlite")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RepositorySqliteTests {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TagRepository tagrepository;

    @Autowired
    @Qualifier("dataSource")
    private DataSource mainDataSource;

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
        final Path cpath = Paths.get("/tmp/crest-dump");
        if (!cpath.toFile().exists()) {
            try {
                Files.createDirectories(cpath);
            }
            catch (final IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testPayload() throws Exception {

        final PayloadDataBaseCustom repobean = new PayloadDataSQLITEImpl(mainDataSource);
        final Instant now = Instant.now();
        final Date time = new Date(now.toEpochMilli());
        final PayloadDto dto = DataGenerator.generatePayloadDto("myhashsqlite1", "mydata", "mystreamer",
                "test", time);
        log.debug("Save payload {}", dto);
        if (dto.getSize() == null) {
            dto.setSize(dto.getData().length);
        }
        final PayloadDto saved = repobean.save(dto);
        assertThat(saved).isNotNull();
        final PayloadDto loaded = repobean.find("myhashsqlite1");
        assertThat(loaded.toString().length()).isPositive();

        DataGenerator.generatePayloadData("/tmp/cdms/payloadatasqlite.blob", " for sqlite");
        final File f = new File("/tmp/cdms/payloadatasqlite.blob");
        InputStream ds = new BufferedInputStream(new FileInputStream(f));

        dto.hash("mynewhashsqlite1");
        final PayloadDto savedfromblob = repobean.save(dto, ds);
        assertThat(savedfromblob.toString().length()).isNotNegative();
        if (ds != null) {
            ds.close();
        }
        final InputStream loadedblob = repobean.findData(savedfromblob.getHash());
        assertThat(loadedblob.available()).isPositive();
        repobean.delete(savedfromblob.getHash());

        ds = new BufferedInputStream(new FileInputStream(f));
        PayloadHandler.saveStreamToFile(ds, "/tmp/cdms/payloadatacopysqlite.blob");
        final File f1 = new File("/tmp/cdms/payloadatacopysqlite.blob");
        final InputStream ds1 = new BufferedInputStream(new FileInputStream(f1));
        final byte[] barr = PayloadHandler.getBytesFromInputStream(ds1);
        assertThat(barr).isNotEmpty();
        if (ds1 != null) {
            ds1.close();
        }
        final PayloadDto loadedblob1 = repobean.find(savedfromblob.getHash());
        assertThat(loadedblob1).isNull();

//        final PayloadDto pdto = lobhandler.convertToDto(loadedblob1);
//        assertThat(pdto).isNotNull();
//        assertThat(pdto.getHash()).isEqualTo(loadedblob1.getHash());
    }

    @Test
    public void testTags() throws Exception {
        final Instant now = Instant.now();
        final Date time = new Date(now.toEpochMilli());
        TagMetaDataBaseCustom tagmetarepobean = new TagMetaSQLITEImpl(mainDataSource);
        final Tag mtag = DataGenerator.generateTag("ASQLITE-TEST-FOR-META", "test");
        final Tag savedtag = tagrepository.save(mtag);
        final TagMetaDto metadto = DataGenerator.generateTagMetaDto("ASQLITE-TEST-FOR-META", "{ \"key\" : \"val\" }",
                time);
        final TagMetaDto savedmeta = tagmetarepobean.save(metadto);
        assertThat(savedmeta).isNotNull();
        assertThat(savedmeta.toString().length()).isPositive();
        assertThat(savedmeta.getTagName()).isEqualTo(savedtag.getName());

        final TagMetaDto storedmeta = tagmetarepobean.find(savedmeta.getTagName());
        assertThat(storedmeta).isNotNull();
        storedmeta.tagInfo("{ \"key1\" : \"val1\" }");
        final TagMetaDto updmeta = tagmetarepobean.update(storedmeta);
        assertThat(updmeta).isNotNull();
        tagmetarepobean.delete(updmeta.getTagName());
        final TagMetaDto deletedmeta = tagmetarepobean.find(updmeta.getTagName());
        assertThat(deletedmeta).isNull();
    }

}
