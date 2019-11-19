package hep.crest.server.caching;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * The caching properties.
 *
 * @author formica
 *
 */
@Component
@ConfigurationProperties("caching")
public class CachingProperties {

    /**
     * Logger.
     */
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * The default cache in seconds.
     */
    public static final Integer DEFAULT_CACHE_TIME = 60; // number of seconds

    /**
     * The iov group max age.
     */
    private Integer iovsgroupsMaxage = 120; // number of seconds

    /**
     * The iov group snapshot max age.
     */
    private Integer iovsgroupsSnapshotMaxage = 600; // number of seconds

    /**
     * The iov max age.
     */
    private Integer iovsMaxage = 600; // number of seconds

    /**
     * The iov snapshot max age.
     */
    private Integer iovsSnapshotMaxage = 1200;

    /**
     * The payload max age.
     */
    private Integer payloadsMaxage = 1200;

    /**
     * The group size for time based folders.
     */
    private Integer timetypeGroupsize = 1000;

    /**
     * The group size for run based folders.
     */
    private Integer runtypeGroupsize = 10;

    /**
     * @return Integer
     */
    public Integer getIovsgroupsMaxage() {
        log.info("property iovsgroups_maxage has value: {}", iovsgroupsMaxage);
        return iovsgroupsMaxage;
    }

    /**
     * @return Integer
     */
    public Integer getIovsgroupsSnapshotMaxage() {
        return iovsgroupsSnapshotMaxage;
    }

    /**
     * @return Integer
     */
    public Integer getIovsMaxage() {
        return iovsMaxage;
    }

    /**
     * @return Integer
     */
    public Integer getIovsSnapshotMaxage() {
        return iovsSnapshotMaxage;
    }

    /**
     * @return Integer
     */
    public Integer getPayloadsMaxage() {
        return payloadsMaxage;
    }

    /**
     * @return Integer
     */
    public Integer getTimetypeGroupsize() {
        return timetypeGroupsize;
    }

    /**
     * @return Integer
     */
    public Integer getRuntypeGroupsize() {
        return runtypeGroupsize;
    }

    /**
     * @param iovsgroupsmaxage
     *            the Integer
     * @return
     */
    public void setIovsgroupsMaxage(Integer iovsgroupsmaxage) {
        this.iovsgroupsMaxage = iovsgroupsmaxage;
    }

    /**
     * @param iovsgroupssnapshotmaxage
     *            the Integer
     * @return
     */
    public void setIovsgroupsSnapshotMaxage(Integer iovsgroupssnapshotmaxage) {
        this.iovsgroupsSnapshotMaxage = iovsgroupssnapshotmaxage;
    }

    /**
     * @param iovsmaxage
     *            the Integer
     * @return
     */
    public void setIovsMaxage(Integer iovsmaxage) {
        this.iovsMaxage = iovsmaxage;
    }

    /**
     * @param iovssnapshotmaxage
     *            the Integer
     * @return
     */
    public void setIovsSnapshotMaxage(Integer iovssnapshotmaxage) {
        this.iovsSnapshotMaxage = iovssnapshotmaxage;
    }

    /**
     * @param payloadsmaxage
     *            the Integer
     * @return
     */
    public void setPayloadsMaxage(Integer payloadsmaxage) {
        this.payloadsMaxage = payloadsmaxage;
    }

    /**
     * @param timetypegroupsize
     *            the Integer
     * @return
     */
    public void setTimetypeGroupsize(Integer timetypegroupsize) {
        this.timetypeGroupsize = timetypegroupsize;
    }

    /**
     * @param runtypegroupsize
     *            the Integer
     * @return
     */
    public void setRuntypeGroupsize(Integer runtypegroupsize) {
        this.runtypeGroupsize = runtypegroupsize;
    }

}
