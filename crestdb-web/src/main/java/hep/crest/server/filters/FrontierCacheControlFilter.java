package hep.crest.server.filters;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hep.crest.server.annotations.CacheControlFrontier;

/**
 * 
 * @author aformic
 *
 * Filter class to modify the header of the response in case of an annotation
 * in the method : @CacheControlFrontier
 * In this case the Frontier header is added using the max-age parameter
 * which is found in the Cache-Control header.
 */
@Provider
@CacheControlFrontier
public class FrontierCacheControlFilter implements ContainerResponseFilter {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private static final String FRONTIER_HEADER_CACHE_CONTROL = "X-Frontier-Control";
	
	
    /* (non-Javadoc)
     * @see javax.ws.rs.container.ContainerResponseFilter#filter(javax.ws.rs.container.ContainerRequestContext, javax.ws.rs.container.ContainerResponseContext)
     */
    @Override
    public void filter(ContainerRequestContext pRequestContext, ContainerResponseContext pResponseContext)
            throws IOException {
        for (Annotation a : pResponseContext.getEntityAnnotations()) {
            if (a.annotationType() == CacheControlFrontier.class) {
                String value = ((CacheControlFrontier) a).value();
                log.debug("CacheControl for Frontier has default set to {}",value);
                List<Object> cachecontrollist = pResponseContext.getHeaders().get(HttpHeaders.CACHE_CONTROL);
                if (cachecontrollist == null) {
                	return;
                }
                for (Object object : cachecontrollist) {
					log.debug("found obj {}",object);
					String val = object.toString();
					if (val.contains("max-age")) {
		                String fc = setFrontierCaching(val);

		                log.debug("{} will be set to {}",FRONTIER_HEADER_CACHE_CONTROL,fc);
		                pResponseContext.getHeaders().putSingle(FRONTIER_HEADER_CACHE_CONTROL, fc);
		                break;
					}
				}             
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