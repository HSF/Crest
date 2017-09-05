package hep.crest.server.swagger.api.impl;

import hep.crest.server.swagger.api.*;
import hep.crest.swagger.model.*;

import hep.crest.swagger.model.GlobalTagDto;
import hep.crest.swagger.model.TagDto;

import java.util.List;
import hep.crest.server.swagger.api.NotFoundException;

import java.io.InputStream;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import org.springframework.stereotype.Component;

import javax.validation.constraints.*;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-09-05T16:23:23.401+02:00")
@Component
public class GlobaltagsApiServiceImpl extends GlobaltagsApiService {
    @Override
    public Response createGlobalTag(GlobalTagDto body,  String force, SecurityContext securityContext, UriInfo info) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response findGlobalTag(String name, SecurityContext securityContext, UriInfo info) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response findGlobalTagFetchTags(String name,  String record,  String label, SecurityContext securityContext, UriInfo info) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response listGlobalTags( String by,  Integer page,  Integer size,  String sort, SecurityContext securityContext, UriInfo info) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
