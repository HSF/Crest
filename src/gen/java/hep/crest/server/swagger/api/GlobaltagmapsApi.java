package hep.crest.server.swagger.api;

import hep.crest.server.swagger.api.GlobaltagmapsApiService;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import hep.crest.server.swagger.model.GlobalTagMapDto;
import hep.crest.server.swagger.model.GlobalTagMapSetDto;
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

@Path("/globaltagmaps")


///// @jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJerseyServerCodegen")
public class GlobaltagmapsApi  {

   @Autowired
   private GlobaltagmapsApiService delegate;
   @Context
   protected Request request;
   @Context
   protected HttpHeaders headers;
   @Context
   protected UriInfo uriInfo;
   @Autowired
   protected JAXRSContext context;




    @jakarta.ws.rs.POST
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @Operation(summary = "Create a GlobalTagMap in the database.", description = "", responses = {
            @ApiResponse(responseCode = "201", description = "successful operation", content =
                @Content(schema = @Schema(implementation = GlobalTagMapDto.class))),
            @ApiResponse(responseCode = "200", description = "Generic error response", content =
                @Content(schema = @Schema(implementation = HTTPResponse.class))),
            },security = {
            @SecurityRequirement(name = "OpenID", scopes={ "openid" }),
            @SecurityRequirement(name = "BearerAuth")
        }, tags={ "globaltagmaps", })
    public Response createGlobalTagMap(@Parameter(description = "") @Valid  GlobalTagMapDto globalTagMapDto,@Context SecurityContext securityContext)
    throws NotFoundException {
        context.setHttpHeaders(headers);
        context.setRequest(request);
        context.setUriInfo(uriInfo);
        return delegate.createGlobalTagMap(globalTagMapDto, securityContext);
    }

    @jakarta.ws.rs.DELETE
    @Path("/{name}")
    @Produces({ "application/json" })
    @Operation(summary = "Delete GlobalTagMapDto lists.", description = "", responses = {
            @ApiResponse(responseCode = "200", description = "successful operation", content =
                @Content(schema = @Schema(implementation = GlobalTagMapSetDto.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content =
                @Content(schema = @Schema(implementation = HTTPResponse.class))),
            @ApiResponse(responseCode = "200", description = "Generic error response", content =
                @Content(schema = @Schema(implementation = HTTPResponse.class))),
            },security = {
            @SecurityRequirement(name = "OpenID", scopes={ "openid" }),
            @SecurityRequirement(name = "BearerAuth")
        }, tags={ "globaltagmaps", })
    public Response deleteGlobalTagMap(@Parameter(description = "the global tag name", required = true) @PathParam("name") @NotNull  String name,@Parameter(description = "label: the generic name labelling all tags of a certain kind.", required = true, example = "none") @DefaultValue("none") @QueryParam("label") @NotNull  String label,@Parameter(description = "tagname: the name of the tag associated.", required = true, example = "none") @DefaultValue("none") @QueryParam("tagname") @NotNull  String tagname,@Parameter(description = "record: the record.") @QueryParam("record")  String record,@Context SecurityContext securityContext)
    throws NotFoundException {
        context.setHttpHeaders(headers);
        context.setRequest(request);
        context.setUriInfo(uriInfo);
        return delegate.deleteGlobalTagMap(name, label, tagname, record, securityContext);
    }

    @jakarta.ws.rs.GET
    @Path("/{name}")
    @Produces({ "application/json" })
    @Operation(summary = "Find GlobalTagMapDto lists.", description = "", responses = {
            @ApiResponse(responseCode = "200", description = "successful operation", content =
                @Content(schema = @Schema(implementation = GlobalTagMapSetDto.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content =
                @Content(schema = @Schema(implementation = HTTPResponse.class))),
            @ApiResponse(responseCode = "200", description = "Generic error response", content =
                @Content(schema = @Schema(implementation = HTTPResponse.class))),
            },security = {
            @SecurityRequirement(name = "OpenID", scopes={ "openid" }),
            @SecurityRequirement(name = "BearerAuth")
        }, tags={ "globaltagmaps", })
    public Response findGlobalTagMap(@Parameter(description = "", required = true) @PathParam("name") @NotNull  String name,@Parameter(description = "If the mode is BackTrace then it will search for global tags containing the tag <name>" , example="Trace")@HeaderParam("X-Crest-MapMode") String xCrestMapMode,@Context SecurityContext securityContext)
    throws NotFoundException {
        context.setHttpHeaders(headers);
        context.setRequest(request);
        context.setUriInfo(uriInfo);
        return delegate.findGlobalTagMap(name, xCrestMapMode, securityContext);
    }
}
