package hep.crest.server.swagger.api;

import hep.crest.server.swagger.api.GlobaltagsApiService;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import hep.crest.server.swagger.model.GlobalTagDto;
import hep.crest.server.swagger.model.GlobalTagSetDto;
import hep.crest.server.swagger.model.HTTPResponse;
import hep.crest.server.swagger.model.TagSetDto;

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

@Path("/globaltags")


///// @jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJerseyServerCodegen")
public class GlobaltagsApi  {

   @Autowired
   private GlobaltagsApiService delegate;
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
    @Operation(summary = "Create a GlobalTag in the database.", description = "", responses = {
            @ApiResponse(responseCode = "201", description = "successful operation", content =
                @Content(schema = @Schema(implementation = GlobalTagDto.class))),
            @ApiResponse(responseCode = "200", description = "Generic error response", content =
                @Content(schema = @Schema(implementation = HTTPResponse.class))),
            },security = {
            @SecurityRequirement(name = "OpenID", scopes={ "openid" }),
            @SecurityRequirement(name = "BearerAuth")
        }, tags={ "globaltags", })
    public Response createGlobalTag(@Parameter(description = "force: tell the server if it should use or not the insertion time provided {default: false}", example = "false") @DefaultValue("false") @QueryParam("force")  String force,@Parameter(description = "") @Valid  GlobalTagDto globalTagDto,@Context SecurityContext securityContext)
    throws NotFoundException {
        context.setHttpHeaders(headers);
        context.setRequest(request);
        context.setUriInfo(uriInfo);
        return delegate.createGlobalTag(force, globalTagDto, securityContext);
    }

    @jakarta.ws.rs.GET
    @Path("/{name}")
    @Produces({ "application/json" })
    @Operation(summary = "Finds a GlobalTagDto by name", description = "", responses = {
            @ApiResponse(responseCode = "200", description = "successful operation", content =
                @Content(schema = @Schema(implementation = GlobalTagSetDto.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content =
                @Content(schema = @Schema(implementation = HTTPResponse.class))),
            @ApiResponse(responseCode = "200", description = "Generic error response", content =
                @Content(schema = @Schema(implementation = HTTPResponse.class))),
            },security = {
            @SecurityRequirement(name = "OpenID", scopes={ "openid" }),
            @SecurityRequirement(name = "BearerAuth")
        }, tags={ "globaltags", })
    public Response findGlobalTag(@Parameter(description = "", required = true) @PathParam("name") @NotNull  String name,@Context SecurityContext securityContext)
    throws NotFoundException {
        context.setHttpHeaders(headers);
        context.setRequest(request);
        context.setUriInfo(uriInfo);
        return delegate.findGlobalTag(name, securityContext);
    }

    @jakarta.ws.rs.GET
    @Path("/{name}/tags")
    @Produces({ "application/json" })
    @Operation(summary = "Finds a TagDtos lists associated to the global tag name in input.", description = "", responses = {
            @ApiResponse(responseCode = "200", description = "successful operation", content =
                @Content(schema = @Schema(implementation = TagSetDto.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content =
                @Content(schema = @Schema(implementation = HTTPResponse.class))),
            @ApiResponse(responseCode = "200", description = "Generic error response", content =
                @Content(schema = @Schema(implementation = HTTPResponse.class))),
            },security = {
            @SecurityRequirement(name = "OpenID", scopes={ "openid" }),
            @SecurityRequirement(name = "BearerAuth")
        }, tags={ "globaltags", })
    public Response findGlobalTagFetchTags(@Parameter(description = "", required = true) @PathParam("name") @NotNull  String name,@Parameter(description = "record:  the record string {}", example = "none") @DefaultValue("none") @QueryParam("record")  String record,@Parameter(description = "label:  the label string {}", example = "none") @DefaultValue("none") @QueryParam("label")  String label,@Context SecurityContext securityContext)
    throws NotFoundException {
        context.setHttpHeaders(headers);
        context.setRequest(request);
        context.setUriInfo(uriInfo);
        return delegate.findGlobalTagFetchTags(name, record, label, securityContext);
    }

    @jakarta.ws.rs.GET
    @Produces({ "application/json" })
    @Operation(summary = "Finds a GlobalTagDtos lists.", description = "", responses = {
            @ApiResponse(responseCode = "200", description = "successful operation", content =
                @Content(schema = @Schema(implementation = GlobalTagSetDto.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content =
                @Content(schema = @Schema(implementation = HTTPResponse.class))),
            @ApiResponse(responseCode = "200", description = "Generic error response", content =
                @Content(schema = @Schema(implementation = HTTPResponse.class))),
            },security = {
            @SecurityRequirement(name = "OpenID", scopes={ "openid" }),
            @SecurityRequirement(name = "BearerAuth")
        }, tags={ "globaltags", })
    public Response listGlobalTags(@Parameter(description = "the global tag name search pattern {none}", example = "all") @DefaultValue("all") @QueryParam("name")  String name,@Parameter(description = "the global tag workflow search pattern {none}") @QueryParam("workflow")  String workflow,@Parameter(description = "the global tag scenario search pattern {none}") @QueryParam("scenario")  String scenario,@Parameter(description = "the global tag release search pattern {none}") @QueryParam("release")  String release,@Parameter(description = "the global tag validity low limit {x>=validity}") @QueryParam("validity")  Long validity,@Parameter(description = "the global tag description search pattern {none}") @QueryParam("description")  String description,@Parameter(description = "page: the page number {0}", example = "0") @DefaultValue("0") @QueryParam("page")  Integer page,@Parameter(description = "size: the page size {1000}", example = "1000") @DefaultValue("1000") @QueryParam("size")  Integer size,@Parameter(description = "sort: the sort pattern {name:ASC}", example = "name:ASC") @DefaultValue("name:ASC") @QueryParam("sort")  String sort,@Context SecurityContext securityContext)
    throws NotFoundException {
        context.setHttpHeaders(headers);
        context.setRequest(request);
        context.setUriInfo(uriInfo);
        return delegate.listGlobalTags(name, workflow, scenario, release, validity, description, page, size, sort, securityContext);
    }
}
