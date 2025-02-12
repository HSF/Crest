package hep.crest.server.config.filters;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;

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
     * jakarta.ws.rs.container.ContainerResponseFilter#filter(jakarta.ws.rs.container.
     * ContainerRequestContext, jakarta.ws.rs.container.ContainerResponseContext)
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
