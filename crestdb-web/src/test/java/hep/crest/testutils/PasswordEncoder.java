package hep.crest.testutils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoder {

	public PasswordEncoder() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		String rawPassword = "crestusr";
		BCryptPasswordEncoder bpc = new BCryptPasswordEncoder();
		System.out.println("Password is "+bpc.encode(rawPassword));
	}

}
