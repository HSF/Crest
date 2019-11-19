/**
 * 
 */
package hep.crest.data.repositories.externals;

/**
 * @author formica
 *
 */
public final class TagMetaRequests {

    
    /**
     * Private ctor.
     */
    private TagMetaRequests() {
    }

    /**
     * @param tablename the String
     * @return String
     */
    public static final String getFindQuery(String tablename) {
        return "select TAG_NAME,DESCRIPTION, CHANNEL_SIZE, COLUMN_SIZE,INSERTION_TIME,CHANNEL_INFO,PAYLOAD_INFO,CHANNEL_SIZE from "
                + tablename + " where TAG_NAME=?";
    }

    /**
     * @param tablename the String
     * @return String
     */
    public static final String getInsertAllQuery(String tablename) {
        return "INSERT INTO " + tablename
                + " (TAG_NAME, DESCRIPTION, CHANNEL_SIZE, COLUMN_SIZE, CHANNEL_INFO, PAYLOAD_INFO, INSERTION_TIME) VALUES (?,?,?,?,?,?,?)";

    }

    /**
     * @param tablename the String
     * @return String
     */
    public static final String getFindMetaQuery(String tablename) {
        return "select TAG_NAME,DESCRIPTION,INSERTION_TIME,CHANNEL_SIZE,COLUMN_SIZE from "
                + tablename + " where TAG_NAME=?";
    }
    
    /**
     * @param tablename the String
     * @return String
     */
    public static final String getUpdateQuery(String tablename) {
        return "UPDATE " + tablename
                + " SET DESCRIPTION=?, CHANNEL_SIZE=?, COLUMN_SIZE=?, CHANNEL_INFO=?, PAYLOAD_INFO=? WHERE TAG_NAME=?";

    }

    /**
     * @param tablename the String
     * @return String
     */
    public static final String getDeleteQuery(String tablename) {
        return "DELETE FROM " + tablename + " WHERE TAG_NAME=(?)";
    }
    
}
