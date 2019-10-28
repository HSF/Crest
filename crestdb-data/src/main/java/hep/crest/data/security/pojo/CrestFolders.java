package hep.crest.data.security.pojo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import hep.crest.data.config.DatabasePropertyConfigurator;

/**
 * @author formica
 *
 */
@Entity
@Table(name = "CREST_FOLDERS", schema = DatabasePropertyConfigurator.SCHEMA_NAME)
public class CrestFolders {

    /**
     * The node full path.
     */
    private String nodeFullpath;
    /**
     * The schema name.
     */
    private String schemaName;
    /**
     * The node name.
     */
    private String nodeName;
    /**
     * The node description.
     */
    private String nodeDescription;
    /**
     * The tag base name.
     */
    private String tagPattern;
    /**
     * The group role.
     */
    private String groupRole;

    /**
     * Default ctor.
     */
    public CrestFolders() {
    }

    /**
     * @param nodeFullpath
     *            the String
     * @param schemaName
     *            the String
     * @param nodeName
     *            the String
     * @param nodeDescription
     *            the String
     * @param tagPattern
     *            the String
     * @param groupRole
     *            the String
     */
    public CrestFolders(String nodeFullpath, String schemaName, String nodeName,
            String nodeDescription, String tagPattern, String groupRole) {
        super();
        this.nodeFullpath = nodeFullpath;
        this.schemaName = schemaName;
        this.nodeName = nodeName;
        this.nodeDescription = nodeDescription;
        this.tagPattern = tagPattern;
        this.groupRole = groupRole;
    }

    /**
     * @return String
     */
    @Id
    @Column(name = "CREST_NODE_FULLPATH", unique = true, nullable = false, length = 255)
    public String getNodeFullpath() {
        return nodeFullpath;
    }

    /**
     * @param nodeFullpath
     *            the String
     * @return
     */
    public void setNodeFullpath(String nodeFullpath) {
        this.nodeFullpath = nodeFullpath;
    }

    /**
     * @return String
     */
    @Column(name = "CREST_SCHEMA_NAME", unique = false, nullable = false, length = 255)
    public String getSchemaName() {
        return schemaName;
    }

    /**
     * @param schemaName
     *            the String
     * @return
     */
    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    /**
     * @return String
     */
    @Column(name = "CREST_NODE_NAME", unique = false, nullable = false, length = 255)
    public String getNodeName() {
        return nodeName;
    }

    /**
     * @param nodeName
     *            the String
     * @return
     */
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    /**
     * @return String
     */
    @Column(name = "CREST_NODE_DESCRIPTION", unique = true, nullable = false, length = 2000)
    public String getNodeDescription() {
        return nodeDescription;
    }

    /**
     * @param nodeDescription
     *            the String
     * @return
     */
    public void setNodeDescription(String nodeDescription) {
        this.nodeDescription = nodeDescription;
    }

    /**
     * @return String
     */
    @Column(name = "CREST_TAG_PATTERN", unique = true, nullable = false, length = 255)
    public String getTagPattern() {
        return tagPattern;
    }

    /**
     * @param tagPattern
     *            the String
     * @return
     */
    public void setTagPattern(String tagPattern) {
        this.tagPattern = tagPattern;
    }

    /**
     * @return String
     */
    @Column(name = "CREST_GROUP_ROLE", unique = false, nullable = false, length = 100)
    public String getGroupRole() {
        return groupRole;
    }

    /**
     * @param groupRole
     *            the String
     * @return
     */
    public void setGroupRole(String groupRole) {
        this.groupRole = groupRole;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "CrestFolders [nodeFullpath=" + nodeFullpath + ", nodeName=" + nodeName
                + ", nodeDescription=" + nodeDescription + ", tagPattern=" + tagPattern
                + ", groupRole=" + groupRole + "]";
    }

}
