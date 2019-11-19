package hep.crest.server.swagger.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.springframework.beans.factory.annotation.Autowired;

import hep.crest.swagger.model.GenericMap;
import hep.crest.swagger.model.TagDto;
import hep.crest.swagger.model.TagMetaDto;
import hep.crest.swagger.model.TagSetDto;
import io.swagger.annotations.ApiParam;

@Path("/tags")


@io.swagger.annotations.Api(description = "the tags API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2019-10-03T10:49:50.724+02:00")
public class TagsApi  {
	@Autowired
	private TagsApiService delegate;

    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Create a Tag in the database.", notes = "This method allows to insert a Tag.Arguments: TagDto should be provided in the body as a JSON file.", response = TagDto.class, tags={ "tags", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = TagDto.class) })
    public Response createTag(@ApiParam(value = "A json string that is used to construct a tagdto object: { name: xxx, ... }" ,required=true) TagDto body
,@Context SecurityContext securityContext,@Context UriInfo info)
    throws NotFoundException {
        return delegate.createTag(body,securityContext,info);
    }
    @POST
    @Path("/{name}/meta")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Create a TagMeta in the database.", notes = "This method allows to insert a TagMeta.Arguments: TagMetaDto should be provided in the body as a JSON file.", response = TagMetaDto.class, tags={ "tags", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = TagMetaDto.class) })
    public Response createTagMeta(@ApiParam(value = "name: the tag name",required=true) @PathParam("name") String name
,@ApiParam(value = "A json string that is used to construct a tagmetadto object: { tagName: xxx, ... }" ,required=true) TagMetaDto body
,@Context SecurityContext securityContext,@Context UriInfo info)
    throws NotFoundException {
        return delegate.createTagMeta(name,body,securityContext,info);
    }
    @GET
    @Path("/{name}")
    
    @Produces({ "application/json", "application/xml" })
    @io.swagger.annotations.ApiOperation(value = "Finds a TagDto by name", notes = "This method will search for a tag with the given name. Only one tag should be returned.", response = TagSetDto.class, tags={ "tags", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = TagSetDto.class) })
    public Response findTag(@ApiParam(value = "name: the tag name",required=true) @PathParam("name") String name
,@Context SecurityContext securityContext,@Context UriInfo info)
    throws NotFoundException {
        return delegate.findTag(name,securityContext,info);
    }
    @GET
    @Path("/{name}/meta")
    
    @Produces({ "application/json", "application/xml" })
    @io.swagger.annotations.ApiOperation(value = "Finds a TagMetaDto by name", notes = "This method will search for a tag metadata with the given name. Only one tag should be returned.", response = TagMetaDto.class, tags={ "tags", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = TagMetaDto.class) })
    public Response findTagMeta(@ApiParam(value = "name: the tag name",required=true) @PathParam("name") String name
,@Context SecurityContext securityContext,@Context UriInfo info)
    throws NotFoundException {
        return delegate.findTagMeta(name,securityContext,info);
    }
    @GET
    
    
    @Produces({ "application/json", "application/xml" })
    @io.swagger.annotations.ApiOperation(value = "Finds a TagDtos lists.", notes = "This method allows to perform search and sorting.Arguments: by=<pattern>, page={ipage}, size={isize}, sort=<sortpattern>. The pattern <pattern> is in the form <param-name><operation><param-value>       <param-name> is the name of one of the fields in the dto       <operation> can be [< : >] ; for string use only [:]        <param-value> depends on the chosen parameter. A list of this criteria can be provided       using comma separated strings for <pattern>.      The pattern <sortpattern> is <field>:[DESC|ASC]", response = TagSetDto.class, tags={ "tags", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = TagSetDto.class) })
    public Response listTags(@ApiParam(value = "by: the search pattern {none}", defaultValue="none") @DefaultValue("none") @QueryParam("by") String by
,@ApiParam(value = "page: the page number {0}", defaultValue="0") @DefaultValue("0") @QueryParam("page") Integer page
,@ApiParam(value = "size: the page size {1000}", defaultValue="1000") @DefaultValue("1000") @QueryParam("size") Integer size
,@ApiParam(value = "sort: the sort pattern {name:ASC}", defaultValue="name:ASC") @DefaultValue("name:ASC") @QueryParam("sort") String sort
,@Context SecurityContext securityContext,@Context UriInfo info)
    throws NotFoundException {
        return delegate.listTags(by,page,size,sort,securityContext,info);
    }
    @PUT
    @Path("/{name}")
    
    @Produces({ "application/json", "application/xml" })
    @io.swagger.annotations.ApiOperation(value = "Update a TagDto by name", notes = "This method will search for a tag with the given name, and update its content for the provided body fields. Only the following fields can be updated: description, timeType, objectTime, endOfValidity, lastValidatedTime.", response = TagDto.class, tags={ "tags", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = TagDto.class) })
    public Response updateTag(@ApiParam(value = "name: the tag name",required=true) @PathParam("name") String name
,@ApiParam(value = "A json string that is used to construct a map of updatable fields: { description: xxx, ... }" ,required=true) GenericMap body
,@Context SecurityContext securityContext,@Context UriInfo info)
    throws NotFoundException {
        return delegate.updateTag(name,body,securityContext,info);
    }
    @PUT
    @Path("/{name}/meta")
    
    @Produces({ "application/json", "application/xml" })
    @io.swagger.annotations.ApiOperation(value = "Update a TagMetaDto by name", notes = "This method will search for a tag with the given name, and update its content for the provided body fields. Only the following fields can be updated: description, timeType, objectTime, endOfValidity, lastValidatedTime.", response = TagMetaDto.class, tags={ "tags", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = TagMetaDto.class) })
    public Response updateTagMeta(@ApiParam(value = "name: the tag name",required=true) @PathParam("name") String name
,@ApiParam(value = "A json string that is used to construct a map of updatable fields: { description: xxx, ... }" ,required=true) GenericMap body
,@Context SecurityContext securityContext,@Context UriInfo info)
    throws NotFoundException {
        return delegate.updateTagMeta(name,body,securityContext,info);
    }
}
