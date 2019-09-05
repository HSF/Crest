package hep.crest.server.swagger.api;

import hep.crest.swagger.model.*;
import hep.crest.server.swagger.api.GlobaltagmapsApiService;

import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.*;

import hep.crest.swagger.model.GlobalTagMapDto;

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

@Path("/globaltagmaps")


@io.swagger.annotations.Api(description = "the globaltagmaps API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2019-09-05T14:30:55.225+02:00")
public class GlobaltagmapsApi  {
	@Autowired
	private GlobaltagmapsApiService delegate;

    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Create a GlobalTagMap in the database.", notes = "This method allows to insert a GlobalTag.Arguments: GlobalTagMapDto should be provided in the body as a JSON file.", response = GlobalTagMapDto.class, tags={ "globaltagmaps", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = GlobalTagMapDto.class) })
    public Response createGlobalTagMap(@ApiParam(value = "A json string that is used to construct a globaltagmapdto object: { globaltagname: xxx, ... }" ,required=true) GlobalTagMapDto body
,@Context SecurityContext securityContext,@Context UriInfo info)
    throws NotFoundException {
        return delegate.createGlobalTagMap(body,securityContext,info);
    }
    @GET
    @Path("/{name}")
    
    @Produces({ "application/json", "application/xml" })
    @io.swagger.annotations.ApiOperation(value = "Find GlobalTagMapDto lists.", notes = "This method search for mappings using the global tag name.", response = GlobalTagMapDto.class, responseContainer = "List", tags={ "globaltagmaps", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = GlobalTagMapDto.class, responseContainer = "List") })
    public Response findGlobalTagMap(@ApiParam(value = "",required=true) @PathParam("name") String name
,@ApiParam(value = "If the mode is BackTrace then it will search for global tags containing the tag <name>" , defaultValue="Trace")@HeaderParam("X-Crest-MapMode") String xCrestMapMode
,@Context SecurityContext securityContext,@Context UriInfo info)
    throws NotFoundException {
        return delegate.findGlobalTagMap(name,xCrestMapMode,securityContext,info);
    }
}
