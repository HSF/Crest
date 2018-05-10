package hep.crest.data.security.pojo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import hep.crest.data.config.DatabasePropertyConfigurator;

@Entity
@Table(name = "CREST_FOLDERS", schema = DatabasePropertyConfigurator.SCHEMA_NAME)
public class CrestFolders {

	/**
	 * 
	 */
	private String nodeFullpath;
	/**
	 * 
	 */
	private String schemaName;
	/**
	 * 
	 */
	private String nodeName;
	/**
	 * 
	 */
	private String nodeDescription;
	/**
	 * 
	 */
	private String tagPattern;
	/**
	 * 
	 */
	private String groupRole;

	/**
	 * 
	 */
	public CrestFolders() {
	}


	public CrestFolders(String nodeFullpath, String schemaName, String nodeName, String nodeDescription,
			String tagPattern, String groupRole) {
		super();
		this.nodeFullpath = nodeFullpath;
		this.schemaName = schemaName;
		this.nodeName = nodeName;
		this.nodeDescription = nodeDescription;
		this.tagPattern = tagPattern;
		this.groupRole = groupRole;
	}


	@Id
	@Column(name = "CREST_NODE_FULLPATH", unique = true, nullable = false, length = 255)
	public String getNodeFullpath() {
		return nodeFullpath;
	}

	public void setNodeFullpath(String nodeFullpath) {
		this.nodeFullpath = nodeFullpath;
	}

	@Column(name = "CREST_SCHEMA_NAME", unique = false, nullable = false, length = 255)
	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	@Column(name = "CREST_NODE_NAME", unique = false, nullable = false, length = 255)
	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	@Column(name = "CREST_NODE_DESCRIPTION", unique = true, nullable = false, length = 2000)
	public String getNodeDescription() {
		return nodeDescription;
	}

	public void setNodeDescription(String nodeDescription) {
		this.nodeDescription = nodeDescription;
	}

	@Column(name = "CREST_TAG_PATTERN", unique = true, nullable = false, length = 255)
	public String getTagPattern() {
		return tagPattern;
	}

	public void setTagPattern(String tagPattern) {
		this.tagPattern = tagPattern;
	}

	@Column(name = "CREST_GROUP_ROLE", unique = false, nullable = false, length = 100)
	public String getGroupRole() {
		return groupRole;
	}

	public void setGroupRole(String groupRole) {
		this.groupRole = groupRole;
	}

	@Override
	public String toString() {
		return "CrestFolders [nodeFullpath=" + nodeFullpath + ", nodeName=" + nodeName + ", nodeDescription="
				+ nodeDescription + ", tagPattern=" + tagPattern + ", groupRole=" + groupRole + "]";
	}

}
