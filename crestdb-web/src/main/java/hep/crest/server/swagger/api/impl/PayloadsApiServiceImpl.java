package hep.crest.server.swagger.api.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import hep.crest.data.config.CrestProperties;
import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.server.annotations.CacheControlCdb;
import hep.crest.server.services.IovService;
import hep.crest.server.services.PayloadService;
import hep.crest.server.services.TagService;
import hep.crest.server.swagger.api.ApiResponseMessage;
import hep.crest.server.swagger.api.NotFoundException;
import hep.crest.server.swagger.api.PayloadsApiService;
import hep.crest.swagger.model.HTTPResponse;
import hep.crest.swagger.model.IovDto;
import hep.crest.swagger.model.PayloadDto;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-09-05T16:23:23.401+02:00")
@Component
public class PayloadsApiServiceImpl extends PayloadsApiService {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private PayloadService payloadService;
	@Autowired
	private IovService iovService;
	@Autowired
	TagService tagService;

	@Autowired
	private CrestProperties cprops;

    @Override
    public Response createPayload(PayloadDto body, SecurityContext securityContext, UriInfo info) throws NotFoundException {
		try {
			PayloadDto saved = payloadService.insertPayload(body);
			log.debug("Saved PayloadDto {}",saved);
			return Response.created(info.getRequestUri()).entity(saved).build();
		} catch (CdbServiceException e) {
			log.error("Error saving PayloadDto {}",body);
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
	@CacheControlCdb("public, max-age=604800")
	public Response getPayload(String hash, String format, SecurityContext securityContext, UriInfo info)
			throws NotFoundException {
		this.log.info("PayloadRestController processing request to download payload {} using format {}",hash,format);
		try {
			if (format == null || format.equals("BLOB")) {
				InputStream in = payloadService.getPayloadData(hash);
				StreamingOutput stream = 
						new StreamingOutput() {
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
							if (os != null)
								os.close();
							if (in != null)
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
			} else {
				log.debug("Retrieve the full pojo with hash {}",hash);
				PayloadDto entity = payloadService.getPayload(hash);
				if (entity == null) {
					String msg = "Cannot find payload corresponding to hash " + hash;
					ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
					return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
				}		
				return Response.ok(entity,MediaType.APPLICATION_JSON_TYPE).build();
			}
		} catch (CdbServiceException e) {
			String msg = "Error retrieving payload from hash " + hash;
			ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
		}
	}
	
	@Override
	public Response storePayloadWithIovMultiForm(InputStream fileInputStream, FormDataContentDisposition fileDetail,
			String tag, BigDecimal since, String format, BigDecimal endtime, SecurityContext securityContext, UriInfo info)
			throws NotFoundException {
		this.log.info("PayloadRestController processing request to store payload in tag {} and at since {}",tag,since);
		try {
			String filename = cprops.getDumpdir() +"/"+tag+"_"+since+".blob";
			File tempfile = new File(filename);
			Path temppath = Paths.get(filename);
			String hash = payloadService.saveInputStreamGetHash(fileInputStream, filename);
			if (format == null) {
				format = "JSON";
			}
			log.debug("Create dto with hash {},  format {}, ...",hash,format);
			PayloadDto payloaddto = new PayloadDto().hash(hash).objectType(format).streamerInfo(format.getBytes()).version("test");
			InputStream is = new FileInputStream(tempfile);
		    FileChannel tempchan = FileChannel.open(temppath);
		    payloaddto.setSize((int)(tempchan.size()));
		    PayloadDto saved = payloadService.insertPayloadAndInputStream(payloaddto,is);
		    IovDto iovdto = new IovDto().payloadHash(hash).tagName(tag).since(since);
			IovDto savediov = iovService.insertIov(iovdto);
			log.debug("Create payload {} and iov {} ",saved,savediov);
			
			Files.deleteIfExists(temppath);
			HTTPResponse resp = new HTTPResponse().action("storePayloadWithIovMultiForm").code(Response.Status.CREATED.getStatusCode()).id(hash).message("Created new entry in tag "+tag);
			return Response.created(info.getRequestUri()).entity(resp).build();
			
		} catch (CdbServiceException | IOException e) {
			String msg = "Error creating payload resource using " + tag.toString() + " : "+e.getMessage();
			ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
		}
	}
	
	@Override
    public Response getBlob(String hash, SecurityContext securityContext, UriInfo info) throws NotFoundException {
		this.log.info("PayloadRestController processing request to download blob {}",hash);
		StreamingOutput stream = null;
		try {
			InputStream in = payloadService.getPayloadData(hash);
			stream = new StreamingOutput() {
				@Override
				public void write(OutputStream os) throws IOException, WebApplicationException {
					try {
						int read = 0;
						byte[] bytes = new byte[2048];

						while ((read = in.read(bytes)) != -1) {
							os.write(bytes, 0, read);
							log.trace("Copying {} bytes into the output...",read);
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
    public Response getPayloadMetaInfo(String hash, SecurityContext securityContext, UriInfo info) throws NotFoundException {
		this.log.info("PayloadRestController processing request for payload meta information for {}",hash);
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
