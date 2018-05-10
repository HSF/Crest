package hep.crest.data.security.pojo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import hep.crest.data.config.DatabasePropertyConfigurator;

@Entity
@Table(name = "CREST_ROLES", schema = DatabasePropertyConfigurator.SCHEMA_NAME)
public class CrestRoles {

	private String id;
	private String role;

	public CrestRoles() {
	}

	public CrestRoles(String id, String role) {
		this.id = id;
		this.role = role;
	}

	@Id
	@Column(name = "CREST_USRID", unique = true, nullable = false, length = 100)
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}


	@Column(name = "CREST_USRROLE", unique = false, nullable = false, length = 100)
	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

}
