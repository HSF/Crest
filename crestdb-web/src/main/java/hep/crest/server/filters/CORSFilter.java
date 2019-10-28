package hep.crest.server.filters;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

/**
 * @author formica
 *
 */
@Provider
public class CORSFilter implements ContainerResponseFilter {

    // Remember to add X-Crest-PayloadFormat to origin filter.
    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.ws.rs.container.ContainerResponseFilter#filter(javax.ws.rs.container.
     * ContainerRequestContext, javax.ws.rs.container.ContainerResponseContext)
     */
    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response)
            throws IOException {
        response.getHeaders().add("Access-Control-Allow-Origin", "*");
        response.getHeaders().add("Access-Control-Allow-Headers",
                "origin, content-type, accept, authorization, X-Crest-PayloadFormat");
        response.getHeaders().add("Access-Control-Allow-Credentials", "true");
        response.getHeaders().add("Access-Control-Allow-Methods",
                "GET, POST, PUT, DELETE, OPTIONS, HEAD");
    }
}
