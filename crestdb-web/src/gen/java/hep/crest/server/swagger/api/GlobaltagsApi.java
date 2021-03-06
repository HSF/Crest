package hep.crest.server.swagger.api;

import hep.crest.swagger.model.*;
import hep.crest.server.swagger.api.GlobaltagsApiService;

import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.*;

import hep.crest.swagger.model.GlobalTagDto;
import hep.crest.swagger.model.GlobalTagSetDto;
import hep.crest.swagger.model.TagSetDto;

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

@Path("/globaltags")


@io.swagger.annotations.Api(description = "the globaltags API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2019-12-26T18:15:33.531+01:00")
public class GlobaltagsApi  {
	@Autowired
	private GlobaltagsApiService delegate;

    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Create a GlobalTag in the database.", notes = "This method allows to insert a GlobalTag.Arguments: GlobalTagDto should be provided in the body as a JSON file.", response = GlobalTagDto.class, tags={ "globaltags", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "successful operation", response = GlobalTagDto.class) })
    public Response createGlobalTag(@ApiParam(value = "A json string that is used to construct a globaltagdto object: { name: xxx, ... }" ,required=true) GlobalTagDto body
,@ApiParam(value = "force: tell the server if it should use or not the insertion time provided {default: false}", defaultValue="false") @DefaultValue("false") @QueryParam("force") String force
,@Context SecurityContext securityContext,@Context UriInfo info)
    throws NotFoundException {
        return delegate.createGlobalTag(body,force,securityContext,info);
    }
    @GET
    @Path("/{name}")
    
    @Produces({ "application/json", "application/xml" })
    @io.swagger.annotations.ApiOperation(value = "Finds a GlobalTagDto by name", notes = "This method will search for a global tag with the given name. Only one global tag should be returned.", response = GlobalTagSetDto.class, tags={ "globaltags", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = GlobalTagSetDto.class) })
    public Response findGlobalTag(@ApiParam(value = "",required=true) @PathParam("name") String name
,@Context SecurityContext securityContext,@Context UriInfo info)
    throws NotFoundException {
        return delegate.findGlobalTag(name,securityContext,info);
    }
    @GET
    @Path("/{name}/tags")
    
    @Produces({ "application/json", "application/xml" })
    @io.swagger.annotations.ApiOperation(value = "Finds a TagDtos lists associated to the global tag name in input.", notes = "This method allows to trace a global tag.Arguments: record=<record> filter output by record, label=<label> filter output by label", response = TagSetDto.class, tags={ "globaltags", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = TagSetDto.class) })
    public Response findGlobalTagFetchTags(@ApiParam(value = "",required=true) @PathParam("name") String name
,@ApiParam(value = "record:  the record string {}", defaultValue="none") @DefaultValue("none") @QueryParam("record") String record
,@ApiParam(value = "label:  the label string {}", defaultValue="none") @DefaultValue("none") @QueryParam("label") String label
,@Context SecurityContext securityContext,@Context UriInfo info)
    throws NotFoundException {
        return delegate.findGlobalTagFetchTags(name,record,label,securityContext,info);
    }
    @GET
    
    
    @Produces({ "application/json", "application/xml" })
    @io.swagger.annotations.ApiOperation(value = "Finds a GlobalTagDtos lists.", notes = "This method allows to perform search and sorting.Arguments: by=<pattern>, page={ipage}, size={isize}, sort=<sortpattern>. The pattern <pattern> is in the form <param-name><operation><param-value>       <param-name> is the name of one of the fields in the dto       <operation> can be [< : >] ; for string use only [:]        <param-value> depends on the chosen parameter. A list of this criteria can be provided       using comma separated strings for <pattern>.      The pattern <sortpattern> is <field>:[DESC|ASC]", response = GlobalTagSetDto.class, tags={ "globaltags", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = GlobalTagSetDto.class) })
    public Response listGlobalTags(@ApiParam(value = "by: the search pattern {none}", defaultValue="none") @DefaultValue("none") @QueryParam("by") String by
,@ApiParam(value = "page: the page number {0}", defaultValue="0") @DefaultValue("0") @QueryParam("page") Integer page
,@ApiParam(value = "size: the page size {1000}", defaultValue="1000") @DefaultValue("1000") @QueryParam("size") Integer size
,@ApiParam(value = "sort: the sort pattern {name:ASC}", defaultValue="name:ASC") @DefaultValue("name:ASC") @QueryParam("sort") String sort
,@Context SecurityContext securityContext,@Context UriInfo info)
    throws NotFoundException {
        return delegate.listGlobalTags(by,page,size,sort,securityContext,info);
    }
}
