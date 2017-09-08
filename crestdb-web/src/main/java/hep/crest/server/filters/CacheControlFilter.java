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

	private static String frontier_cache_header_control = "X-Frontier-Control";
	
	
    @Override
    public void filter(ContainerRequestContext pRequestContext, ContainerResponseContext pResponseContext)
            throws IOException {
        for (Annotation a : pResponseContext.getEntityAnnotations()) {
            if (a.annotationType() == CacheControlCdb.class) {
                String value = ((CacheControlCdb) a).value();
                log.debug("CacheControl will be set to "+value);
                pResponseContext.getHeaders().putSingle(HttpHeaders.CACHE_CONTROL, value);

                String fc = setFrontierCaching(value);
                log.debug(frontier_cache_header_control+" will be set to "+fc);
                pResponseContext.getHeaders().putSingle(frontier_cache_header_control, fc);
                
                break;
            }
        }
    }

    protected String setFrontierCaching(String value) {
    	String frontier_cache = "short";
        String[] params = value.split(",");
        // From Dave Dykstra:  X-Frontier-Control: ttl=<value>
        // where <value> is short, long, or forever.
        // Now look for max-age (ma): if ma < 600 then set ttl to short
        //                       	  if ma > 600 and ma < 3600*5 then set to long
        //							  if ma > 3600*5 then set to forever
        for (int i=0; i<params.length; i++) {
        	String val = params[i].trim();
        	log.debug("Examine control cache parameter "+val);
        	if (val.startsWith("max-age")) {
        		String maval = val.split("=")[1];
        		Integer maxAge = new Integer(maval);
        		if (maxAge<=600) {
        			frontier_cache = "short";
        		} else if (maxAge<=(3600*5)) {
        			frontier_cache = "long";
        		} else {
        			frontier_cache = "forever";
        		}
                break;
        	}
        }
        return "ttl="+frontier_cache;
    }
}