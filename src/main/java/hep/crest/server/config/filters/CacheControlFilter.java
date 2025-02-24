package hep.crest.server.config.filters;

import hep.crest.server.annotations.CacheControlCdb;
import lombok.extern.slf4j.Slf4j;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.annotation.Annotation;

/**
 * @author formica
 */
@Provider
@CacheControlCdb
@Slf4j
public class CacheControlFilter implements ContainerResponseFilter {

    /**
     * The header for Frontier cache control settings.
     */
    private static final String FRONTIER_HEADER_CACHE_CONTROL = "X-Frontier-Control";

    /*
     * (non-Javadoc)
     * 
     * @see
     * jakarta.ws.rs.container.ContainerResponseFilter#filter(jakarta.ws.rs.container.
     * ContainerRequestContext, jakarta.ws.rs.container.ContainerResponseContext)
     */
    @Override
    public void filter(ContainerRequestContext pRequestContext,
            ContainerResponseContext pResponseContext) throws IOException {
        log.debug("CacheControlFilter processing context entity annotations");
        for (final Annotation a : pResponseContext.getEntityAnnotations()) {
            log.debug("Found annotation {}", a);
            if (a.annotationType() == CacheControlCdb.class) {
                final String value = ((CacheControlCdb) a).value();
                log.debug("CacheControl will be set to {}", value);
                pResponseContext.getHeaders().putSingle(HttpHeaders.CACHE_CONTROL, value);

                final String fc = setFrontierCaching(value);
                log.debug("{} will be set to {}", FRONTIER_HEADER_CACHE_CONTROL, fc);
                pResponseContext.getHeaders().putSingle(FRONTIER_HEADER_CACHE_CONTROL, fc);

                break;
            }
        }
    }

    /**
     * @param value
     *            the String
     * @return String
     */
    protected String setFrontierCaching(String value) {
        String frontiercache = "short";
        final String[] params = value.split(",");
        // From Dave Dykstra: X-Frontier-Control: ttl=<value>
        // where <value> is short, long, or forever.
        // Now look for max-age (ma): if ma < 600 then set ttl to short
        // if ma > 600 and ma < 3600*5 then set to long
        // if ma > 3600*5 then set to forever
        for (int i = 0; i < params.length; i++) {
            final String val = params[i].trim();
            log.debug("Examine control cache parameter {}", val);
            if (val.startsWith("max-age")) {
                final String maval = val.split("=")[1];
                final Integer maxAge = Integer.valueOf(maval);
                if (maxAge <= 600) {
                    frontiercache = "short";
                }
                else if (maxAge <= 3600 * 5) {
                    frontiercache = "long";
                }
                else {
                    frontiercache = "forever";
                }
                break;
            }
        }
        return "ttl=" + frontiercache;
    }
}
