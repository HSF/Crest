/**
 *
 */
package hep.crest.data.repositories.externals;

/**
 * @author formica
 *
 */
public final class SqlRequests {

    /**
     * Where condition on HASH.
     */
    private static final String WHERE_HASH = " WHERE HASH=? ";
    /**
     * Insert.
     */
    private static final String INSERT_INTO = "INSERT INTO ";

    /**
     * Private ctor.
     */
    private SqlRequests() {
    }

    /**
     * @param tablename the String
     * @return String
     */
    public static final String getFindQuery(String tablename) {
        return "select HASH,OBJECT_TYPE,VERSION,INSERTION_TIME,DATA,STREAMER_INFO, "
               + " DATA_SIZE from " + tablename + WHERE_HASH;
    }

    /**
     * @param tablename the String
     * @return String
     */
    public static final String getInsertQuery(String tablename) {
        return INSERT_INTO + tablename
               + "(HASH, OBJECT_TYPE, VERSION, DATA, STREAMER_INFO, INSERTION_TIME, DATA_SIZE) "
               + " VALUES (?,?,?,?,?,?,?)";
    }

    /**
     * @param tablename the String
     * @return String
     */
    public static final String getExistsHashQuery(String tablename) {
        return "select HASH from " + tablename + WHERE_HASH;
    }

    /**
     * @param tablename the String
     * @return String
     */
    public static final String getFindMetaQuery(String tablename) {
        return "select HASH,OBJECT_TYPE,VERSION,INSERTION_TIME,STREAMER_INFO, "
               + " DATA_SIZE from " + tablename + WHERE_HASH;
    }

    /**
     * @param tablename the String
     * @return String
     */
    public static final String getFindDataHashQuery(String tablename) {
        return "select HASH,DATA from " + tablename + WHERE_HASH;
    }

    /**
     * @param tablename the String
     * @return String
     */
    public static final String getFindDataQuery(String tablename) {
        return "select DATA from " + tablename + WHERE_HASH;
    }

    /**
     * @param tablename the String
     * @return String
     */
    public static final String getInsertAllQuery(String tablename) {
        return INSERT_INTO + tablename
               + "(HASH, OBJECT_TYPE, VERSION, DATA, STREAMER_INFO, INSERTION_TIME,DATA_SIZE) "
               + " VALUES (?,?,?,?,?,?,?)";
    }

    /**
     * @param tablename the String
     * @return String
     */
    public static final String getInsertMetaQuery(String tablename) {
        return INSERT_INTO + tablename
               + "(HASH, OBJECT_TYPE, VERSION, STREAMER_INFO, INSERTION_TIME,DATA_SIZE) "
               + " VALUES (?,?,?,?,?,?)";
    }

    /**
     * @param tablename the String
     * @return String
     */
    public static final String getDeleteQuery(String tablename) {
        return "DELETE FROM " + tablename + WHERE_HASH;
    }

    /**
     * Get payload info query using range.
     *
     * @param tablename
     * @param payloadtablename
     * @return
     */
    public static final String getRangeIovPayloadQuery(String tablename, String payloadtablename) {
        return "select iv.TAG_NAME, iv.SINCE, iv.INSERTION_TIME, iv.PAYLOAD_HASH, pyld.STREAMER_INFO, "
               + " pyld.VERSION, pyld.OBJECT_TYPE, " + " pyld.DATA_SIZE from " + tablename + " iv "
               + " LEFT JOIN " + payloadtablename + " pyld " + " ON iv.PAYLOAD_HASH=pyld.HASH "
               + " where iv.TAG_NAME=? AND iv.SINCE>=COALESCE(" + "  (SELECT max(iov2.SINCE) FROM "
               + tablename + " iov2 "
               + "  WHERE iov2.TAG_NAME=? AND iov2.SINCE<=? AND iov2.INSERTION_TIME<=? ),0)"
               + " AND iv.SINCE<=? AND iv.INSERTION_TIME<=? "
               + " ORDER BY iv.SINCE ASC, iv.INSERTION_TIME DESC";
    }
}
