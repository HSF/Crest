package hep.crest.server.swagger.api;

import hep.crest.swagger.model.*;
import hep.crest.server.swagger.api.RuninfoApiService;

import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.*;

import hep.crest.swagger.model.RunInfoSetDto;

import java.util.Map;
import java.util.List;
import hep.crest.server.swagger.api.NotFoundException;

import java.io.InputStream;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.servlet.ServletConfig;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import javax.ws.rs.*;

import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.constraints.*;

@Path("/runinfo")


@io.swagger.annotations.Api(description = "the runinfo API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2019-12-10T11:36:04.026+01:00")
public class RuninfoApi  {
	@Autowired
	private RuninfoApiService delegate;

    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/plain" })
    @io.swagger.annotations.ApiOperation(value = "Create an entry for run information.", notes = "Run informations go into a separate table.", response = String.class, tags={ "runinfo", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = String.class) })
    public Response createRunInfo(@ApiParam(value = "A json string that is used to construct one or more runinfodto object: { run: xxx, ... }" ,required=true) RunInfoSetDto body
,@Context SecurityContext securityContext,@Context UriInfo info)
    throws NotFoundException {
        return delegate.createRunInfo(body,securityContext,info);
    }
    @GET
    
    
    @Produces({ "application/json", "application/xml" })
    @io.swagger.annotations.ApiOperation(value = "Finds a RunLumiInfoDto lists.", notes = "This method allows to perform search and sorting.Arguments: by=<pattern>, page={ipage}, size={isize}, sort=<sortpattern>. The pattern <pattern> is in the form <param-name><operation><param-value>       <param-name> is the name of one of the fields in the dto       <operation> can be [< : >] ; for string use only [:]        <param-value> depends on the chosen parameter. A list of this criteria can be provided       using comma separated strings for <pattern>.      The pattern <sortpattern> is <field>:[DESC|ASC]", response = RunInfoSetDto.class, tags={ "runinfo", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = RunInfoSetDto.class) })
    public Response listRunInfo(@ApiParam(value = "by: the search pattern {none}", defaultValue="none") @DefaultValue("none") @QueryParam("by") String by
,@ApiParam(value = "page: the page number {0}", defaultValue="0") @DefaultValue("0") @QueryParam("page") Integer page
,@ApiParam(value = "size: the page size {1000}", defaultValue="1000") @DefaultValue("1000") @QueryParam("size") Integer size
,@ApiParam(value = "sort: the sort pattern {runNumber:ASC}", defaultValue="runNumber:ASC") @DefaultValue("runNumber:ASC") @QueryParam("sort") String sort
,@Context SecurityContext securityContext,@Context UriInfo info)
    throws NotFoundException {
        return delegate.listRunInfo(by,page,size,sort,securityContext,info);
    }
    @GET
    @Path("/select")
    
    @Produces({ "application/json", "application/xml" })
    @io.swagger.annotations.ApiOperation(value = "Finds a RunLumiInfoDto lists using parameters.", notes = "This method allows to perform search.Arguments: from=<someformat>,to=<someformat>, format=<describe previous types>, page={ipage}, size={isize}, sort=<sortpattern>. The pattern <pattern> is in the form <param-name><operation><param-value>       <param-name> is the name of one of the fields in the dto       <operation> can be [< : >] ; for string use only [:]        <param-value> depends on the chosen parameter. A list of this criteria can be provided       using comma separated strings for <pattern>.      The pattern <sortpattern> is <field>:[DESC|ASC]", response = RunInfoSetDto.class, tags={ "runinfo", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = RunInfoSetDto.class) })
    public Response selectRunInfo(@ApiParam(value = "from: the starting time or run-lumi", defaultValue="none") @DefaultValue("none") @QueryParam("from") String from
,@ApiParam(value = "to: the ending time or run-lumi", defaultValue="none") @DefaultValue("none") @QueryParam("to") String to
,@ApiParam(value = "format: the format to digest previous arguments [time] or [run]. Time = yyyymmddhhmiss, Run = runnumber", defaultValue="time") @DefaultValue("time") @QueryParam("format") String format
,@ApiParam(value = "page: the page number {0}", defaultValue="0") @DefaultValue("0") @QueryParam("page") Integer page
,@ApiParam(value = "size: the page size {1000}", defaultValue="1000") @DefaultValue("1000") @QueryParam("size") Integer size
,@ApiParam(value = "sort: the sort pattern {runNumber:ASC}", defaultValue="runNumber:ASC") @DefaultValue("runNumber:ASC") @QueryParam("sort") String sort
,@Context SecurityContext securityContext,@Context UriInfo info)
    throws NotFoundException {
        return delegate.selectRunInfo(from,to,format,page,size,sort,securityContext,info);
    }
}
