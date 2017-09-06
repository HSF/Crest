package hep.crest.server.swagger.api;

import hep.crest.swagger.model.*;
import hep.crest.server.swagger.api.AdminApiService;
/////import hep.crest.server.swagger.api.factories.AdminApiServiceFactory;

import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.*;

import hep.crest.swagger.model.GlobalTagDto;

import java.util.List;
import hep.crest.server.swagger.api.NotFoundException;

import java.io.InputStream;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.*;
import javax.validation.constraints.*;

@Path("/admin")


@io.swagger.annotations.Api(description = "the admin API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-09-06T09:44:28.040+02:00")
public class AdminApi  {
//   private final AdminApiService delegate = AdminApiServiceFactory.getAdminApi();

	@Autowired
	private AdminApiService delegate;

    @DELETE
    @Path("/globaltags/{name}")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Remove a GlobalTag from the database.", notes = "This method allows to remove a GlobalTag.Arguments: the name has to uniquely identify a global tag.", response = void.class, tags={ "admin", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = void.class) })
    public Response removeGlobalTag(@ApiParam(value = "",required=true) @PathParam("name") String name
,@Context SecurityContext securityContext,@Context UriInfo info)
    throws NotFoundException {
        return delegate.removeGlobalTag(name,securityContext,info);
    }
    @DELETE
    @Path("/tags/{name}")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Remove a Tag from the database.", notes = "This method allows to remove a Tag.Arguments: the name has to uniquely identify a tag.", response = void.class, tags={ "admin", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = void.class) })
    public Response removeTag(@ApiParam(value = "",required=true) @PathParam("name") String name
,@Context SecurityContext securityContext,@Context UriInfo info)
    throws NotFoundException {
        return delegate.removeTag(name,securityContext,info);
    }
    @PUT
    @Path("/globaltags/{name}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update a GlobalTag in the database.", notes = "This method allows to update a GlobalTag.Arguments: the name has to uniquely identify a global tag.", response = GlobalTagDto.class, tags={ "admin", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = GlobalTagDto.class) })
    public Response updateGlobalTag(@ApiParam(value = "",required=true) @PathParam("name") String name
,@ApiParam(value = "A json string that is used to construct a GlobalTagDto object: { name: xxx, ... }" ,required=true) GlobalTagDto body
,@Context SecurityContext securityContext,@Context UriInfo info)
    throws NotFoundException {
        return delegate.updateGlobalTag(name,body,securityContext,info);
    }
}
