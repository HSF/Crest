/**
 * 
 */
package hep.crest.data.test.repo;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import hep.crest.data.pojo.GlobalTag;
import hep.crest.data.pojo.Tag;
import hep.crest.data.repositories.GlobalTagRepository;
import hep.crest.data.repositories.TagRepository;

/**
 * @author formica
 *
 */
@RunWith(SpringRunner.class)
@DataJpaTest
@ActiveProfiles("test")
public class SimpleRepositoryTests {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private GlobalTagRepository gtrepo;
	
	@Autowired
	private TagRepository trepo;

	@Test
	public void testGlobalTag() throws Exception {
		final GlobalTag entity = new GlobalTag("GT-TEST-XX");
		entity.setDescription("A test gtag");
		entity.setRelease("v1");
		entity.setWorkflow("test");
		entity.setScenario("test");
		entity.setValidity(new BigDecimal(2000L));
		//entity.setSnapshotTime(new Date(0L));
		this.entityManager.persist(entity);
		final Optional<GlobalTag> gt = this.gtrepo.findById("GT-TEST-XX");
		if (gt.isPresent()) {
			log.info("Found global tag " + gt.get());
		} else {
			log.error("cannot find global tag....something is wrong with the storage");
		}
		assertThat(gt.get().getRelease()).isEqualTo("v1");
		// clean up
		this.gtrepo.delete(gt.get());
	}

	@Test
	public void testTag() throws Exception {
		final Tag entity = new Tag("TAG-TEST-XX");
		entity.setDescription("A test tag");
		entity.setEndOfValidity(new BigDecimal(1000L));
		entity.setObjectType("test");
		entity.setSynchronization("raw");
		entity.setLastValidatedTime(new BigDecimal(2000L));
		entity.setTimeType("time");
		this.entityManager.persist(entity);
		final Tag gt = this.trepo.findByName("TAG-TEST-XX");
		if (gt != null) {
			log.info("Found tag " + gt);
		} else {
			log.error("cannot find tag....something is wrong with the storage");
		}
		assertThat(gt.getObjectType()).isEqualTo("test");
		// clean up
		this.trepo.delete(gt);
	}

}
