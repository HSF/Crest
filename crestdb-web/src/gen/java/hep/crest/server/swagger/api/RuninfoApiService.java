package hep.crest.server.swagger.api;

import hep.crest.server.swagger.api.*;
import hep.crest.swagger.model.*;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import hep.crest.swagger.model.RunLumiInfoDto;

import java.util.List;
import hep.crest.server.swagger.api.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import javax.validation.constraints.*;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2018-01-14T18:09:32.330+01:00")
public abstract class RuninfoApiService {
    public abstract Response createRunLumiInfo(RunLumiInfoDto body,SecurityContext securityContext, UriInfo info) throws NotFoundException;
    public abstract Response findRunLumiInfo( String from, String to, String format, Integer page, Integer size, String sort,SecurityContext securityContext, UriInfo info) throws NotFoundException;
    public abstract Response listRunLumiInfo( String by, Integer page, Integer size, String sort,SecurityContext securityContext, UriInfo info) throws NotFoundException;
}
