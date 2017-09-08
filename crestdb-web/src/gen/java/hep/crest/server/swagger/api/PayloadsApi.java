package hep.crest.server.swagger.api;

import hep.crest.swagger.model.*;
import hep.crest.server.swagger.api.PayloadsApiService;
/////import hep.crest.server.swagger.api.factories.PayloadsApiServiceFactory;

import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.*;

import java.io.File;
import hep.crest.swagger.model.PayloadDto;

import java.util.List;
import hep.crest.server.swagger.api.NotFoundException;

import java.io.InputStream;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.*;
import javax.validation.constraints.*;

@Path("/payloads")


@io.swagger.annotations.Api(description = "the payloads API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-09-06T09:44:28.040+02:00")
public class PayloadsApi  {
//   private final PayloadsApiService delegate = PayloadsApiServiceFactory.getPayloadsApi();

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
    @Path("/{hash}/data")
    
    @Produces({ "application/octet-stream" })
    @io.swagger.annotations.ApiOperation(value = "Finds payload data by hash; the payload object contains the real BLOB.", notes = "Select one payload at the time, no regexp searches allowed here", response = String.class, tags={ "payloads", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = String.class) })
    public Response getBlob(@ApiParam(value = "hash of the payload",required=true) @PathParam("hash") String hash
,@Context SecurityContext securityContext,@Context UriInfo info)
    throws NotFoundException {
        return delegate.getBlob(hash,securityContext,info);
    }
    @GET
    @Path("/{hash}")
    
    @Produces({ "application/json", "application/xml" })
    @io.swagger.annotations.ApiOperation(value = "Finds a payload resource associated to the hash.", notes = "This method retrieves a payload resource.Arguments: hash=<hash> the hash of the payload", response = PayloadDto.class, tags={ "payloads", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = PayloadDto.class) })
    public Response getPayload(@ApiParam(value = "hash:  the hash of the payload",required=true) @PathParam("hash") String hash
,@Context SecurityContext securityContext,@Context UriInfo info)
    throws NotFoundException {
        return delegate.getPayload(hash,securityContext,info);
    }
    @GET
    @Path("/{hash}/meta")
    
    @Produces({ "application/json", "application/xml" })
    @io.swagger.annotations.ApiOperation(value = "Finds a payload resource associated to the hash.", notes = "This method retrieves metadata of the payload resource.Arguments: hash=<hash> the hash of the payload", response = PayloadDto.class, tags={ "payloads", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = PayloadDto.class) })
    public Response getPayloadMetaInfo(@ApiParam(value = "hash:  the hash of the payload",required=true) @PathParam("hash") String hash
,@Context SecurityContext securityContext,@Context UriInfo info)
    throws NotFoundException {
        return delegate.getPayloadMetaInfo(hash,securityContext,info);
    }
}
