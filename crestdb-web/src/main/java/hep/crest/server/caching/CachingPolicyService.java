package hep.crest.server.caching;

import hep.crest.data.config.CrestProperties;
import hep.crest.data.pojo.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.math.BigDecimal;
import java.util.Date;

/**
 * A class to get the cache control.
 *
 * @author formica
 *
 */
@Component
public class CachingPolicyService {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(CachingPolicyService.class);

    /**
     * Properties.
     */
    @Autowired
    private CachingProperties cprops;

    /**
     * @param snapshot
     *            the Long
     * @return CacheControl
     */
    public CacheControl getGroupsCacheControl(Long snapshot) {
        Integer maxage = CachingProperties.DEFAULT_CACHE_TIME;
        if (snapshot != 0L) {
            maxage = cprops.getIovsgroupsSnapshotMaxage();
        }
        final CacheControl cc = new CacheControl();
        cc.setMaxAge(maxage);
        return cc;
    }

    /**
     * @param snapshot
     *            the Long
     * @param until
     *            the BigDecimal
     * @return CacheControl
     */
    public CacheControl getIovsCacheControlForUntil(Long snapshot, BigDecimal until) {
        Integer maxage = CachingProperties.DEFAULT_CACHE_TIME;
        if (!until.equals(CrestProperties.INFINITY)) {
            if (snapshot != 0L) {
                maxage = cprops.getIovsSnapshotMaxage();
            }
            else {
                maxage = cprops.getIovsMaxage();
            }
        }
        final CacheControl cc = new CacheControl();
        cc.setMaxAge(maxage);
        return cc;
    }

    /**
     * @param request
     *            the Request
     * @param tagentity
     *            the Tag
     * @return ResponseBuilder
     */
    public ResponseBuilder verifyLastModified(Request request, Tag tagentity) {
        final Date lastModified = tagentity.getModificationTime();
        log.debug("Use tag modification time {}", lastModified);
        final ResponseBuilder builder = request.evaluatePreconditions(lastModified);
        if (builder != null) {
            final CacheControl cc = new CacheControl();
            builder.cacheControl(cc).header("Last-Modified", lastModified); // add
                                                                            // metadata
        }
        return builder;
    }

}
