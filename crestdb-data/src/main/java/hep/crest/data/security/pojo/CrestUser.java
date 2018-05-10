package hep.crest.data.security.pojo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import hep.crest.data.config.DatabasePropertyConfigurator;

@Entity
@Table(name = "CREST_USERS", schema = DatabasePropertyConfigurator.SCHEMA_NAME)
public class CrestUser {

	private String id;
	private String username;
	private String password;

	public CrestUser() {
	}

	public CrestUser(String username, String password) {
		this.username = username;
		this.password = password;
	}

	@Id
	@Column(name = "CREST_USRID", unique = true, nullable = false, length = 100)
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Column(name = "CREST_USRNAME", unique = true, nullable = false, length = 100)
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Column(name = "CREST_USRPSS", unique = true, nullable = false, length = 100)
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
