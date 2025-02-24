package hep.crest.server.swagger.api;

import hep.crest.server.swagger.api.AdminApiService;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import hep.crest.server.swagger.model.GlobalTagDto;
import hep.crest.server.swagger.model.HTTPResponse;

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

@Path("/admin")


///// @jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJerseyServerCodegen")
public class AdminApi  {

   @Autowired
   private AdminApiService delegate;
   @Context
   protected Request request;
   @Context
   protected HttpHeaders headers;
   @Context
   protected UriInfo uriInfo;
   @Autowired
   protected JAXRSContext context;




    @jakarta.ws.rs.DELETE
    @Path("/globaltags/{name}")
    @Produces({ "application/json" })
    @Operation(summary = "Remove a GlobalTag from the database.", description = "", responses = {
            @ApiResponse(responseCode = "200", description = "successful operation", content =
                @Content(schema = @Schema(implementation = Void.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content =
                @Content(schema = @Schema(implementation = HTTPResponse.class))),
            @ApiResponse(responseCode = "200", description = "Generic error response", content =
                @Content(schema = @Schema(implementation = HTTPResponse.class))),
            },security = {
            @SecurityRequirement(name = "OpenID", scopes={ "openid" }),
            @SecurityRequirement(name = "BearerAuth")
        }, tags={ "admin", })
    public Response removeGlobalTag(@Parameter(description = "", required = true) @PathParam("name") @NotNull  String name,@Context SecurityContext securityContext)
    throws NotFoundException {
        context.setHttpHeaders(headers);
        context.setRequest(request);
        context.setUriInfo(uriInfo);
        return delegate.removeGlobalTag(name, securityContext);
    }

    @jakarta.ws.rs.DELETE
    @Path("/tags/{name}")
    @Produces({ "application/json" })
    @Operation(summary = "Remove a Tag from the database.", description = "", responses = {
            @ApiResponse(responseCode = "200", description = "successful operation", content =
                @Content(schema = @Schema(implementation = Void.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content =
                @Content(schema = @Schema(implementation = HTTPResponse.class))),
            @ApiResponse(responseCode = "200", description = "Generic error response", content =
                @Content(schema = @Schema(implementation = HTTPResponse.class))),
            },security = {
            @SecurityRequirement(name = "OpenID", scopes={ "openid" }),
            @SecurityRequirement(name = "BearerAuth")
        }, tags={ "admin", })
    public Response removeTag(@Parameter(description = "", required = true) @PathParam("name") @NotNull  String name,@Context SecurityContext securityContext)
    throws NotFoundException {
        context.setHttpHeaders(headers);
        context.setRequest(request);
        context.setUriInfo(uriInfo);
        return delegate.removeTag(name, securityContext);
    }

    @jakarta.ws.rs.PUT
    @Path("/globaltags/{name}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @Operation(summary = "Update a GlobalTag in the database.", description = "", responses = {
            @ApiResponse(responseCode = "200", description = "successful operation", content =
                @Content(schema = @Schema(implementation = GlobalTagDto.class))),
            },security = {
            @SecurityRequirement(name = "OpenID", scopes={ "openid" }),
            @SecurityRequirement(name = "BearerAuth")
        }, tags={ "admin", })
    public Response updateGlobalTag(@Parameter(description = "", required = true) @PathParam("name") @NotNull  String name,@Parameter(description = "") @Valid  GlobalTagDto globalTagDto,@Context SecurityContext securityContext)
    throws NotFoundException {
        context.setHttpHeaders(headers);
        context.setRequest(request);
        context.setUriInfo(uriInfo);
        return delegate.updateGlobalTag(name, globalTagDto, securityContext);
    }
}
