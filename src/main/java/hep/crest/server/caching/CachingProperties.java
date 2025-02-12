package hep.crest.server.caching;

import lombok.Data;
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
@Data
public class CachingProperties {
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

}
