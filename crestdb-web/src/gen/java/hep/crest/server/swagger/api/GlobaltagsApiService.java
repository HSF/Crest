package hep.crest.server.swagger.api;

import hep.crest.server.swagger.api.*;
import hep.crest.swagger.model.*;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import hep.crest.swagger.model.GlobalTagDto;
import hep.crest.swagger.model.TagDto;

import java.util.List;
import hep.crest.server.swagger.api.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import javax.validation.constraints.*;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-09-05T16:34:08.877+02:00")
public abstract class GlobaltagsApiService {
    public abstract Response createGlobalTag(GlobalTagDto body, String force,SecurityContext securityContext, UriInfo info) throws NotFoundException;
    public abstract Response findGlobalTag(String name,SecurityContext securityContext, UriInfo info) throws NotFoundException;
    public abstract Response findGlobalTagFetchTags(String name, String record, String label,SecurityContext securityContext, UriInfo info) throws NotFoundException;
    public abstract Response listGlobalTags( String by, Integer page, Integer size, String sort,SecurityContext securityContext, UriInfo info) throws NotFoundException;
}
