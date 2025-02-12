package hep.crest.server.swagger.api;

import hep.crest.server.swagger.api.PayloadsApiService;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.io.File;
import hep.crest.server.swagger.model.GenericMap;
import hep.crest.server.swagger.model.HTTPResponse;
import hep.crest.server.swagger.model.PayloadDto;
import hep.crest.server.swagger.model.PayloadSetDto;
import hep.crest.server.swagger.model.StoreSetDto;

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

@Path("/payloads")


///// @jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJerseyServerCodegen")
public class PayloadsApi  {

   @Autowired
   private PayloadsApiService delegate;
   @Context
   protected Request request;
   @Context
   protected HttpHeaders headers;
   @Context
   protected UriInfo uriInfo;
   @Autowired
   protected JAXRSContext context;




    @jakarta.ws.rs.GET
    @Path("/data")
    @Produces({ "application/octet-stream", "application/json" })
    @Operation(summary = "Finds a payload resource associated to the hash.", description = "", responses = {
            @ApiResponse(responseCode = "200", description = "successful operation", content =
                @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content =
                @Content(schema = @Schema(implementation = HTTPResponse.class))),
            @ApiResponse(responseCode = "200", description = "Generic error response", content =
                @Content(schema = @Schema(implementation = HTTPResponse.class))),
            },security = {
            @SecurityRequirement(name = "OpenID", scopes={ "openid" }),
            @SecurityRequirement(name = "BearerAuth")
        }, tags={ "payloads", })
    public Response getPayload(@Parameter(description = "hash:  the hash of the payload", required = true) @QueryParam("hash") @NotNull  String hash,@Parameter(description = "The format of the output data.  It can be : BLOB (default), META (meta data) or STREAMER (streamerInfo). ", required = true, schema=@Schema(allowableValues={
"BLOB", "META", "STREAMER"
})
, example = "BLOB") @DefaultValue("BLOB") @QueryParam("format") @NotNull  String format,@Context SecurityContext securityContext)
    throws NotFoundException {
        context.setHttpHeaders(headers);
        context.setRequest(request);
        context.setUriInfo(uriInfo);
        return delegate.getPayload(hash, format, securityContext);
    }

    @jakarta.ws.rs.GET
    @Produces({ "application/json" })
    @Operation(summary = "Finds Payloads metadata.", description = "", responses = {
            @ApiResponse(responseCode = "200", description = "successful operation", content =
                @Content(schema = @Schema(implementation = PayloadSetDto.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content =
                @Content(schema = @Schema(implementation = HTTPResponse.class))),
            @ApiResponse(responseCode = "200", description = "Generic error response", content =
                @Content(schema = @Schema(implementation = HTTPResponse.class))),
            },security = {
            @SecurityRequirement(name = "OpenID", scopes={ "openid" }),
            @SecurityRequirement(name = "BearerAuth")
        }, tags={ "payloads", })
    public Response listPayloads(@Parameter(description = "the hash to search {none}") @QueryParam("hash")  String hash,@Parameter(description = "the objectType to search") @QueryParam("objectType")  String objectType,@Parameter(description = "the minimum size to search") @QueryParam("minsize")  Integer minsize,@Parameter(description = "page: the page number {0}", example = "0") @DefaultValue("0") @QueryParam("page")  Integer page,@Parameter(description = "size: the page size {1000}", example = "1000") @DefaultValue("1000") @QueryParam("size")  Integer size,@Parameter(description = "sort: the sort pattern {insertionTime:DESC}", example = "insertionTime:DESC") @DefaultValue("insertionTime:DESC") @QueryParam("sort")  String sort,@Context SecurityContext securityContext)
    throws NotFoundException {
        context.setHttpHeaders(headers);
        context.setRequest(request);
        context.setUriInfo(uriInfo);
        return delegate.listPayloads(hash, objectType, minsize, page, size, sort, securityContext);
    }

    @jakarta.ws.rs.POST
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json", "application/xml" })
    @Operation(summary = "Create Payloads in the database, associated to a given iov since list and tag name.", description = "", responses = {
            @ApiResponse(responseCode = "201", description = "successful operation", content =
                @Content(schema = @Schema(implementation = StoreSetDto.class))),
            @ApiResponse(responseCode = "200", description = "Generic error response", content =
                @Content(schema = @Schema(implementation = HTTPResponse.class))),
            },security = {
            @SecurityRequirement(name = "OpenID", scopes={ "openid" }),
            @SecurityRequirement(name = "BearerAuth")
        }, tags={ "payloads", })
    public Response storePayloadBatch(@Parameter(description = "The tag name", required=true)@FormDataParam("tag")  String tag,@Parameter(description = "the string representing a StoreSetDto in json", required=true)@FormDataParam("storeset")  String storeset,@Parameter(description = "The format of the input data. StoreSetDto entries will have either the content inline (JSON) or stored via external files (FILE). " , schema=@Schema(allowableValues={
"FILE", "JSON"
})
, example="FILE")@HeaderParam("X-Crest-PayloadFormat") String xCrestPayloadFormat,
 @FormDataParam("files") List<FormDataBodyPart> filesBodypart ,@Parameter(description = "The object type")@FormDataParam("objectType")  String objectType,@Parameter(description = "The compression type")@FormDataParam("compressionType")  String compressionType,@Parameter(description = "The version")@FormDataParam("version")  String version,@Parameter(description = "The tag end time. This represents a number.")@FormDataParam("endtime")  String endtime,@Context SecurityContext securityContext)
    throws NotFoundException {
        context.setHttpHeaders(headers);
        context.setRequest(request);
        context.setUriInfo(uriInfo);
        return delegate.storePayloadBatch(tag, storeset, xCrestPayloadFormat, filesBodypart, objectType, compressionType, version, endtime, securityContext);
    }

    @jakarta.ws.rs.PUT
    @Path("/data")
    @Consumes({ "application/json" })
    @Produces({ "application/json", "application/xml" })
    @Operation(summary = "Update a streamerInfo in a payload", description = "", responses = {
            @ApiResponse(responseCode = "200", description = "successful operation", content =
                @Content(schema = @Schema(implementation = PayloadDto.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content =
                @Content(schema = @Schema(implementation = HTTPResponse.class))),
            @ApiResponse(responseCode = "200", description = "Generic error response", content =
                @Content(schema = @Schema(implementation = HTTPResponse.class))),
            },security = {
            @SecurityRequirement(name = "OpenID", scopes={ "openid" }),
            @SecurityRequirement(name = "BearerAuth")
        }, tags={ "payloads", })
    public Response updatePayload(@Parameter(description = "hash:  the hash of the payload", required = true) @PathParam("hash") @NotNull  String hash,@Parameter(description = "") @Valid  GenericMap genericMap,@Context SecurityContext securityContext)
    throws NotFoundException {
        context.setHttpHeaders(headers);
        context.setRequest(request);
        context.setUriInfo(uriInfo);
        return delegate.updatePayload(hash, genericMap, securityContext);
    }

    @jakarta.ws.rs.PUT
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json", "application/xml" })
    @Operation(summary = "Upload and process large JSON data.", description = "", responses = {
            @ApiResponse(responseCode = "201", description = "successful operation", content =
                @Content(schema = @Schema(implementation = StoreSetDto.class))),
            @ApiResponse(responseCode = "200", description = "Generic error response", content =
                @Content(schema = @Schema(implementation = HTTPResponse.class))),
            },security = {
            @SecurityRequirement(name = "OpenID", scopes={ "openid" }),
            @SecurityRequirement(name = "BearerAuth")
        }, tags={ "payloads", })
    public Response uploadJson(@Parameter(description = "The tag name", required=true)@FormDataParam("tag")  String tag,
 @FormDataParam("storeset") FormDataBodyPart storesetBodypart ,@Parameter(description = "The object type")@FormDataParam("objectType")  String objectType,@Parameter(description = "The compression type")@FormDataParam("compressionType")  String compressionType,@Parameter(description = "The version")@FormDataParam("version")  String version,@Parameter(description = "The tag end time, represent a number.")@FormDataParam("endtime")  String endtime,@Context SecurityContext securityContext)
    throws NotFoundException {
        context.setHttpHeaders(headers);
        context.setRequest(request);
        context.setUriInfo(uriInfo);
        return delegate.uploadJson(tag, storesetBodypart, objectType, compressionType, version, endtime, securityContext);
    }
}
