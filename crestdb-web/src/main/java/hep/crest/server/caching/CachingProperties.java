package hep.crest.server.caching;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Component
@ConfigurationProperties(prefix="cdb")
public class CachingProperties {


	public  final Integer default_cache_time = 60; // number of seconds

	private Integer iovsgroups_maxage; // number of seconds
	
	private Integer iovsgroups_snapshot_maxage; // number of seconds
	
	private Integer iovs_maxage; // number of seconds

	private Integer iovs_snapshot_maxage;
	
	private Integer payloads_maxage;
	
	private Integer timetype_groupsize;
	
	private Integer runtype_groupsize;

	public Integer getIovsgroups_maxage() {
		return iovsgroups_maxage;
	}

	public Integer getIovsgroups_snapshot_maxage() {
		return iovsgroups_snapshot_maxage;
	}

	public Integer getIovs_maxage() {
		return iovs_maxage;
	}

	public Integer getIovs_snapshot_maxage() {
		return iovs_snapshot_maxage;
	}

	public Integer getPayloads_maxage() {
		return payloads_maxage;
	}

	public Integer getTimetype_groupsize() {
		return timetype_groupsize;
	}

	public Integer getRuntype_groupsize() {
		return runtype_groupsize;
	}
	
}
