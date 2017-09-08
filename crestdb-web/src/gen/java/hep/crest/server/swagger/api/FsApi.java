package hep.crest.server.swagger.api;

import hep.crest.swagger.model.*;
import hep.crest.server.swagger.api.FsApiService;
/////import hep.crest.server.swagger.api.factories.FsApiServiceFactory;

import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.*;


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
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.*;

@Path("/fs")


@io.swagger.annotations.Api(description = "the fs API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-09-08T10:40:47.444+02:00")
public class FsApi  {
//   private final FsApiService delegate = FsApiServiceFactory.getFsApi();

	@Autowired
	private FsApiService delegate;

    @POST
    @Path("/tar")
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/plain" })
    @io.swagger.annotations.ApiOperation(value = "Dump a tag into filesystem and retrieve the tar file asynchronously.", notes = "This method allows to request a tar file from the server using a tag specified in input.", response = String.class, tags={ "fs", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = String.class) })
    public Response buildTar(@ApiParam(value = "tagname: the tag name {none}",required=true, defaultValue="none") @DefaultValue("none") @QueryParam("tagname") String tagname
,@ApiParam(value = "snapshot: the snapshot time {0}",required=true, defaultValue="0") @DefaultValue("0") @QueryParam("snapshot") Long snapshot
,@Context SecurityContext securityContext,@Context UriInfo info, @Context HttpServletRequest request)
    throws NotFoundException {
        return delegate.buildTar(tagname,snapshot,securityContext,info,request);
    }
}
