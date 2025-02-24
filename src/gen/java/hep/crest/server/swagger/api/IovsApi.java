package hep.crest.server.swagger.api;

import hep.crest.server.swagger.api.IovsApiService;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import hep.crest.server.swagger.model.HTTPResponse;
import hep.crest.server.swagger.model.IovDto;
import hep.crest.server.swagger.model.IovPayloadSetDto;
import hep.crest.server.swagger.model.IovSetDto;
import hep.crest.server.swagger.model.TagSummarySetDto;

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

@Path("/iovs")


///// @jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJerseyServerCodegen")
public class IovsApi  {

   @Autowired
   private IovsApiService delegate;
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
    @Operation(summary = "Finds a IovDtos lists.", description = "", responses = {
            @ApiResponse(responseCode = "200", description = "successful operation", content =
                @Content(schema = @Schema(implementation = IovSetDto.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content =
                @Content(schema = @Schema(implementation = HTTPResponse.class))),
            @ApiResponse(responseCode = "200", description = "Generic error response", content =
                @Content(schema = @Schema(implementation = HTTPResponse.class))),
            },security = {
            @SecurityRequirement(name = "OpenID", scopes={ "openid" }),
            @SecurityRequirement(name = "BearerAuth")
        }, tags={ "iovs", })
    public Response findAllIovs(@Parameter(description = "the method used will determine which query is executed IOVS, RANGE and AT is a standard IOV query requiring a precise tag name GROUPS is a group query type ", required = true, schema=@Schema(allowableValues={
"IOVS", "GROUPS", "MONITOR"
})
, example = "IOVS") @DefaultValue("IOVS") @QueryParam("method") @NotNull  String method,@Parameter(description = "the tag name", example = "none") @DefaultValue("none") @QueryParam("tagname")  String tagname,@Parameter(description = "snapshot: the snapshot time {0}", example = "0") @DefaultValue("0") @QueryParam("snapshot")  Long snapshot,@Parameter(description = "the since time as a string {0}", example = "0") @DefaultValue("0") @QueryParam("since")  String since,@Parameter(description = "the until time as a string {INF}", example = "INF") @DefaultValue("INF") @QueryParam("until")  String until,@Parameter(description = "the format for since and until {number | ms | iso | run-lumi | custom (yyyyMMdd'T'HHmmssX)} If timeformat is equal number, we just parse the argument as a long. ", schema=@Schema(allowableValues={
"NUMBER", "MS", "ISO", "RUN", "RUN_LUMI", "CUSTOM"
})
, example = "NUMBER") @DefaultValue("NUMBER") @QueryParam("timeformat")  String timeformat,@Parameter(description = "The group size represent the pagination type provided for GROUPS query method. ") @QueryParam("groupsize")  Long groupsize,@Parameter(description = "the hash for searching specific IOV list for a given hash. ") @QueryParam("hash")  String hash,@Parameter(description = "the page number {0}", example = "0") @DefaultValue("0") @QueryParam("page")  Integer page,@Parameter(description = "the page size {10000}", example = "10000") @DefaultValue("10000") @QueryParam("size")  Integer size,@Parameter(description = "the sort pattern {id.since:ASC}", example = "id.since:ASC") @DefaultValue("id.since:ASC") @QueryParam("sort")  String sort,@Parameter(description = "The query type. The header parameter X-Crest-Query can be : iovs, ranges, at. The iovs represents an exclusive interval, while ranges and at include previous since. This has an impact on how the since and until ranges are applied. " , schema=@Schema(allowableValues={
"IOVS", "RANGES", "AT"
})
, example="IOVS")@HeaderParam("X-Crest-Query") String xCrestQuery,@Parameter(description = "The since type required in the query. It can be : ms, cool. Since and until will be transformed in these units. It differs from timeformat which indicates how to interpret the since and until strings in input. " , schema=@Schema(allowableValues={
"MS", "COOL", "NUMBER"
})
, example="NUMBER")@HeaderParam("X-Crest-Since") String xCrestSince,@Context SecurityContext securityContext)
    throws NotFoundException {
        context.setHttpHeaders(headers);
        context.setRequest(request);
        context.setUriInfo(uriInfo);
        return delegate.findAllIovs(method, tagname, snapshot, since, until, timeformat, groupsize, hash, page, size, sort, xCrestQuery, xCrestSince, securityContext);
    }

    @jakarta.ws.rs.GET
    @Path("/size")
    @Produces({ "application/json" })
    @Operation(summary = "Get the number o iovs for tags matching pattern.", description = "", responses = {
            @ApiResponse(responseCode = "200", description = "successful operation", content =
                @Content(schema = @Schema(implementation = TagSummarySetDto.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content =
                @Content(schema = @Schema(implementation = HTTPResponse.class))),
            @ApiResponse(responseCode = "200", description = "Generic error response", content =
                @Content(schema = @Schema(implementation = HTTPResponse.class))),
            },security = {
            @SecurityRequirement(name = "OpenID", scopes={ "openid" }),
            @SecurityRequirement(name = "BearerAuth")
        }, tags={ "iovs", })
    public Response getSizeByTag(@Parameter(description = "the tag name, can be a pattern like MDT%", required = true, example = "none") @DefaultValue("none") @QueryParam("tagname") @NotNull  String tagname,@Context SecurityContext securityContext)
    throws NotFoundException {
        context.setHttpHeaders(headers);
        context.setRequest(request);
        context.setUriInfo(uriInfo);
        return delegate.getSizeByTag(tagname, securityContext);
    }

    @jakarta.ws.rs.GET
    @Path("/infos")
    @Produces({ "application/json" })
    @Operation(summary = "Select iovs and payload meta info for a given tagname and in a given range.", description = "", responses = {
            @ApiResponse(responseCode = "200", description = "successful operation", content =
                @Content(schema = @Schema(implementation = IovPayloadSetDto.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content =
                @Content(schema = @Schema(implementation = HTTPResponse.class))),
            @ApiResponse(responseCode = "200", description = "Generic error response", content =
                @Content(schema = @Schema(implementation = HTTPResponse.class))),
            },security = {
            @SecurityRequirement(name = "OpenID", scopes={ "openid" }),
            @SecurityRequirement(name = "BearerAuth")
        }, tags={ "iovs", })
    public Response selectIovPayloads(@Parameter(description = "the tag name", required = true, example = "none") @DefaultValue("none") @QueryParam("tagname") @NotNull  String tagname,@Parameter(description = "the since time as a string {0}", example = "0") @DefaultValue("0") @QueryParam("since")  String since,@Parameter(description = "the until time as a string {INF}", example = "INF") @DefaultValue("INF") @QueryParam("until")  String until,@Parameter(description = "the format for since and until {number | ms | iso | custom (yyyyMMdd'T'HHmmssX)} If timeformat is equal number, we just parse the argument as a long. ", example = "number") @DefaultValue("number") @QueryParam("timeformat")  String timeformat,@Parameter(description = "the page number {0}", example = "0") @DefaultValue("0") @QueryParam("page")  Integer page,@Parameter(description = "the page size {10000}", example = "10000") @DefaultValue("10000") @QueryParam("size")  Integer size,@Parameter(description = "the sort pattern {id.since:ASC}", example = "id.since:ASC") @DefaultValue("id.since:ASC") @QueryParam("sort")  String sort,@Context SecurityContext securityContext)
    throws NotFoundException {
        context.setHttpHeaders(headers);
        context.setRequest(request);
        context.setUriInfo(uriInfo);
        return delegate.selectIovPayloads(tagname, since, until, timeformat, page, size, sort, securityContext);
    }

    @jakarta.ws.rs.POST
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @Operation(summary = "Create IOVs in the database, associated to a tag name.", description = "", responses = {
            @ApiResponse(responseCode = "201", description = "successful operation", content =
                @Content(schema = @Schema(implementation = IovSetDto.class))),
            @ApiResponse(responseCode = "200", description = "Generic error response", content =
                @Content(schema = @Schema(implementation = HTTPResponse.class))),
            },security = {
            @SecurityRequirement(name = "OpenID", scopes={ "openid" }),
            @SecurityRequirement(name = "BearerAuth")
        }, tags={ "iovs", })
    public Response storeIovBatch(@Parameter(description = "") @Valid  IovSetDto iovSetDto,@Context SecurityContext securityContext)
    throws NotFoundException {
        context.setHttpHeaders(headers);
        context.setRequest(request);
        context.setUriInfo(uriInfo);
        return delegate.storeIovBatch(iovSetDto, securityContext);
    }

    @jakarta.ws.rs.PUT
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @Operation(summary = "Create a single IOV in the database, associated to a tag name.", description = "", responses = {
            @ApiResponse(responseCode = "201", description = "successful operation", content =
                @Content(schema = @Schema(implementation = IovSetDto.class))),
            @ApiResponse(responseCode = "400", description = "Bad request", content =
                @Content(schema = @Schema(implementation = HTTPResponse.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content =
                @Content(schema = @Schema(implementation = HTTPResponse.class))),
            @ApiResponse(responseCode = "200", description = "Generic error response", content =
                @Content(schema = @Schema(implementation = HTTPResponse.class))),
            },security = {
            @SecurityRequirement(name = "OpenID", scopes={ "openid" }),
            @SecurityRequirement(name = "BearerAuth")
        }, tags={ "iovs", })
    public Response storeIovOne(@Parameter(description = "") @Valid  IovDto iovDto,@Context SecurityContext securityContext)
    throws NotFoundException {
        context.setHttpHeaders(headers);
        context.setRequest(request);
        context.setUriInfo(uriInfo);
        return delegate.storeIovOne(iovDto, securityContext);
    }
}
