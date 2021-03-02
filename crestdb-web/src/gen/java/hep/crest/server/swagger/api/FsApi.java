package hep.crest.server.swagger.api;

import hep.crest.swagger.model.*;
import hep.crest.server.swagger.api.FsApiService;

import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.*;


import java.util.Map;
import java.util.List;
import hep.crest.server.swagger.api.NotFoundException;

import java.io.InputStream;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import javax.ws.rs.*;

import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.constraints.*;

@Path("/fs")


@io.swagger.annotations.Api(description = "the fs API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2018-01-14T18:09:32.330+01:00")
public class FsApi  {
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
    @POST
    @Path("/tag")
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/plain" })
    @io.swagger.annotations.ApiOperation(value = "Read a tag from filesystem. If the tag does not exists dump it.", notes = "This method allows to dump a tag on filesystem.", response = TagSetDto.class, tags={ "fs", })    @io.swagger.annotations.ApiResponses(value = {
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = TagSetDto.class) })
    public Response findTag(@ApiParam(value = "tagname: the tag name {none}",required=true, defaultValue="none") @DefaultValue("none") @QueryParam("tagname") String tagname
            ,@ApiParam(value = "reqid: the request id from tar method {none}",required=true, defaultValue="none") @DefaultValue("none") @QueryParam("reqid") String reqid
,@Context SecurityContext securityContext,@Context UriInfo info)
    throws NotFoundException {
        return delegate.findTag(tagname,reqid,securityContext,info);
    }
}
