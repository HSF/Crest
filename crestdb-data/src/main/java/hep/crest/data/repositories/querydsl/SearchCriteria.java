/**
 * 
 */
package hep.crest.data.repositories.querydsl;

/**
 * Search criteria class. Contains the needed field to create requests to the
 * DB.
 *
 * @author aformic
 *
 */
public class SearchCriteria {

    /**
     * The key.
     */
    private String key;
    /**
     * The operation.
     */
    private String operation;
    /**
     * The value.
     */
    private Object value;

    /**
     * Default Ctor.
     *
     * @param key
     *            the String
     * @param operation
     *            the String
     * @param value
     *            the Object
     */
    public SearchCriteria(String key, String operation, Object value) {
        super();
        this.key = key;
        this.operation = operation;
        this.value = value;
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @param key
     *            the key to set
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * @return the operation
     */
    public String getOperation() {
        return operation;
    }

    /**
     * @param operation
     *            the operation to set
     */
    public void setOperation(String operation) {
        this.operation = operation;
    }

    /**
     * @return the value
     */
    public Object getValue() {
        return value;
    }

    /**
     * @param value
     *            the value to set
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * @return String
     */
    public String dump() {
        return key + operation + value;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "SearchCriteria [key=" + key + ", operation=" + operation + ", value=" + value + "]";
    }

}
