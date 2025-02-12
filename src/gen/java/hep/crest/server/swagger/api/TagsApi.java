package hep.crest.server.swagger.api;

import hep.crest.server.swagger.api.TagsApiService;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import hep.crest.server.swagger.model.GenericMap;
import hep.crest.server.swagger.model.HTTPResponse;
import hep.crest.server.swagger.model.TagDto;
import hep.crest.server.swagger.model.TagMetaDto;
import hep.crest.server.swagger.model.TagMetaSetDto;
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

@Path("/tags")


///// @jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJerseyServerCodegen")
public class TagsApi  {

   @Autowired
   private TagsApiService delegate;
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
    @Operation(summary = "Create a Tag in the database.", description = "", responses = {
            @ApiResponse(responseCode = "200", description = "successful operation", content =
                @Content(schema = @Schema(implementation = TagDto.class))),
            @ApiResponse(responseCode = "200", description = "Generic error response", content =
                @Content(schema = @Schema(implementation = HTTPResponse.class))),
            },security = {
            @SecurityRequirement(name = "OpenID", scopes={ "openid" }),
            @SecurityRequirement(name = "BearerAuth")
        }, tags={ "tags", })
    public Response createTag(@Parameter(description = "") @Valid  TagDto tagDto,@Context SecurityContext securityContext)
    throws NotFoundException {
        context.setHttpHeaders(headers);
        context.setRequest(request);
        context.setUriInfo(uriInfo);
        return delegate.createTag(tagDto, securityContext);
    }

    @jakarta.ws.rs.POST
    @Path("/{name}/meta")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @Operation(summary = "Create a TagMeta in the database.", description = "", responses = {
            @ApiResponse(responseCode = "200", description = "successful operation", content =
                @Content(schema = @Schema(implementation = TagMetaDto.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content =
                @Content(schema = @Schema(implementation = HTTPResponse.class))),
            @ApiResponse(responseCode = "200", description = "Generic error response", content =
                @Content(schema = @Schema(implementation = HTTPResponse.class))),
            },security = {
            @SecurityRequirement(name = "OpenID", scopes={ "openid" }),
            @SecurityRequirement(name = "BearerAuth")
        }, tags={ "tags", })
    public Response createTagMeta(@Parameter(description = "name: the tag name", required = true) @PathParam("name") @NotNull  String name,@Parameter(description = "") @Valid  TagMetaDto tagMetaDto,@Context SecurityContext securityContext)
    throws NotFoundException {
        context.setHttpHeaders(headers);
        context.setRequest(request);
        context.setUriInfo(uriInfo);
        return delegate.createTagMeta(name, tagMetaDto, securityContext);
    }

    @jakarta.ws.rs.GET
    @Path("/{name}")
    @Produces({ "application/json" })
    @Operation(summary = "Finds a TagDto by name", description = "", responses = {
            @ApiResponse(responseCode = "200", description = "successful operation", content =
                @Content(schema = @Schema(implementation = TagSetDto.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content =
                @Content(schema = @Schema(implementation = HTTPResponse.class))),
            @ApiResponse(responseCode = "200", description = "Generic error response", content =
                @Content(schema = @Schema(implementation = HTTPResponse.class))),
            },security = {
            @SecurityRequirement(name = "OpenID", scopes={ "openid" }),
            @SecurityRequirement(name = "BearerAuth")
        }, tags={ "tags", })
    public Response findTag(@Parameter(description = "name: the tag name", required = true) @PathParam("name") @NotNull  String name,@Context SecurityContext securityContext)
    throws NotFoundException {
        context.setHttpHeaders(headers);
        context.setRequest(request);
        context.setUriInfo(uriInfo);
        return delegate.findTag(name, securityContext);
    }

    @jakarta.ws.rs.GET
    @Path("/{name}/meta")
    @Produces({ "application/json" })
    @Operation(summary = "Finds a TagMetaDto by name", description = "", responses = {
            @ApiResponse(responseCode = "200", description = "successful operation", content =
                @Content(schema = @Schema(implementation = TagMetaSetDto.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content =
                @Content(schema = @Schema(implementation = HTTPResponse.class))),
            @ApiResponse(responseCode = "200", description = "Generic error response", content =
                @Content(schema = @Schema(implementation = HTTPResponse.class))),
            },security = {
            @SecurityRequirement(name = "OpenID", scopes={ "openid" }),
            @SecurityRequirement(name = "BearerAuth")
        }, tags={ "tags", })
    public Response findTagMeta(@Parameter(description = "name: the tag name", required = true) @PathParam("name") @NotNull  String name,@Context SecurityContext securityContext)
    throws NotFoundException {
        context.setHttpHeaders(headers);
        context.setRequest(request);
        context.setUriInfo(uriInfo);
        return delegate.findTagMeta(name, securityContext);
    }

    @jakarta.ws.rs.GET
    @Produces({ "application/json" })
    @Operation(summary = "Finds a TagDtos lists.", description = "", responses = {
            @ApiResponse(responseCode = "200", description = "successful operation", content =
                @Content(schema = @Schema(implementation = TagSetDto.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content =
                @Content(schema = @Schema(implementation = HTTPResponse.class))),
            @ApiResponse(responseCode = "200", description = "Generic error response", content =
                @Content(schema = @Schema(implementation = HTTPResponse.class))),
            },security = {
            @SecurityRequirement(name = "OpenID", scopes={ "openid" }),
            @SecurityRequirement(name = "BearerAuth")
        }, tags={ "tags", })
    public Response listTags(@Parameter(description = "the tag name search pattern {all}", example = "all") @DefaultValue("all") @QueryParam("name")  String name,@Parameter(description = "the tag timeType {none}") @QueryParam("timeType")  String timeType,@Parameter(description = "the tag objectType search pattern {none}") @QueryParam("objectType")  String objectType,@Parameter(description = "the global tag description search pattern {none}") @QueryParam("description")  String description,@Parameter(description = "page: the page number {0}", example = "0") @DefaultValue("0") @QueryParam("page")  Integer page,@Parameter(description = "size: the page size {1000}", example = "1000") @DefaultValue("1000") @QueryParam("size")  Integer size,@Parameter(description = "sort: the sort pattern {name:ASC}", example = "name:ASC") @DefaultValue("name:ASC") @QueryParam("sort")  String sort,@Context SecurityContext securityContext)
    throws NotFoundException {
        context.setHttpHeaders(headers);
        context.setRequest(request);
        context.setUriInfo(uriInfo);
        return delegate.listTags(name, timeType, objectType, description, page, size, sort, securityContext);
    }

    @jakarta.ws.rs.PUT
    @Path("/{name}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @Operation(summary = "Update a TagDto by name", description = "", responses = {
            @ApiResponse(responseCode = "200", description = "successful operation", content =
                @Content(schema = @Schema(implementation = TagDto.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content =
                @Content(schema = @Schema(implementation = HTTPResponse.class))),
            @ApiResponse(responseCode = "200", description = "Generic error response", content =
                @Content(schema = @Schema(implementation = HTTPResponse.class))),
            },security = {
            @SecurityRequirement(name = "OpenID", scopes={ "openid" }),
            @SecurityRequirement(name = "BearerAuth")
        }, tags={ "tags", })
    public Response updateTag(@Parameter(description = "name: the tag name", required = true) @PathParam("name") @NotNull  String name,@Parameter(description = "") @Valid  GenericMap genericMap,@Context SecurityContext securityContext)
    throws NotFoundException {
        context.setHttpHeaders(headers);
        context.setRequest(request);
        context.setUriInfo(uriInfo);
        return delegate.updateTag(name, genericMap, securityContext);
    }

    @jakarta.ws.rs.PUT
    @Path("/{name}/meta")
    @Consumes({ "application/json" })
    @Produces({ "application/json", "application/xml" })
    @Operation(summary = "Update a TagMetaDto by name", description = "", responses = {
            @ApiResponse(responseCode = "200", description = "successful operation", content =
                @Content(schema = @Schema(implementation = TagMetaDto.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content =
                @Content(schema = @Schema(implementation = HTTPResponse.class))),
            @ApiResponse(responseCode = "200", description = "Generic error response", content =
                @Content(schema = @Schema(implementation = HTTPResponse.class))),
            },security = {
            @SecurityRequirement(name = "OpenID", scopes={ "openid" }),
            @SecurityRequirement(name = "BearerAuth")
        }, tags={ "tags", })
    public Response updateTagMeta(@Parameter(description = "name: the tag name", required = true) @PathParam("name") @NotNull  String name,@Parameter(description = "") @Valid  GenericMap genericMap,@Context SecurityContext securityContext)
    throws NotFoundException {
        context.setHttpHeaders(headers);
        context.setRequest(request);
        context.setUriInfo(uriInfo);
        return delegate.updateTagMeta(name, genericMap, securityContext);
    }
}
