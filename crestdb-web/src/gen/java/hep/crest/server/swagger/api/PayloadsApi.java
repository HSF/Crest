package hep.crest.server.swagger.api;

import hep.crest.swagger.model.HTTPResponse;
import hep.crest.swagger.model.IovSetDto;
import hep.crest.swagger.model.PayloadDto;
import hep.crest.swagger.model.PayloadSetDto;
import io.swagger.annotations.ApiParam;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

@Path("/payloads")


@io.swagger.annotations.Api(description = "the payloads API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2019-03-28T10:58:03.879+01:00")
public class PayloadsApi  {
	@Autowired
	private PayloadsApiService delegate;

    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Create a Payload in the database.", notes = "This method allows to insert a Payload.Arguments: PayloadDto should be provided in the body as a JSON file.", response = PayloadDto.class, tags={ "payloads", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = PayloadDto.class) })
    public Response createPayload(@ApiParam(value = "A json string that is used to construct a iovdto object: { name: xxx, ... }" ,required=true) PayloadDto body
,@Context SecurityContext securityContext,@Context UriInfo info)
    throws NotFoundException {
        return delegate.createPayload(body,securityContext,info);
    }
    @POST
    @Path("/upload")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Create a Payload in the database.", notes = "This method allows to insert a Payload.Arguments: PayloadDto should be provided in the body as a JSON file.", response = PayloadDto.class, tags={ "payloads", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = PayloadDto.class) })
    public Response createPayloadMultiForm(
            @FormDataParam("file") InputStream fileInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail
,@ApiParam(value = "Json body for payloaddto", required=true)@FormDataParam("payload")  FormDataBodyPart payload
,@Context SecurityContext securityContext,@Context UriInfo info)
    throws NotFoundException {
        return delegate.createPayloadMultiForm(fileInputStream, fileDetail,payload,securityContext,info);
    }

    @GET
    @Path("/{hash}")
    
    @Produces({ "application/octet-stream", "application/json", "application/xml" })
    @io.swagger.annotations.ApiOperation(value = "Finds a payload resource associated to the hash.", notes = "This method retrieves a payload resource.Arguments: hash=<hash> the hash of the payload", response = String.class, tags={ "payloads", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = String.class) })
    public Response getPayload(@ApiParam(value = "hash:  the hash of the payload",required=true) @PathParam("hash") String hash
,@ApiParam(value = "The format of the output data. The header parameter X-Crest-PayloadFormat can be : BLOB (default) or DTO (in JSON format)." , defaultValue="BLOB")@HeaderParam("X-Crest-PayloadFormat") String xCrestPayloadFormat
,@Context SecurityContext securityContext,@Context UriInfo info)
    throws NotFoundException {
        return delegate.getPayload(hash,xCrestPayloadFormat,securityContext,info);
    }
    @GET
    @Path("/{hash}/meta")
    
    @Produces({ "application/json", "application/xml" })
    @io.swagger.annotations.ApiOperation(value = "Finds a payload resource associated to the hash.", notes = "This method retrieves metadata of the payload resource.Arguments: hash=<hash> the hash of the payload", response = PayloadSetDto.class, tags={ "payloads", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = PayloadSetDto.class) })
    public Response getPayloadMetaInfo(@ApiParam(value = "hash:  the hash of the payload",required=true) @PathParam("hash") String hash
,@Context SecurityContext securityContext,@Context UriInfo info)
    throws NotFoundException {
        return delegate.getPayloadMetaInfo(hash,securityContext,info);
    }
    
    @POST
    @Path("/storebatch")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Create many Payloads in the database, associated to a given iov since list and tag name.", notes = "This method allows to insert a Payload and an IOV. Arguments: tagname,stream,end time. The header parameter X-Crest-PayloadFormat can be : JSON (default) or TXT or BLOB", response = IovSetDto.class, tags={ "payloads", })
    @io.swagger.annotations.ApiResponses(value = {
        @io.swagger.annotations.ApiResponse(code = 201, message = "successful operation", response = IovSetDto.class) })
    public Response storePayloadBatchWithIovMultiForm(@ApiParam(value = "The tag name", required=true)@FormDataParam("tag")  String tag
,@ApiParam(value = "", required=true)@FormDataParam("iovsetupload")  FormDataBodyPart iovsetupload
,@ApiParam(value = "The format of the input data" , defaultValue="JSON")@HeaderParam("X-Crest-PayloadFormat") String xCrestPayloadFormat
,@ApiParam(value = "The payload objectType")@FormDataParam("objectType")  String objectType
,@ApiParam(value = "The version")@FormDataParam("version")  String version
,@ApiParam(value = "The end time to be used for protection at tag level")@FormDataParam("endtime")  BigDecimal endtime
,@ApiParam(value = "The streamerInfo CLOB as a string")@FormDataParam("streamerInfo")  String streamerInfo
,@Context SecurityContext securityContext,@Context UriInfo info)
    throws NotFoundException {
        return delegate.storePayloadBatchWithIovMultiForm(tag,iovsetupload,xCrestPayloadFormat,objectType,version,
                endtime, streamerInfo,
                securityContext,info);
    }
    
    @POST
    @Path("/uploadbatch")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Create many Payloads in the database, associated to a given iov since list and tag name.", notes = "This method allows to insert a Payload and an IOV. Arguments: tagname,stream,end time. The header parameter X-Crest-PayloadFormat can be : JSON (default) or TXT or BLOB", response = IovSetDto.class, tags={ "payloads", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "successful operation", response = IovSetDto.class) })
    public Response uploadPayloadBatchWithIovMultiForm(
            @FormDataParam("files") List<FormDataBodyPart> filesbodyparts,
            @FormDataParam("files") FormDataContentDisposition filesDetail
            ,@ApiParam(value = "The tag name", required=true)@FormDataParam("tag")  String tag
,@ApiParam(value = "", required=true)@FormDataParam("iovsetupload")  FormDataBodyPart iovsetupload
,@ApiParam(value = "The format of the input data" , defaultValue="FILE")@HeaderParam("X-Crest-PayloadFormat") String xCrestPayloadFormat
,@ApiParam(value = "The payload objectType")@FormDataParam("objectType")  String objectType
,@ApiParam(value = "The version")@FormDataParam("version")  String version
,@ApiParam(value = "The end time to be used for protection at tag level")@FormDataParam("endtime")  BigDecimal endtime
,@ApiParam(value = "The streamerInfo CLOB as a string")@FormDataParam("streamerInfo")  String streamerInfo
,@Context SecurityContext securityContext,@Context UriInfo info)
    throws NotFoundException {
        return delegate.uploadPayloadBatchWithIovMultiForm(filesbodyparts, filesDetail,tag,iovsetupload,
                xCrestPayloadFormat,objectType,version,endtime,streamerInfo,securityContext,info);
    }
    @POST
    @Path("/store")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Create a Payload in the database, associated to a given iov since and tag name.", notes = "This method allows to insert a Payload and an IOV. Arguments: since,tagname,stream,end time. The header parameter X-Crest-PayloadFormat can be : JSON (default) or TXT or BLOB", response = HTTPResponse.class, tags={ "payloads", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = HTTPResponse.class) })
    public Response storePayloadWithIovMultiForm(
            @FormDataParam("file") InputStream fileInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail
,@ApiParam(value = "The tag name", required=true)@FormDataParam("tag")  String tag
,@ApiParam(value = "The since time", required=true)@FormDataParam("since")  BigDecimal since
,@ApiParam(value = "The format of the input data" , defaultValue="JSON")@HeaderParam("X-Crest-PayloadFormat") String xCrestPayloadFormat
,@ApiParam(value = "The payload objectType")@FormDataParam("objectType")  String objectType
,@ApiParam(value = "The version")@FormDataParam("version")  String version
,@ApiParam(value = "The end time to be used for protection at tag level")@FormDataParam("endtime")  BigDecimal endtime
,@ApiParam(value = "The streamerInfo CLOB as a string")@FormDataParam("streamerInfo")  String streamerInfo
,@Context SecurityContext securityContext,@Context UriInfo info)
    throws NotFoundException {
        return delegate.storePayloadWithIovMultiForm(fileInputStream, fileDetail,tag,since,xCrestPayloadFormat,
                objectType, version, endtime, streamerInfo, securityContext,info);
    }
}
