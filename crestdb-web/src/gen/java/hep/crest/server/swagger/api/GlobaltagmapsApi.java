package hep.crest.server.swagger.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.springframework.beans.factory.annotation.Autowired;

import hep.crest.swagger.model.GlobalTagMapDto;
import hep.crest.swagger.model.GlobalTagMapSetDto;
import io.swagger.annotations.ApiParam;

@Path("/globaltagmaps")


@io.swagger.annotations.Api(description = "the globaltagmaps API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2019-10-04T10:30:37.214+02:00")
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
    @io.swagger.annotations.ApiOperation(value = "Find GlobalTagMapDto lists.", notes = "This method search for mappings using the global tag name.", response = GlobalTagMapSetDto.class, tags={ "globaltagmaps", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = GlobalTagMapSetDto.class) })
    public Response findGlobalTagMap(@ApiParam(value = "",required=true) @PathParam("name") String name
,@ApiParam(value = "If the mode is BackTrace then it will search for global tags containing the tag <name>" , defaultValue="Trace")@HeaderParam("X-Crest-MapMode") String xCrestMapMode
,@Context SecurityContext securityContext,@Context UriInfo info)
    throws NotFoundException {
        return delegate.findGlobalTagMap(name,xCrestMapMode,securityContext,info);
    }
}
