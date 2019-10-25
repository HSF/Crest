/**
 * 
 */
package hep.crest.data.repositories.externals;

/**
 * @author formica
 *
 */
public final class PayloadRequests {

    /**
     * @param tablename the String
     * @return String
     */
    public static final String getFindQuery(String tablename) {
        return "select HASH,OBJECT_TYPE,VERSION,INSERTION_TIME,DATA,STREAMER_INFO, "
                + " DATA_SIZE from "+ tablename + " where HASH=?";
    }

    /**
     * @param tablename the String
     * @return String
     */
    public static final String getInsertQuery(String tablename) {
        return "INSERT INTO "+tablename
                + "(HASH, OBJECT_TYPE, VERSION, DATA, STREAMER_INFO, INSERTION_TIME, DATA_SIZE) "
                + " VALUES (?,?,?,?,?,?,?)";
    }

    /**
     * @param tablename the String
     * @return String
     */
    public static final String getFindMetaQuery(String tablename) {
        return "select HASH,OBJECT_TYPE,VERSION,INSERTION_TIME,STREAMER_INFO, "
                + " DATA_SIZE from " + tablename + " where HASH=?";
    }
    
    /**
     * @param tablename the String
     * @return String
     */
    public static final String getFindDataHashQuery(String tablename) {
        return "select HASH,DATA from " + tablename + " where HASH=?";
    }

    /**
     * @param tablename the String
     * @return String
     */
    public static final String getFindDataQuery(String tablename) {
        return "select DATA from " + tablename + " where HASH=?";
    }

    /**
     * @param tablename the String
     * @return String
     */
    public static final String getInsertAllQuery(String tablename) {
        return  "INSERT INTO " + tablename
                + "(HASH, OBJECT_TYPE, VERSION, DATA, STREAMER_INFO, INSERTION_TIME,DATA_SIZE) "
                + " VALUES (?,?,?,?,?,?,?)";
    }
    
    /**
     * @param tablename the String
     * @return String
     */
    public static final String getInsertMetaQuery(String tablename) {
        return "INSERT INTO " + tablename
                + "(HASH, OBJECT_TYPE, VERSION, STREAMER_INFO, INSERTION_TIME,DATA_SIZE) "
                + " VALUES (?,?,?,?,?,?)";
    }

    /**
     * @param tablename the String
     * @return String
     */
    public static final String getDeleteQuery(String tablename) {
        return "DELETE FROM " + tablename + " WHERE HASH=(?)";
    }
    
}
