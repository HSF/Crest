package hep.crest.server.swagger.api;

import hep.crest.server.swagger.api.*;
import hep.crest.swagger.model.*;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import java.io.File;
import hep.crest.swagger.model.PayloadDto;

import java.util.List;
import hep.crest.server.swagger.api.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import javax.validation.constraints.*;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-09-06T09:44:28.040+02:00")
public abstract class PayloadsApiService {
    public abstract Response createPayload(PayloadDto body,SecurityContext securityContext, UriInfo info) throws NotFoundException;
    public abstract Response createPayloadMultiForm(InputStream fileInputStream, FormDataContentDisposition fileDetail,String payload,SecurityContext securityContext, UriInfo info) throws NotFoundException;
    public abstract Response getBlob(String hash,SecurityContext securityContext, UriInfo info) throws NotFoundException;
    public abstract Response getPayload(String hash,SecurityContext securityContext, UriInfo info) throws NotFoundException;
    public abstract Response getPayloadMetaInfo(String hash,SecurityContext securityContext, UriInfo info) throws NotFoundException;
}
