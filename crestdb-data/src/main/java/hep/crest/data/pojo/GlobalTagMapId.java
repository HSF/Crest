package hep.crest.data.pojo;
// Generated Aug 2, 2016 3:50:25 PM by Hibernate Tools 3.2.2.GA

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * GlobalTagMapId generated by hbm2java.
 */
@Embeddable
public class GlobalTagMapId implements java.io.Serializable {

    /**
     * Serializer.
     */
    private static final long serialVersionUID = -2041411292811311312L;
    /**
     * The global tag name.
     */
    private String globalTagName;
    /**
     * The record. Used to identify a specific tag usage.
     */
    private String record;
    /**
     * The label. It represent a generic tag name, similarly to COOL folder.
     */
    private String label;

    /**
     * Default Ctor.
     */
    public GlobalTagMapId() {
    }

    /**
     * @param globalTagName
     *            the String
     * @param record
     *            the String
     * @param label
     *            the String
     */
    public GlobalTagMapId(String globalTagName, String record, String label) {
        this.globalTagName = globalTagName;
        this.record = record;
        this.label = label;
    }

    /**
     * @return String
     */
    @Column(name = "GLOBAL_TAG_NAME", nullable = false, length = 100)
    public String getGlobalTagName() {
        return this.globalTagName;
    }

    /**
     * @param globalTagName
     *            the String
     * @return
     */
    public void setGlobalTagName(String globalTagName) {
        this.globalTagName = globalTagName;
    }

    /**
     * @return String
     */
    @Column(name = "RECORD", nullable = false, length = 100)
    public String getRecord() {
        return this.record;
    }

    /**
     * @param record
     *            the String
     * @return
     */
    public void setRecord(String record) {
        this.record = record;
    }

    /**
     * @return String
     */
    @Column(name = "LABEL", nullable = false, length = 100)
    public String getLabel() {
        return this.label;
    }

    /**
     * @param label
     *            the String
     * @return
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (!(other instanceof GlobalTagMapId)) {
            return false;
        }
        final GlobalTagMapId castOther = (GlobalTagMapId) other;

        return (this.getGlobalTagName() == castOther.getGlobalTagName()
                || this.getGlobalTagName() != null && castOther.getGlobalTagName() != null
                        && this.getGlobalTagName().equals(castOther.getGlobalTagName()))
                && (this.getRecord() == castOther.getRecord()
                        || this.getRecord() != null && castOther.getRecord() != null
                                && this.getRecord().equals(castOther.getRecord()))
                && (this.getLabel() == castOther.getLabel()
                        || this.getLabel() != null && castOther.getLabel() != null
                                && this.getLabel().equals(castOther.getLabel()));
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int result = 17;

        result = 37 * result
                + (getGlobalTagName() == null ? 0 : this.getGlobalTagName().hashCode());
        result = 37 * result + (getRecord() == null ? 0 : this.getRecord().hashCode());
        result = 37 * result + (getLabel() == null ? 0 : this.getLabel().hashCode());
        return result;
    }

}
