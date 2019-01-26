package hep.crest.data.config;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Component
@ConfigurationProperties("crest")
public class CrestProperties {

	private Logger log = LoggerFactory.getLogger(this.getClass()); 

	private String dumpdir;
	private String schemaname;
	private String security;
	private String synchro;
	private String webstaticdir;
	private String authenticationtype;

	public static final BigDecimal INFINITY = new BigDecimal("253402297199000000000");

	public String getDumpdir() {
		log.info("property dumpdir has value:{}",dumpdir);
		return dumpdir;
	}

	public void setDumpdir(String dumpdir) {
		this.dumpdir = dumpdir;
	}

	public String getSchemaname() {
		log.info("property schemaname has value:{}",schemaname);
		return schemaname;
	}

	public void setSchemaname(String schemaname) {
		this.schemaname = schemaname;
	}

	public String getSecurity() {
		return security;
	}

	public void setSecurity(String security) {
		this.security = security;
	}

	public String getSynchro() {
		return synchro;
	}

	public void setSynchro(String synchro) {
		this.synchro = synchro;
	}

	public String getWebstaticdir() {
		return webstaticdir;
	}

	public void setWebstaticdir(String webstaticdir) {
		this.webstaticdir = webstaticdir;
	}

	public String getAuthenticationtype() {
		return this.authenticationtype;
	}
	
	public void setAuthenticationtype(String authtype) {
		this.authenticationtype = authtype;
	}
}
