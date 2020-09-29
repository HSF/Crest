package hep.crest.server.swagger.api;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import hep.crest.swagger.model.PayloadDto;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2019-03-28T10:58:03.879+01:00")
public abstract class PayloadsApiService {
    public abstract Response createPayload(PayloadDto body,SecurityContext securityContext, UriInfo info) throws NotFoundException;
    public abstract Response createPayloadMultiForm(InputStream fileInputStream, FormDataContentDisposition fileDetail,FormDataBodyPart payload,SecurityContext securityContext, UriInfo info) throws NotFoundException;
    public abstract Response getPayload(String hash,String xCrestPayloadFormat,SecurityContext securityContext, UriInfo info) throws NotFoundException;
    public abstract Response getPayloadMetaInfo(String hash,SecurityContext securityContext, UriInfo info) throws NotFoundException;
    public abstract Response uploadPayloadBatchWithIovMultiForm(List<FormDataBodyPart> filesbodyparts, FormDataContentDisposition filesDetail, String tag, FormDataBodyPart iovsetupload, String xCrestPayloadFormat,String objectType, String version, BigDecimal endtime,SecurityContext securityContext, UriInfo info) throws NotFoundException;
    public abstract Response storePayloadBatchWithIovMultiForm(String tag, FormDataBodyPart iovsetupload, String xCrestPayloadFormat,String objectType, String version, BigDecimal endtime,SecurityContext securityContext, UriInfo info) throws NotFoundException;
    public abstract Response storePayloadWithIovMultiForm(InputStream fileInputStream,
                                                          FormDataContentDisposition fileDetail,String tag,
                                                          BigDecimal since,String xCrestPayloadFormat,
                                                          String objectType, String version, BigDecimal endtime,
                                                          SecurityContext securityContext, UriInfo info) throws NotFoundException;
}
