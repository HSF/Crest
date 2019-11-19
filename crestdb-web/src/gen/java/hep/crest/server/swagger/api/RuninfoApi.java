package hep.crest.server.swagger.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.springframework.beans.factory.annotation.Autowired;

import hep.crest.swagger.model.RunLumiInfoDto;
import hep.crest.swagger.model.RunLumiSetDto;
import io.swagger.annotations.ApiParam;

@Path("/runinfo")


@io.swagger.annotations.Api(description = "the runinfo API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2018-01-14T18:09:32.330+01:00")
public class RuninfoApi  {

	@Autowired
	private RuninfoApiService delegate;

    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/plain" })
    @io.swagger.annotations.ApiOperation(value = "Create an entry for run information.", notes = "Run informations go into a separate table.", response = String.class, tags={ "runinfo", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = String.class) })
    public Response createRunLumiInfo(@ApiParam(value = "A json string that is used to construct a runlumiinfodto object: { run: xxx, ... }" ,required=true) RunLumiInfoDto body
,@Context SecurityContext securityContext,@Context UriInfo info)
    throws NotFoundException {
        return delegate.createRunLumiInfo(body,securityContext,info);
    }
    @GET
    @Path("/list")
    
    @Produces({ "application/json", "application/xml" })
    @io.swagger.annotations.ApiOperation(value = "Finds a RunLumiInfoDto lists using parameters.", notes = "This method allows to perform search.Arguments: from=<someformat>,to=<someformat>, format=<describe previous types>, page={ipage}, size={isize}, sort=<sortpattern>. The pattern <pattern> is in the form <param-name><operation><param-value>       <param-name> is the name of one of the fields in the dto       <operation> can be [< : >] ; for string use only [:]        <param-value> depends on the chosen parameter. A list of this criteria can be provided       using comma separated strings for <pattern>.      The pattern <sortpattern> is <field>:[DESC|ASC]", response = RunLumiSetDto.class, tags={ "runinfo", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = RunLumiSetDto.class) })
    public Response findRunLumiInfo(@ApiParam(value = "from: the starting time or run-lumi", defaultValue="none") @DefaultValue("none") @QueryParam("from") String from
,@ApiParam(value = "to: the ending time or run-lumi", defaultValue="none") @DefaultValue("none") @QueryParam("to") String to
,@ApiParam(value = "format: the format to digest previous arguments [time] or [run-lumi]. Time = yyyymmddhhmiss, Run-lumi = run-lumi", defaultValue="time") @DefaultValue("time") @QueryParam("format") String format
,@ApiParam(value = "page: the page number {0}", defaultValue="0") @DefaultValue("0") @QueryParam("page") Integer page
,@ApiParam(value = "size: the page size {1000}", defaultValue="1000") @DefaultValue("1000") @QueryParam("size") Integer size
,@ApiParam(value = "sort: the sort pattern {since:ASC}", defaultValue="since:ASC") @DefaultValue("since:ASC") @QueryParam("sort") String sort
,@Context SecurityContext securityContext,@Context UriInfo info)
    throws NotFoundException {
        return delegate.findRunLumiInfo(from,to,format,page,size,sort,securityContext,info);
    }
    @GET
    
    
    @Produces({ "application/json", "application/xml" })
    @io.swagger.annotations.ApiOperation(value = "Finds a RunLumiInfoDto lists.", notes = "This method allows to perform search and sorting.Arguments: by=<pattern>, page={ipage}, size={isize}, sort=<sortpattern>. The pattern <pattern> is in the form <param-name><operation><param-value>       <param-name> is the name of one of the fields in the dto       <operation> can be [< : >] ; for string use only [:]        <param-value> depends on the chosen parameter. A list of this criteria can be provided       using comma separated strings for <pattern>.      The pattern <sortpattern> is <field>:[DESC|ASC]", response = RunLumiInfoDto.class, responseContainer = "List", tags={ "runinfo", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = RunLumiInfoDto.class, responseContainer = "List") })
    public Response listRunLumiInfo(@ApiParam(value = "by: the search pattern {none}", defaultValue="none") @DefaultValue("none") @QueryParam("by") String by
,@ApiParam(value = "page: the page number {0}", defaultValue="0") @DefaultValue("0") @QueryParam("page") Integer page
,@ApiParam(value = "size: the page size {1000}", defaultValue="1000") @DefaultValue("1000") @QueryParam("size") Integer size
,@ApiParam(value = "sort: the sort pattern {since:ASC}", defaultValue="since:ASC") @DefaultValue("since:ASC") @QueryParam("sort") String sort
,@Context SecurityContext securityContext,@Context UriInfo info)
    throws NotFoundException {
        return delegate.listRunLumiInfo(by,page,size,sort,securityContext,info);
    }
}
