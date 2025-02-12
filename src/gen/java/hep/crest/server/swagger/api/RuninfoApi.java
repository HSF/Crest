package hep.crest.server.swagger.api;

import hep.crest.server.swagger.api.RuninfoApiService;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import hep.crest.server.swagger.model.RunLumiInfoDto;
import hep.crest.server.swagger.model.RunLumiSetDto;

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

@Path("/runinfo")


///// @jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJerseyServerCodegen")
public class RuninfoApi  {

   @Autowired
   private RuninfoApiService delegate;
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
    @Operation(summary = "Create an entry for run information.", description = "", responses = {
            @ApiResponse(responseCode = "200", description = "successful operation", content =
                @Content(schema = @Schema(implementation = RunLumiSetDto.class))),
            },security = {
            @SecurityRequirement(name = "OpenID", scopes={ "openid" }),
            @SecurityRequirement(name = "BearerAuth")
        }, tags={ "runinfo", })
    public Response createRunInfo(@Parameter(description = "") @Valid  RunLumiSetDto runLumiSetDto,@Context SecurityContext securityContext)
    throws NotFoundException {
        context.setHttpHeaders(headers);
        context.setRequest(request);
        context.setUriInfo(uriInfo);
        return delegate.createRunInfo(runLumiSetDto, securityContext);
    }

    @jakarta.ws.rs.GET
    @Produces({ "application/json" })
    @Operation(summary = "Finds a RunLumiInfoDto lists using parameters.", description = "", responses = {
            @ApiResponse(responseCode = "200", description = "successful operation", content =
                @Content(schema = @Schema(implementation = RunLumiSetDto.class))),
            },security = {
            @SecurityRequirement(name = "OpenID", scopes={ "openid" }),
            @SecurityRequirement(name = "BearerAuth")
        }, tags={ "runinfo", })
    public Response listRunInfo(@Parameter(description = "since: the starting time or run-lumi", example = "none") @DefaultValue("none") @QueryParam("since")  String since,@Parameter(description = "until: the ending time or run-lumi", example = "none") @DefaultValue("none") @QueryParam("until")  String until,@Parameter(description = "the format to digest previous arguments [iso], [number], [run-lumi]. Time(iso) = yyyymmddhhmiss,  Time(number) = milliseconds or Run(number) = runnumber Run(run-lumi) = runnumber-lumisection ", example = "number") @DefaultValue("number") @QueryParam("format")  String format,@Parameter(description = "the mode for the request : [daterange] or [runrange] ", schema=@Schema(allowableValues={
"daterange", "runrange"
})
, example = "runrange") @DefaultValue("runrange") @QueryParam("mode")  String mode,@Parameter(description = "page: the page number {0}", example = "0") @DefaultValue("0") @QueryParam("page")  Integer page,@Parameter(description = "size: the page size {1000}", example = "1000") @DefaultValue("1000") @QueryParam("size")  Integer size,@Parameter(description = "sort: the sort pattern {id.runNumber:ASC}", example = "id.runNumber:ASC") @DefaultValue("id.runNumber:ASC") @QueryParam("sort")  String sort,@Context SecurityContext securityContext)
    throws NotFoundException {
        context.setHttpHeaders(headers);
        context.setRequest(request);
        context.setUriInfo(uriInfo);
        return delegate.listRunInfo(since, until, format, mode, page, size, sort, securityContext);
    }

    @jakarta.ws.rs.PUT
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @Operation(summary = "Update an entry for run information.", description = "", responses = {
            @ApiResponse(responseCode = "200", description = "successful operation", content =
                @Content(schema = @Schema(implementation = RunLumiSetDto.class))),
            },security = {
            @SecurityRequirement(name = "OpenID", scopes={ "openid" }),
            @SecurityRequirement(name = "BearerAuth")
        }, tags={ "runinfo", })
    public Response updateRunInfo(@Parameter(description = "") @Valid  RunLumiInfoDto runLumiInfoDto,@Context SecurityContext securityContext)
    throws NotFoundException {
        context.setHttpHeaders(headers);
        context.setRequest(request);
        context.setUriInfo(uriInfo);
        return delegate.updateRunInfo(runLumiInfoDto, securityContext);
    }
}
