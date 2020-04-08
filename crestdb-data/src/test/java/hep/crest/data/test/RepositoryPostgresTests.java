package hep.crest.data.test;


import hep.crest.data.repositories.PayloadDataBaseCustom;
import hep.crest.data.repositories.PayloadDataPostgresImpl;
import hep.crest.data.repositories.TagRepository;
import hep.crest.data.test.tools.DataGenerator;
import hep.crest.swagger.model.PayloadDto;
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
@TestPropertySource(locations="classpath:application-sqlite.yml")
@ActiveProfiles("sqlite")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RepositoryPostgresTests {

    private static final Logger log = LoggerFactory.getLogger(RepositoryPostgresTests.class);

    @Autowired
    private TagRepository tagrepository;

    @Autowired
    @Qualifier("dataSource") 
    private DataSource mainDataSource;

//    @Autowired
//    @Qualifier("sqliteDatasource")
//    private DataSource sqliteDataSource;

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
    // This is a trick, we do not have a postgresql connection
        final PayloadDataBaseCustom repobean = new PayloadDataPostgresImpl(mainDataSource);
        final Instant now = Instant.now();
        final Date time = new Date(now.toEpochMilli());
        final PayloadDto dto = DataGenerator.generatePayloadDto("myhashsqlite1", "mydata", "mystreamer",
                "test", time);
        log.debug("Save payload {}", dto);
        if (dto.getSize() == null) {
            dto.setSize(dto.getData().length);
        }
        assertThat(dto).isNotNull();
    }
}
