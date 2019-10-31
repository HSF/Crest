package hep.crest.data.test;


import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.sql.DataSource;

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
import org.springframework.test.context.junit4.SpringRunner;

import hep.crest.data.handlers.PayloadHandler;
import hep.crest.data.pojo.Payload;
import hep.crest.data.repositories.PayloadDataBaseCustom;
import hep.crest.data.repositories.PayloadDataGeneral;
import hep.crest.data.repositories.PayloadDataSQLITEImpl;
import hep.crest.data.test.tools.DataGenerator;
import hep.crest.swagger.model.PayloadDto;

@RunWith(SpringRunner.class)
@DataJpaTest
@ActiveProfiles("sqlite")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RepositorySqliteTests {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

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
    }

    @Test
    public void testPayload() throws Exception {
        
        final PayloadDataBaseCustom repobean = new PayloadDataSQLITEImpl(mainDataSource);
        final PayloadHandler handler = new PayloadHandler();
        ((PayloadDataGeneral)repobean).setPayloadHandler(handler);

        final PayloadDto dto = DataGenerator.generatePayloadDto("myhashsqlite1", "mydata", "mystreamer",
                "test");
        log.debug("Save payload {}", dto);
        if (dto.getSize() == null) {
            dto.setSize(dto.getData().length);
        }
        final Payload saved = repobean.save(dto);
        assertThat(saved).isNotNull();
        final Payload loaded = repobean.find("myhashsqlite1");
        assertThat(loaded.toString().length()).isGreaterThan(0);
        
        DataGenerator.generatePayloadData("/tmp/cdms/payloadatasqlite.blob","none");
        final File f = new File("/tmp/cdms/payloadatasqlite.blob");
        InputStream ds = new BufferedInputStream(new FileInputStream(f));
        
        dto.hash("mynewhashsqlite1");
        final Payload savedfromblob = repobean.save(dto,ds);
        assertThat(savedfromblob.toString().length()).isGreaterThan(0);
        if (ds != null) {
            ds.close();
        }
        final Payload loadedblob = repobean.findData(savedfromblob.getHash());
        assertThat(loadedblob.toString().length()).isGreaterThan(0);
        repobean.delete(loadedblob.getHash());
        
        ds = new BufferedInputStream(new FileInputStream(f));
        handler.saveStreamToFile(ds, "/tmp/cdms/payloadatacopysqlite.blob");
        final File f1 = new File("/tmp/cdms/payloadatacopysqlite.blob");
        final InputStream ds1 = new BufferedInputStream(new FileInputStream(f1));
        final byte[] barr = handler.getBytesFromInputStream(ds1);
        assertThat(barr.length).isGreaterThan(0);
        if (ds1 != null) {
            ds1.close();
        }
        final Payload loadedblob1 = repobean.find(savedfromblob.getHash());
        assertThat(loadedblob1.toString().length()).isGreaterThan(0);

        final PayloadDto pdto = handler.convertToDto(loadedblob1);
        assertThat(pdto).isNotNull();
        assertThat(pdto.getHash()).isEqualTo(loadedblob1.getHash());
    }
    
}
