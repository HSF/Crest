package hep.crest.server.data.pojo;
// Generated Aug 2, 2016 3:50:25 PM by Hibernate Tools 3.2.2.GA

import hep.crest.server.config.DatabasePropertyConfigurator;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Tag generated by hbm2java.
 */
@Entity
@Table(name = "TAG", schema = DatabasePropertyConfigurator.SCHEMA_NAME)
// This object represents a Tag.
// Define default constructors.
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class Tag implements java.io.Serializable {

    /**
     * Serializer.
     */
    private static final long serialVersionUID = -7205518190608667851L;
    /**
     * The tag name.
     */
    @Id
    @Column(name = "NAME", unique = true, nullable = false, length = 255)
    private String name;
    /**
     * The time type.
     */
    @Column(name = "TIME_TYPE", nullable = false, length = 16)
    private String timeType;
    /**
     * The object type.
     */
    @Column(name = "OBJECT_TYPE", nullable = false, length = 4000)
    private String objectType;
    /**
     * The synchronization.
     */
    @Column(name = "SYNCHRONIZATION", nullable = false, length = 20)
    private String synchronization = TagSynchroEnum.NONE.type();
    /**
     * The description.
     */
    @Column(name = "DESCRIPTION", nullable = false, length = 4000)
    private String description;
    /**
     * The last validated time.
     */
    @Column(name = "LAST_VALIDATED_TIME", nullable = false, precision = 22, scale = 0)
    private BigInteger lastValidatedTime = BigInteger.ZERO;
    /**
     * The end of validity.
     */
    @Column(name = "END_OF_VALIDITY", nullable = false, precision = 22, scale = 0)
    private BigInteger endOfValidity = BigInteger.ZERO;
    /**
     * The insertion time.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "INSERTION_TIME", nullable = false, updatable = true, length = 11)
    @EqualsAndHashCode.Exclude
    private Date insertionTime;
    /**
     * The modification time.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "MODIFICATION_TIME", nullable = false, length = 11)
    @EqualsAndHashCode.Exclude
    private Date modificationTime;
    /**
     * The mapping with global tags.
     * We exclude them from equals and hashcode methods, as well as toString method.
     */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "tag")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<GlobalTagMap> globalTagMaps = new HashSet<>(0);

    /**
     * Before saving.
     *
     * @return
     */
    @PrePersist
    public void prePersist() {
        if (this.insertionTime == null) {
            final Timestamp now = Timestamp.from(Instant.now());
            this.insertionTime = now;
            this.modificationTime = now;
        }
    }

    /**
     * Before updating.
     *
     * @return
     */
    @PreUpdate
    public void preUpdate() {
        final Timestamp now = Timestamp.from(Instant.now());
        this.modificationTime = now;
    }
}
