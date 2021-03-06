package hep.crest.server.swagger.api;

import hep.crest.server.swagger.api.*;
import hep.crest.swagger.model.*;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import hep.crest.swagger.model.RunLumiSetDto;

import java.util.List;
import hep.crest.server.swagger.api.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import javax.validation.constraints.*;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2020-05-12T22:36:06.312+02:00")
public abstract class RuninfoApiService {
    public abstract Response createRunInfo(RunLumiSetDto body,SecurityContext securityContext, UriInfo info) throws NotFoundException;
    public abstract Response listRunInfo( String by, Integer page, Integer size, String sort,SecurityContext securityContext, UriInfo info) throws NotFoundException;
    public abstract Response selectRunInfo( String from, String to, String format, String mode,SecurityContext securityContext, UriInfo info) throws NotFoundException;
}
