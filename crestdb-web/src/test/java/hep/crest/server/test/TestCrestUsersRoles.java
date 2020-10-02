package hep.crest.server.test;

import hep.crest.data.security.pojo.CrestRoles;
import hep.crest.data.security.pojo.CrestUser;
import hep.crest.data.security.pojo.RoleRepository;
import hep.crest.data.security.pojo.UserRepository;
import hep.crest.server.security.UserResource;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ActiveProfiles("test")
public class TestCrestUsersRoles {
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    public void testA_storeUsersRoles() {
        final String pss = new BCryptPasswordEncoder().encode("password");
        final CrestUser usrentity = new CrestUser("cusr", pss);
        usrentity.setId("cusr_1");
        log.info("Store request for user: " + usrentity);
        CrestUser saveduser = userRepository.save(usrentity);
        assertThat(saveduser).isNotNull();
        
        final CrestRoles rolentity = new CrestRoles("cusr_1", "ROLE_user");
        log.info("Store request for role: " + rolentity);
        CrestRoles savedrole = roleRepository.save(rolentity);
        assertThat(savedrole).isNotNull();
        
        final String pssadmin = new BCryptPasswordEncoder().encode("password");
        final CrestUser adminentity = new CrestUser("adminusr", pssadmin);
        adminentity.setId("adm_1");
        log.info("Store request for user: " + adminentity);
        saveduser = userRepository.save(adminentity);
        assertThat(saveduser).isNotNull();
        
        final CrestRoles adrolentity = new CrestRoles("adm_1", "ROLE_admin");
        log.info("Store request for role: " + adrolentity);
        savedrole = roleRepository.save(adrolentity);
        assertThat(savedrole).isNotNull();
        
        final String pssguru = new BCryptPasswordEncoder().encode("guru");
        final CrestUser gentity = new CrestUser("guruusr", pssguru);
        gentity.setId("guru_1");
        log.info("Store request for user: " + gentity);
        saveduser = userRepository.save(gentity);
        assertThat(saveduser).isNotNull();

        final CrestRoles grolentity = new CrestRoles("guru_1", "ROLE_GURU");
        log.info("Store request for role: " + grolentity);
        roleRepository.save(grolentity);
    }

    @Test
    public void userResourceTest() {
        UserResource resource = new UserResource();
        resource.setId("user");
        resource.setUsername("crest");
        resource.setPassword("guessit");
        assertThat(resource.toString().length()).isGreaterThan(0);
        assertThat(resource.getId()).isEqualTo("user");
        assertThat(resource.getUsername()).isEqualTo("crest");
        assertThat(resource.getPassword()).isEqualTo("guessit");
        assertThat(resource.hashCode()).isNotZero();
    }
}
