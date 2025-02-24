package hep.crest.server.swagger.api;

import hep.crest.server.swagger.api.*;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;

import hep.crest.server.swagger.model.HTTPResponse;
import hep.crest.server.swagger.model.IovDto;
import hep.crest.server.swagger.model.IovPayloadSetDto;
import hep.crest.server.swagger.model.IovSetDto;
import hep.crest.server.swagger.model.TagSummarySetDto;

import java.util.List;
import hep.crest.server.swagger.api.NotFoundException;

import java.io.InputStream;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.validation.constraints.*;
//// @jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJerseyServerCodegen")
public abstract class IovsApiService {
    public abstract Response findAllIovs( @NotNull String method,String tagname,Long snapshot,String since,String until,String timeformat,Long groupsize,String hash,Integer page,Integer size,String sort,String xCrestQuery,String xCrestSince,SecurityContext securityContext) throws NotFoundException;
    public abstract Response getSizeByTag( @NotNull String tagname,SecurityContext securityContext) throws NotFoundException;
    public abstract Response selectIovPayloads( @NotNull String tagname,String since,String until,String timeformat,Integer page,Integer size,String sort,SecurityContext securityContext) throws NotFoundException;
    public abstract Response storeIovBatch(IovSetDto iovSetDto,SecurityContext securityContext) throws NotFoundException;
    public abstract Response storeIovOne(IovDto iovDto,SecurityContext securityContext) throws NotFoundException;
}
