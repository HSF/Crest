package hep.crest.server.swagger.api;

import hep.crest.server.swagger.api.*;
import hep.crest.swagger.model.*;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import hep.crest.swagger.model.GenericMap;
import hep.crest.swagger.model.TagDto;

import java.util.List;
import hep.crest.server.swagger.api.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import javax.validation.constraints.*;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-09-20T12:16:15.815+02:00")
public abstract class TagsApiService {
    public abstract Response createTag(TagDto body,SecurityContext securityContext, UriInfo info) throws NotFoundException;
    public abstract Response findTag(String name,SecurityContext securityContext, UriInfo info) throws NotFoundException;
    public abstract Response listTags( String by, Integer page, Integer size, String sort,SecurityContext securityContext, UriInfo info) throws NotFoundException;
    public abstract Response updateTag(String name,GenericMap body,SecurityContext securityContext, UriInfo info) throws NotFoundException;
}
