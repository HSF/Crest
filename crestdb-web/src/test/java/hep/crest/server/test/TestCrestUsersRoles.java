package hep.crest.server.test;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import hep.crest.data.security.pojo.CrestRoles;
import hep.crest.data.security.pojo.CrestUser;
import hep.crest.data.security.pojo.RoleRepository;
import hep.crest.data.security.pojo.UserRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestCrestUsersRoles {
    @Autowired
    private UserRepository userRepository;
   
    @Autowired
    private RoleRepository roleRepository;

    @Test
    public void testA_storeUsersRoles() {
    		String pss = new BCryptPasswordEncoder().encode("password");
    		CrestUser usrentity = new CrestUser("cusr",pss);
    		usrentity.setId("cusr_1");
        System.out.println("Store request for user: "+usrentity);
        userRepository.save(usrentity);
		CrestRoles rolentity = new CrestRoles("cusr_1","ROLE_user");
        System.out.println("Store request for role: "+rolentity);
		roleRepository.save(rolentity);
		
		String pssadmin = new BCryptPasswordEncoder().encode("password");
		CrestUser adminentity = new CrestUser("adminusr",pssadmin);
		adminentity.setId("adm_1");
		System.out.println("Store request for user: "+adminentity);
		userRepository.save(adminentity);
		CrestRoles adrolentity = new CrestRoles("adm_1","ROLE_admin");
		System.out.println("Store request for role: "+adrolentity);
		roleRepository.save(adrolentity);
		
		String pssguru = new BCryptPasswordEncoder().encode("guru");
		CrestUser gentity = new CrestUser("guruusr",pssguru);
		gentity.setId("guru_1");
		System.out.println("Store request for user: "+gentity);
		userRepository.save(gentity);
		CrestRoles grolentity = new CrestRoles("guru_1","ROLE_GURU");
		System.out.println("Store request for role: "+grolentity);
		roleRepository.save(grolentity);
    }
    

}
