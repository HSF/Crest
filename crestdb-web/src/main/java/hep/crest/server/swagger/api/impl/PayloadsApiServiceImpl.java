package hep.crest.server.swagger.api.impl;

import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.server.annotations.CacheControlCdb;
import hep.crest.server.services.PayloadService;
import hep.crest.server.swagger.api.*;
import hep.crest.swagger.model.*;

import java.io.IOException;

import hep.crest.swagger.model.PayloadDto;

import java.io.InputStream;
import java.io.OutputStream;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.constraints.*;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-09-05T16:23:23.401+02:00")
@Component
public class PayloadsApiServiceImpl extends PayloadsApiService {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private PayloadService payloadService;

    @Override
    public Response createPayload(PayloadDto body, SecurityContext securityContext, UriInfo info) throws NotFoundException {
		try {
			PayloadDto saved = payloadService.insertPayload(body);
			return Response.created(info.getRequestUri()).entity(saved).build();
		} catch (CdbServiceException e) {
			String msg = "Error creating payload resource using " + body.toString();
			ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
		}
    }
    @Override
    public Response createPayloadMultiForm(InputStream fileInputStream, FormDataContentDisposition fileDetail, FormDataBodyPart payload, SecurityContext securityContext, UriInfo info) throws NotFoundException {
		this.log.info("PayloadRestController processing request to upload payload ");
		//PayloadDto payload = new PayloadDto();
		try {
			payload.setMediaType(MediaType.APPLICATION_JSON_TYPE);
			PayloadDto payloaddto = payload.getValueAs(PayloadDto.class);
			log.debug("Received body json "+payload);
		    PayloadDto saved = payloadService.insertPayloadAndInputStream(payloaddto,fileInputStream);
			return Response.created(info.getRequestUri()).entity(saved).build();
			
		} catch (CdbServiceException e) {
			String msg = "Error creating payload resource using " + payload.toString();
			ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
		}
    }
    
    @Override
    public Response getBlob(String hash, SecurityContext securityContext, UriInfo info) throws NotFoundException {
		this.log.info("PayloadRestController processing request to download payload " + hash);
		try {
			InputStream in = payloadService.getPayloadData(hash);
			StreamingOutput stream = new StreamingOutput() {
				@Override
				public void write(OutputStream os) throws IOException, WebApplicationException {
					try {
						int read = 0;
						byte[] bytes = new byte[2048];

						while ((read = in.read(bytes)) != -1) {
							os.write(bytes, 0, read);
							log.trace("Copying " + read + " bytes into the output...");
						}
						os.flush();
					} catch (Exception e) {
						throw new WebApplicationException(e);
					} finally {
						log.debug("closing streams...");
						os.close();
						in.close();
					}
				}
			};
			log.debug("Send back the stream....");
			return Response.ok(stream, "application/octet-stream") ///MediaType.APPLICATION_JSON_TYPE)
					.header("Content-Disposition", "attachment; filename=\"" + hash + ".blob\"")
					// .header("Content-Length", new
					// Long(f.length()).toString())
					.build();
		} catch (CdbServiceException e) {
			String msg = "Error retrieving payload from hash " + hash;
			ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
		}
    }
    
    @Override
	@CacheControlCdb("public, max-age=604800")
    public Response getPayload(String hash, SecurityContext securityContext, UriInfo info) throws NotFoundException {
		this.log.info("PayloadRestController processing request for payload " + hash);
		try {

			PayloadDto entity = payloadService.getPayload(hash);
			if (entity == null) {
				String msg = "Cannot find payload corresponding to hash " + hash;
				ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
				return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
			}
			return Response.ok().entity(entity).build();
			
		} catch (CdbServiceException e) {
			String msg = "Error retrieving payload from hash " + hash;
			ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
		}
    }
    
    @Override
    public Response getPayloadMetaInfo(String hash, SecurityContext securityContext, UriInfo info) throws NotFoundException {
		this.log.info("PayloadRestController processing request for payload meta information " + hash);
		try {

			PayloadDto entity = payloadService.getPayloadMetaInfo(hash);
			if (entity == null) {
				String msg = "Cannot find payload corresponding to hash " + hash;
				ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
				return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
			}
			return Response.ok().entity(entity).build();
		} catch (CdbServiceException e) {
			String msg = "Error retrieving payload from hash " + hash;
			ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
		}
    }
}
