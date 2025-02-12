package hep.crest.server.swagger.api;

import hep.crest.server.swagger.api.*;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;

import hep.crest.server.swagger.model.GlobalTagDto;
import hep.crest.server.swagger.model.GlobalTagSetDto;
import hep.crest.server.swagger.model.HTTPResponse;
import hep.crest.server.swagger.model.TagSetDto;

import java.util.List;
import hep.crest.server.swagger.api.NotFoundException;

import java.io.InputStream;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.validation.constraints.*;
//// @jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJerseyServerCodegen")
public abstract class GlobaltagsApiService {
    public abstract Response createGlobalTag(String force,GlobalTagDto globalTagDto,SecurityContext securityContext) throws NotFoundException;
    public abstract Response findGlobalTag(String name,SecurityContext securityContext) throws NotFoundException;
    public abstract Response findGlobalTagFetchTags(String name,String record,String label,SecurityContext securityContext) throws NotFoundException;
    public abstract Response listGlobalTags(String name,String workflow,String scenario,String release,Long validity,String description,Integer page,Integer size,String sort,SecurityContext securityContext) throws NotFoundException;
}
