package hep.crest.server.swagger.api;

import hep.crest.server.swagger.api.*;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;

import hep.crest.server.swagger.model.GlobalTagMapDto;
import hep.crest.server.swagger.model.GlobalTagMapSetDto;
import hep.crest.server.swagger.model.HTTPResponse;

import java.util.List;
import hep.crest.server.swagger.api.NotFoundException;

import java.io.InputStream;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.validation.constraints.*;
//// @jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJerseyServerCodegen")
public abstract class GlobaltagmapsApiService {
    public abstract Response createGlobalTagMap(GlobalTagMapDto globalTagMapDto,SecurityContext securityContext) throws NotFoundException;
    public abstract Response deleteGlobalTagMap(String name, @NotNull String label, @NotNull String tagname,String record,SecurityContext securityContext) throws NotFoundException;
    public abstract Response findGlobalTagMap(String name,String xCrestMapMode,SecurityContext securityContext) throws NotFoundException;
}
