package hep.crest.server;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles({"test", "crest"})
class ServerApplicationTests {

	@Test
	void contextLoads() {
	}

}
