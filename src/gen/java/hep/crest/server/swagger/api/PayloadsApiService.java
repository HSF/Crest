package hep.crest.server.swagger.api;

import hep.crest.server.swagger.api.*;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;

import java.io.File;
import hep.crest.server.swagger.model.GenericMap;
import hep.crest.server.swagger.model.HTTPResponse;
import hep.crest.server.swagger.model.PayloadDto;
import hep.crest.server.swagger.model.PayloadSetDto;
import hep.crest.server.swagger.model.StoreSetDto;

import java.util.List;
import hep.crest.server.swagger.api.NotFoundException;

import java.io.InputStream;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.validation.constraints.*;
//// @jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJerseyServerCodegen")
public abstract class PayloadsApiService {
    public abstract Response getPayload( @NotNull String hash, @NotNull String format,SecurityContext securityContext) throws NotFoundException;
    public abstract Response listPayloads(String hash,String objectType,Integer minsize,Integer page,Integer size,String sort,SecurityContext securityContext) throws NotFoundException;
    public abstract Response storePayloadBatch(String tag,String storeset,String xCrestPayloadFormat,List<FormDataBodyPart> filesBodypart,String objectType,String compressionType,String version,String endtime,SecurityContext securityContext) throws NotFoundException;
    public abstract Response updatePayload(String hash,GenericMap genericMap,SecurityContext securityContext) throws NotFoundException;
    public abstract Response uploadJson(String tag,FormDataBodyPart storesetBodypart,String objectType,String compressionType,String version,String endtime,SecurityContext securityContext) throws NotFoundException;
}
