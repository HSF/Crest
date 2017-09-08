package hep.crest.server.services;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Component
@ConfigurationProperties("crest")
public class CrestProperties {

	
	private String dump_dir;

	public String getDump_dir() {
		System.out.println("property dump_dir has value: "+dump_dir);
		return dump_dir;
	}

	public void setDump_dir(String dump_dir) {
		this.dump_dir = dump_dir;
	}
	
}
