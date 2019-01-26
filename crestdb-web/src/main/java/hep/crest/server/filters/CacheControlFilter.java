package hep.crest.server.filters;

import java.io.IOException;
import java.lang.annotation.Annotation;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hep.crest.server.annotations.CacheControlCdb;

@Provider
@CacheControlCdb
public class CacheControlFilter implements ContainerResponseFilter {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private static final String FRONTIER_HEADER_CACHE_CONTROL = "X-Frontier-Control";
	
	
    /* (non-Javadoc)
     * @see javax.ws.rs.container.ContainerResponseFilter#filter(javax.ws.rs.container.ContainerRequestContext, javax.ws.rs.container.ContainerResponseContext)
     */
    @Override
    public void filter(ContainerRequestContext pRequestContext, ContainerResponseContext pResponseContext)
            throws IOException {
        for (Annotation a : pResponseContext.getEntityAnnotations()) {
            if (a.annotationType() == CacheControlCdb.class) {
                String value = ((CacheControlCdb) a).value();
                log.debug("CacheControl will be set to {}",value);
                pResponseContext.getHeaders().putSingle(HttpHeaders.CACHE_CONTROL, value);

                String fc = setFrontierCaching(value);
                log.debug("{} will be set to {}",FRONTIER_HEADER_CACHE_CONTROL,fc);
                pResponseContext.getHeaders().putSingle(FRONTIER_HEADER_CACHE_CONTROL, fc);
                
                break;
            }
        }
    }

    /**
     * @param value
     * @return
     */
    protected String setFrontierCaching(String value) {
    	String frontiercache = "short";
        String[] params = value.split(",");
        // From Dave Dykstra:  X-Frontier-Control: ttl=<value>
        // where <value> is short, long, or forever.
        // Now look for max-age (ma): if ma < 600 then set ttl to short
        //                       	  if ma > 600 and ma < 3600*5 then set to long
        //							  if ma > 3600*5 then set to forever
        for (int i=0; i<params.length; i++) {
        	String val = params[i].trim();
        	log.debug("Examine control cache parameter {}",val);
        	if (val.startsWith("max-age")) {
        		String maval = val.split("=")[1];
        		Integer maxAge = new Integer(maval);
        		if (maxAge<=600) {
        			frontiercache = "short";
        		} else if (maxAge<=(3600*5)) {
        			frontiercache = "long";
        		} else {
        			frontiercache = "forever";
        		}
                break;
        	}
        }
        return "ttl="+frontiercache;
    }
}