package hep.crest.server;


import hep.crest.server.caching.CachingProperties;
import hep.crest.server.services.IovService;
import hep.crest.server.services.TagService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ServiceTests {

    private static final Logger log = LoggerFactory.getLogger(ServiceTests.class);

    @Autowired
    private TagService tagService;

    @Autowired
    private IovService iovService;

    @Autowired
    private CachingProperties cprops;

    @Test
    public void testGroupSize() throws Exception {
        Long groupsize = iovService.getOptimalGroupSize("run");
        assertThat(groupsize).isEqualTo(Long.valueOf(cprops.getRuntypeGroupsize()));

        groupsize = iovService.getOptimalGroupSize("run-lumi");
        assertThat(groupsize).isEqualTo(Long.valueOf(cprops.getRuntypeGroupsize()));

        groupsize = iovService.getOptimalGroupSize("cool");
        assertThat(groupsize).isEqualTo(10L);
    }
}
