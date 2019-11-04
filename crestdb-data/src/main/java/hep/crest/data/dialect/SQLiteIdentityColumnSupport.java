/**
 * 
 */
package hep.crest.data.dialect;

import org.hibernate.MappingException;
import org.hibernate.dialect.identity.IdentityColumnSupportImpl;

/**
 * @author formica
 *
 */
public class SQLiteIdentityColumnSupport extends IdentityColumnSupportImpl {

    /*
     * (non-Javadoc)
     *
     * @see org.hibernate.dialect.identity.IdentityColumnSupportImpl#
     * supportsIdentityColumns()
     */
    @Override
    public boolean supportsIdentityColumns() {
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.hibernate.dialect.identity.IdentityColumnSupportImpl#
     * getIdentitySelectString(java.lang.String, java.lang.String, int)
     */
    @Override
    public String getIdentitySelectString(String table, String column, int type)
            throws MappingException {
        return "select last_insert_rowid()";
    }

    /*
     * (non-Javadoc)
     *
     * @see org.hibernate.dialect.identity.IdentityColumnSupportImpl#
     * getIdentityColumnString(int)
     */
    @Override
    public String getIdentityColumnString(int type) throws MappingException {
        return "integer";
    }
}