package hep.crest.server.security;

import org.springframework.stereotype.Component;

@Component
public class AccountRepository {

	public UserResource findByUsername(String username) {
		if (username.equals("reader")) {
			return new UserResource("reader","r_password");
		} else if (username.equals("admin")) {
			return new UserResource("admin","a_password");
		} else if (username.equals("guest")){
			return new UserResource("guest","g_password");
		} else {
			return null;
		}
	}
}
