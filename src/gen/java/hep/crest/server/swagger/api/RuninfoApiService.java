package hep.crest.server.swagger.api;

import hep.crest.server.swagger.api.*;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;

import hep.crest.server.swagger.model.RunLumiInfoDto;
import hep.crest.server.swagger.model.RunLumiSetDto;

import java.util.List;
import hep.crest.server.swagger.api.NotFoundException;

import java.io.InputStream;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.validation.constraints.*;
//// @jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJerseyServerCodegen")
public abstract class RuninfoApiService {
    public abstract Response createRunInfo(RunLumiSetDto runLumiSetDto,SecurityContext securityContext) throws NotFoundException;
    public abstract Response listRunInfo(String since,String until,String format,String mode,Integer page,Integer size,String sort,SecurityContext securityContext) throws NotFoundException;
    public abstract Response updateRunInfo(RunLumiInfoDto runLumiInfoDto,SecurityContext securityContext) throws NotFoundException;
}
