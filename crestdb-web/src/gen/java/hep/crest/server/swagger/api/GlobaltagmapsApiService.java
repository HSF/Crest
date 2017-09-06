package hep.crest.server.swagger.api;

import hep.crest.server.swagger.api.*;
import hep.crest.swagger.model.*;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import hep.crest.swagger.model.GlobalTagMapDto;

import java.util.List;
import hep.crest.server.swagger.api.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import javax.validation.constraints.*;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-09-06T09:44:28.040+02:00")
public abstract class GlobaltagmapsApiService {
    public abstract Response createGlobalTagMap(GlobalTagMapDto body,SecurityContext securityContext, UriInfo info) throws NotFoundException;
    public abstract Response findGlobalTagMap(String name,SecurityContext securityContext, UriInfo info) throws NotFoundException;
}
