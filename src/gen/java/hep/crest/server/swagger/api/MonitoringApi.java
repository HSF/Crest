package hep.crest.server.swagger.api;

import hep.crest.server.swagger.api.MonitoringApiService;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import hep.crest.server.swagger.model.HTTPResponse;
import hep.crest.server.swagger.model.PayloadTagInfoSetDto;

import java.util.Map;
import java.util.List;
import hep.crest.server.swagger.api.NotFoundException;

import java.io.InputStream;

import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import hep.crest.server.swagger.impl.JAXRSContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Request;

import jakarta.servlet.ServletConfig;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.*;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

@Path("/monitoring/payloads")


///// @jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJerseyServerCodegen")
public class MonitoringApi  {

   @Autowired
   private MonitoringApiService delegate;
   @Context
   protected Request request;
   @Context
   protected HttpHeaders headers;
   @Context
   protected UriInfo uriInfo;
   @Autowired
   protected JAXRSContext context;




    @jakarta.ws.rs.GET
    @Produces({ "application/json" })
    @Operation(summary = "Retrieves monitoring information on payload as a list of PayloadTagInfoDtos.", description = "", responses = {
            @ApiResponse(responseCode = "200", description = "successful operation", content =
                @Content(schema = @Schema(implementation = PayloadTagInfoSetDto.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content =
                @Content(schema = @Schema(implementation = HTTPResponse.class))),
            @ApiResponse(responseCode = "200", description = "Generic error response", content =
                @Content(schema = @Schema(implementation = HTTPResponse.class))),
            },security = {
            @SecurityRequirement(name = "OpenID", scopes={ "openid" }),
            @SecurityRequirement(name = "BearerAuth")
        }, tags={ "monitoring", })
    public Response listPayloadTagInfo(@Parameter(description = "tagname: the search pattern {none}", example = "none") @DefaultValue("none") @QueryParam("tagname")  String tagname,@Context SecurityContext securityContext)
    throws NotFoundException {
        context.setHttpHeaders(headers);
        context.setRequest(request);
        context.setUriInfo(uriInfo);
        return delegate.listPayloadTagInfo(tagname, securityContext);
    }
}
