/**
 * 
 */
package hep.crest.data.dialect;
import java.sql.Types;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.identity.IdentityColumnSupport;

/**
 * This code was taken from internet.
 * @author formica
 *
 */
public class SQLiteDialect extends Dialect {

    private static final String INTEGER = "integer";
    private static final String BLOB = "blob";
    /**
     * Ctor. Register types.
     */
    public SQLiteDialect() {
        super.registerColumnType(Types.BIT, INTEGER);  //NOSONAR
        super.registerColumnType(Types.TINYINT, "tinyint");  //NOSONAR
        super.registerColumnType(Types.SMALLINT, "smallint");  //NOSONAR
        super.registerColumnType(Types.INTEGER, INTEGER);  //NOSONAR
        super.registerColumnType(Types.BIGINT, "bigint");  //NOSONAR
        super.registerColumnType(Types.FLOAT, "float");
        super.registerColumnType(Types.REAL, "real");
        super.registerColumnType(Types.DOUBLE, "double");
        super.registerColumnType(Types.NUMERIC, "numeric");
        super.registerColumnType(Types.DECIMAL, "decimal");
        super.registerColumnType(Types.CHAR, "char");
        super.registerColumnType(Types.VARCHAR, "varchar");
        super.registerColumnType(Types.LONGVARCHAR, "longvarchar");
        super.registerColumnType(Types.DATE, "date");
        super.registerColumnType(Types.TIME, "time");
        super.registerColumnType(Types.TIMESTAMP, "timestamp");
        super.registerColumnType(Types.BINARY, BLOB);
        super.registerColumnType(Types.VARBINARY, BLOB);
        super.registerColumnType(Types.LONGVARBINARY, BLOB);
        super.registerColumnType(Types.BLOB, BLOB);
        super.registerColumnType(Types.CLOB, "clob");
        super.registerColumnType(Types.BOOLEAN, INTEGER);
    }

    @Override
    public IdentityColumnSupport getIdentityColumnSupport() {
        return new SQLiteIdentityColumnSupport();
    }

    @Override
    public boolean hasAlterTable() {
        return false;
    }

    @Override
    public boolean dropConstraints() {
        return false;
    }

    @Override
    public String getDropForeignKeyString() {
        return "";
    }

    @Override
    public String getAddForeignKeyConstraintString(String constraintName, String[] foreignKey, String referencedTable, String[] primaryKey, boolean referencesPrimaryKey) {
        return "";
    }

    @Override
    public String getAddPrimaryKeyConstraintString(String constraintName) {
        return "";
    }

    @Override
    public String getForUpdateString() {
        return "";
    }

    @Override
    public String getAddColumnString() {
        return "add column";
    }

    @Override
    public boolean supportsOuterJoinForUpdate() {
        return false;
    }

    @Override
    public boolean supportsIfExistsBeforeTableName() {
        return true;
    }

    @Override
    public boolean supportsCascadeDelete() {
        return false;
    }
}