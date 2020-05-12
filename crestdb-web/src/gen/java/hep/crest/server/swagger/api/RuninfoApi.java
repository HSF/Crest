package hep.crest.server.swagger.api;

import hep.crest.swagger.model.RunLumiSetDto;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;

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

@Path("/runinfo")


@io.swagger.annotations.Api(description = "the runinfo API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2020-05-12T22:36:06.312+02:00")
public class RuninfoApi  {
	@Autowired
	private RuninfoApiService delegate;

    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/plain" })
    @io.swagger.annotations.ApiOperation(value = "Create an entry for run information.", notes = "Run informations go into a separate table.", response = String.class, tags={ "runinfo", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = String.class) })
    public Response createRunInfo(@ApiParam(value = "A json string that is used to construct a list of runlumiinfodto object: { run: xxx, ... }" ,required=true) RunLumiSetDto body
,@Context SecurityContext securityContext,@Context UriInfo info)
    throws NotFoundException {
        return delegate.createRunInfo(body,securityContext,info);
    }
    @GET
    
    
    @Produces({ "application/json", "application/xml" })
    @io.swagger.annotations.ApiOperation(value = "Finds a RunLumiInfoDto lists.", notes = "This method allows to perform search and sorting.Arguments: by=<pattern>, page={ipage}, size={isize}, sort=<sortpattern>. The pattern <pattern> is in the form <param-name><operation><param-value>       <param-name> is the name of one of the fields in the dto       <operation> can be [< : >] ; for string use only [:]        <param-value> depends on the chosen parameter. A list of this criteria can be provided       using comma separated strings for <pattern>.      The pattern <sortpattern> is <field>:[DESC|ASC]", response = RunLumiSetDto.class, tags={ "runinfo", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = RunLumiSetDto.class) })
    public Response listRunInfo(@ApiParam(value = "by: the search pattern {none}", defaultValue="none") @DefaultValue("none") @QueryParam("by") String by
,@ApiParam(value = "page: the page number {0}", defaultValue="0") @DefaultValue("0") @QueryParam("page") Integer page
,@ApiParam(value = "size: the page size {1000}", defaultValue="1000") @DefaultValue("1000") @QueryParam("size") Integer size
,@ApiParam(value = "sort: the sort pattern {since:ASC}", defaultValue="since:ASC") @DefaultValue("since:ASC") @QueryParam("sort") String sort
,@Context SecurityContext securityContext,@Context UriInfo info)
    throws NotFoundException {
        return delegate.listRunInfo(by,page,size,sort,securityContext,info);
    }
    @GET
    @Path("/select")
    
    @Produces({ "application/json", "application/xml" })
    @io.swagger.annotations.ApiOperation(value = "Finds a RunLumiInfoDto lists using parameters.", notes = "This method allows to perform search.Arguments: from=<someformat>,to=<someformat>, format=<describe previous types>, page={ipage}, size={isize}, sort=<sortpattern>. The pattern <pattern> is in the form <param-name><operation><param-value>       <param-name> is the name of one of the fields in the dto       <operation> can be [< : >] ; for string use only [:]        <param-value> depends on the chosen parameter. A list of this criteria can be provided       using comma separated strings for <pattern>.      The pattern <sortpattern> is <field>:[DESC|ASC]", response = RunLumiSetDto.class, tags={ "runinfo", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = RunLumiSetDto.class) })
    public Response selectRunInfo(@ApiParam(value = "from: the starting time or run-lumi", defaultValue="none") @DefaultValue("none") @QueryParam("from") String from
,@ApiParam(value = "to: the ending time or run-lumi", defaultValue="none") @DefaultValue("none") @QueryParam("to") String to
,@ApiParam(value = "format: the format to digest previous arguments [iso], [number]. Time(iso) = yyyymmddhhmiss, Run(number) = runnumber, Time(number) = milliseconds", defaultValue="number") @DefaultValue("number") @QueryParam("format") String format
,@ApiParam(value = "mode: the mode for the request : [daterange] or [runrange]. Interprets", defaultValue="runrange") @DefaultValue("runrange") @QueryParam("mode") String mode
,@Context SecurityContext securityContext,@Context UriInfo info)
    throws NotFoundException {
        return delegate.selectRunInfo(from,to,format,mode,securityContext,info);
    }
}
