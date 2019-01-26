package hep.crest.server.caching;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Component
@ConfigurationProperties("caching")
public class CachingProperties {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	public static final Integer DEFAULT_CACHE_TIME = 60; // number of seconds

	private Integer iovsgroupsMaxage = 120; // number of seconds
	
	private Integer iovsgroupsSnapshotMaxage = 600; // number of seconds
	
	private Integer iovsMaxage = 600; // number of seconds

	private Integer iovsSnapshotMaxage = 1200;
	
	private Integer payloadsMaxage = 1200;
	
	private Integer timetypeGroupsize = 1000;
	
	private Integer runtypeGroupsize = 10;

	public Integer getIovsgroupsMaxage() {
		log.info("property iovsgroups_maxage has value: {}",iovsgroupsMaxage);
		return iovsgroupsMaxage;
	}

	public Integer getIovsgroupsSnapshotMaxage() {
		return iovsgroupsSnapshotMaxage;
	}

	public Integer getIovsMaxage() {
		return iovsMaxage;
	}

	public Integer getIovsSnapshotMaxage() {
		return iovsSnapshotMaxage;
	}

	public Integer getPayloadsMaxage() {
		return payloadsMaxage;
	}

	public Integer getTimetypeGroupsize() {
		return timetypeGroupsize;
	}

	public Integer getRuntypeGroupsize() {
		return runtypeGroupsize;
	}

	public void setIovsgroupsMaxage(Integer iovsgroupsmaxage) {
		this.iovsgroupsMaxage = iovsgroupsmaxage;
	}

	public void setIovsgroupsSnapshotMaxage(Integer iovsgroupssnapshotmaxage) {
		this.iovsgroupsSnapshotMaxage = iovsgroupssnapshotmaxage;
	}

	public void setIovsMaxage(Integer iovsmaxage) {
		this.iovsMaxage = iovsmaxage;
	}

	public void setIovsSnapshotMaxage(Integer iovssnapshotmaxage) {
		this.iovsSnapshotMaxage = iovssnapshotmaxage;
	}

	public void setPayloadsMaxage(Integer payloadsmaxage) {
		this.payloadsMaxage = payloadsmaxage;
	}

	public void setTimetypeGroupsize(Integer timetypegroupsize) {
		this.timetypeGroupsize = timetypegroupsize;
	}

	public void setRuntypeGroupsize(Integer runtypegroupsize) {
		this.runtypeGroupsize = runtypegroupsize;
	}
	
}
