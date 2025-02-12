package hep.crest.server.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import hep.crest.server.swagger.model.HTTPResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.io.OutputStream;

public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(CustomAccessDeniedHandler.class);

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException exc) throws IOException {

        Authentication auth
                = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            log.warn("User: {} attempted to access the protected URL: {}", auth.getName(), request.getRequestURI());
        }
        log.warn("Redirect to accessDenied URL");
        String message = "Access denied to protected URL using " + request.getMethod();
        HTTPResponse httpresp =
                new HTTPResponse().code(Response.Status.FORBIDDEN.getStatusCode())
                        .error(Response.Status.FORBIDDEN.getReasonPhrase())
                        .type("ACCESS_ERROR")
                        .message(message).id(request.getRequestURI());
        OutputStream out = response.getOutputStream();
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(out, httpresp);
        out.flush();
    }
}
