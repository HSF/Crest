package hep.crest.server.data.pojo;
// Generated Aug 2, 2016 3:50:25 PM by Hibernate Tools 3.2.2.GA

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigInteger;
import java.util.Date;

/**
 * IovId generated by hbm2java.
 */
// This object represents an ID and it is embedded in Iov.
// Define default constructors.
// Define equals and hashcode methods.
// These are essential for ID comparison.
@Embeddable
@Data
@Accessors(chain = true)
public class IovId implements java.io.Serializable {

    /**
     * Serializer.
     */
    private static final long serialVersionUID = -2770785371714771938L;
    /**
     * The tag name.
     * This column should reference an existing Tag.
     */
    @Column(name = "TAG_NAME", nullable = false, length = 255)
    private String tagName;
    /**
     * The since time.
     * The concept of time is flexible: can be any kind of Long.
     */
    @Column(name = "SINCE", nullable = false, precision = 22, scale = 0)
    private BigInteger since;
    /**
     * The insertion time.
     * This time is used to determine Iovs which were inserted before a snapshot time.
     */
    @Column(name = "INSERTION_TIME", nullable = false, length = 11)
    @EqualsAndHashCode.Exclude
    private Date insertionTime;
}
