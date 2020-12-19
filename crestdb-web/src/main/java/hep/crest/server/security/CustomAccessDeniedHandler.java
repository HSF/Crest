package hep.crest.server.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import hep.crest.server.swagger.api.ApiResponseMessage;
import hep.crest.swagger.model.HTTPResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
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
        final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,
                "Access denied to protected URL using " + request.getMethod());
        HTTPResponse httpresp =
                new HTTPResponse().code(Response.Status.FORBIDDEN.getStatusCode())
                        .message(resp.getMessage()).action(request.getMethod() + " " + request.getRequestURI());
        OutputStream out = response.getOutputStream();
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(out, httpresp);
        out.flush();
    }
}
