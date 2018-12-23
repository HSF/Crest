package hep.crest.server.caching;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Component
@ConfigurationProperties("caching")
public class CachingProperties {


	public final Integer default_cache_time = 60; // number of seconds

	private Integer iovsgroups_maxage = 120; // number of seconds
	
	private Integer iovsgroups_snapshot_maxage = 600; // number of seconds
	
	private Integer iovs_maxage = 600; // number of seconds

	private Integer iovs_snapshot_maxage = 1200;
	
	private Integer payloads_maxage = 1200;
	
	private Integer timetype_groupsize = 1000;
	
	private Integer runtype_groupsize = 10;

	public Integer getIovsgroups_maxage() {
		System.out.println("property iovsgroups_maxage has value: "+iovsgroups_maxage);
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

	public void setIovsgroups_maxage(Integer iovsgroups_maxage) {
		this.iovsgroups_maxage = iovsgroups_maxage;
	}

	public void setIovsgroups_snapshot_maxage(Integer iovsgroups_snapshot_maxage) {
		this.iovsgroups_snapshot_maxage = iovsgroups_snapshot_maxage;
	}

	public void setIovs_maxage(Integer iovs_maxage) {
		this.iovs_maxage = iovs_maxage;
	}

	public void setIovs_snapshot_maxage(Integer iovs_snapshot_maxage) {
		this.iovs_snapshot_maxage = iovs_snapshot_maxage;
	}

	public void setPayloads_maxage(Integer payloads_maxage) {
		this.payloads_maxage = payloads_maxage;
	}

	public void setTimetype_groupsize(Integer timetype_groupsize) {
		this.timetype_groupsize = timetype_groupsize;
	}

	public void setRuntype_groupsize(Integer runtype_groupsize) {
		this.runtype_groupsize = runtype_groupsize;
	}
	
}
