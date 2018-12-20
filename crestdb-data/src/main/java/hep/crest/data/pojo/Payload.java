package hep.crest.data.pojo;
// Generated Aug 2, 2016 3:50:25 PM by Hibernate Tools 3.2.2.GA

import java.sql.Blob;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;

import hep.crest.data.config.*;

/**
 * Payload generated by hbm2java
 */
@Entity
@Table(name = "PAYLOAD", schema = DatabasePropertyConfigurator.SCHEMA_NAME)
public class Payload implements java.io.Serializable {

	/**
	* 
	*/
	private static final long serialVersionUID = 5486724556389838782L;
	private String hash;
	private String version;
	private String objectType;
	private Integer size;
	private Blob data;
	private Blob streamerInfo;
	private Date insertionTime;

	public Payload() {
	}

	public Payload(String hash, String objectType, Blob data, Blob streamerInfo, Date insertionTime) {
		this.hash = hash;
		this.objectType = objectType;
		this.data = data;
		this.streamerInfo = streamerInfo;
		this.insertionTime = insertionTime;
	}

	/*
	 * @Override public PayloadDto createDto() { return PayloadHandler. }
	 */

	@Id
	@Column(name = "HASH", unique = true, nullable = false, length = 64)
	public String getHash() {
		return this.hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	////// @/////Version
	@Column(name = "VERSION", nullable = false, length = 20)
	public String getVersion() {
		return this.version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Column(name = "OBJECT_TYPE", nullable = false, length = 100)
	public String getObjectType() {
		return this.objectType;
	}

	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	@Column(name = "PYLD_SIZE", nullable = true)
	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	@Column(name = "DATA", nullable = false)
	@Lob
	@Type(type = "org.hibernate.type.BlobType")
	public Blob getData() {
		return this.data;
	}

	public void setData(Blob data) {
		this.data = data;
	}

	@Column(name = "STREAMER_INFO", nullable = false)
	@Lob
	@Type(type = "org.hibernate.type.BlobType")
	public Blob getStreamerInfo() {
		return this.streamerInfo;
	}

	public void setStreamerInfo(Blob streamerInfo) {
		this.streamerInfo = streamerInfo;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "INSERTION_TIME", nullable = false, length = 11)
	public Date getInsertionTime() {
		return this.insertionTime;
	}

	public void setInsertionTime(Date insertionTime) {
		this.insertionTime = insertionTime;
	}

	@Override
	public String toString() {
		return "Payload [hash=" + hash + ", version=" + version + ", objectType=" + objectType + ", size=" + size
				+ ", insertionTime=" + insertionTime + "]";
	}

}
