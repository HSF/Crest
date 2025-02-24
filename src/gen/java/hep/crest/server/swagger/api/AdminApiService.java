package hep.crest.server.swagger.api;

import hep.crest.server.swagger.api.*;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;

import hep.crest.server.swagger.model.GlobalTagDto;
import hep.crest.server.swagger.model.HTTPResponse;

import java.util.List;
import hep.crest.server.swagger.api.NotFoundException;

import java.io.InputStream;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.validation.constraints.*;
//// @jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJerseyServerCodegen")
public abstract class AdminApiService {
    public abstract Response removeGlobalTag(String name,SecurityContext securityContext) throws NotFoundException;
    public abstract Response removeTag(String name,SecurityContext securityContext) throws NotFoundException;
    public abstract Response updateGlobalTag(String name,GlobalTagDto globalTagDto,SecurityContext securityContext) throws NotFoundException;
}
