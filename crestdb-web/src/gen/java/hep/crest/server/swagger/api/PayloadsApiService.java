package hep.crest.server.swagger.api;

import hep.crest.server.swagger.api.*;
import hep.crest.swagger.model.*;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import java.io.File;
import hep.crest.swagger.model.HTTPResponse;
import hep.crest.swagger.model.PayloadDto;

import java.util.List;
import hep.crest.server.swagger.api.NotFoundException;

import java.io.InputStream;
import java.math.BigDecimal;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import javax.validation.constraints.*;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2018-12-17T23:42:37.030+01:00")
public abstract class PayloadsApiService {
    public abstract Response createPayload(PayloadDto body,SecurityContext securityContext, UriInfo info) throws NotFoundException;
    public abstract Response createPayloadMultiForm(InputStream fileInputStream, FormDataContentDisposition fileDetail,FormDataBodyPart payload,SecurityContext securityContext, UriInfo info) throws NotFoundException;
    public abstract Response getBlob(String hash,SecurityContext securityContext, UriInfo info) throws NotFoundException;
    public abstract Response getPayload(String hash,String format,SecurityContext securityContext, UriInfo info) throws NotFoundException;
    public abstract Response getPayloadMetaInfo(String hash,SecurityContext securityContext, UriInfo info) throws NotFoundException;
    public abstract Response storePayloadWithIovMultiForm(InputStream fileInputStream, FormDataContentDisposition fileDetail,String tag,BigDecimal since,String format,BigDecimal endtime,SecurityContext securityContext, UriInfo info) throws NotFoundException;
}
