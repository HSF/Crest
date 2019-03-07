package hep.crest.server.swagger.api;

import hep.crest.swagger.model.*;
import hep.crest.server.swagger.api.FoldersApiService;

import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.*;

import hep.crest.swagger.model.FolderDto;

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

@Path("/folders")


@io.swagger.annotations.Api(description = "the folders API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2019-03-07T17:03:27.462+01:00")
public class FoldersApi  {
	@Autowired
	private FoldersApiService delegate;

    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/plain" })
    @io.swagger.annotations.ApiOperation(value = "Create an entry for folder information.", notes = "Folder informations go into a dedicated table.", response = String.class, tags={ "folders", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = String.class) })
    public Response createFolder(@ApiParam(value = "A json string that is used to construct a folderdto object: { node: xxx, ... }" ,required=true) FolderDto body
,@Context SecurityContext securityContext,@Context UriInfo info)
    throws NotFoundException {
        return delegate.createFolder(body,securityContext,info);
    }
    @GET
    
    
    @Produces({ "application/json", "application/xml" })
    @io.swagger.annotations.ApiOperation(value = "Finds a FolderDto list.", notes = "This method allows to perform search and sorting.Arguments: by=<pattern>, sort=<sortpattern>. The pattern <pattern> is in the form <param-name><operation><param-value>       <param-name> is the name of one of the fields in the dto       <operation> can be [< : >] ; for string use only [:]        <param-value> depends on the chosen parameter. A list of this criteria can be provided       using comma separated strings for <pattern>.      The pattern <sortpattern> is <field>:[DESC|ASC]", response = FolderDto.class, responseContainer = "List", tags={ "folders", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = FolderDto.class, responseContainer = "List") })
    public Response listFolders(@ApiParam(value = "by: the search pattern {none}", defaultValue="none") @DefaultValue("none") @QueryParam("by") String by
,@ApiParam(value = "sort: the sort pattern {nodeFullpath:ASC}", defaultValue="nodeFullpath:ASC") @DefaultValue("nodeFullpath:ASC") @QueryParam("sort") String sort
,@Context SecurityContext securityContext,@Context UriInfo info)
    throws NotFoundException {
        return delegate.listFolders(by,sort,securityContext,info);
    }
}
