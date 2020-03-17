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
    private final String key;
    /**
     * The operation.
     */
    private final String operation;
    /**
     * The value.
     */
    private final Object value;

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
     * @return the operation
     */
    public String getOperation() {
        return operation;
    }

    /**
     * @return the value
     */
    public Object getValue() {
        return value;
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
