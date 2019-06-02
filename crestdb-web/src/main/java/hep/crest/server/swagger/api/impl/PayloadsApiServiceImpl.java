package hep.crest.server.swagger.api.impl;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.media.multipart.BodyPartEntity;
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
import hep.crest.swagger.model.IovPayloadDto;
import hep.crest.swagger.model.IovSetDto;
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
	public Response createPayload(PayloadDto body, SecurityContext securityContext, UriInfo info)
			throws NotFoundException {
		try {
			PayloadDto saved = payloadService.insertPayload(body);
			log.debug("Saved PayloadDto {}", saved);
			return Response.created(info.getRequestUri()).entity(saved).build();
		} catch (CdbServiceException e) {
			log.error("Error saving PayloadDto {}", body);
			String msg = "Error creating payload resource using " + body.toString();
			ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
		}
	}

	@Override
	public Response createPayloadMultiForm(InputStream fileInputStream, FormDataContentDisposition fileDetail,
			FormDataBodyPart payload, SecurityContext securityContext, UriInfo info) throws NotFoundException {
		this.log.info("PayloadRestController processing request to upload payload ");
		// PayloadDto payload = new PayloadDto();
		try {
			payload.setMediaType(MediaType.APPLICATION_JSON_TYPE);
			PayloadDto payloaddto = payload.getValueAs(PayloadDto.class);
			log.debug("Received body json " + payload);
			PayloadDto saved = payloadService.insertPayloadAndInputStream(payloaddto, fileInputStream);
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
		this.log.info("PayloadRestController processing request to download payload {} using format {}", hash, format);
		try {
			PayloadDto pdto = payloadService.getPayloadMetaInfo(hash);
			String ptype = pdto.getObjectType();
			log.debug("Found metadata {}", pdto);

			MediaType media_type = getMediaType(ptype);

			if (format == null || format.equalsIgnoreCase("BLOB") || format.equalsIgnoreCase("BIN")) {
				InputStream in = payloadService.getPayloadData(hash);
				StreamingOutput stream = new StreamingOutput() {
					@Override
					public void write(OutputStream os) throws IOException, WebApplicationException {
						try {
							int read = 0;
							byte[] bytes = new byte[2048];

							while ((read = in.read(bytes)) != -1) {
								os.write(bytes, 0, read);
								log.trace("Copying {} bytes into the output...", read);
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
				String rettype = media_type.toString();
				String ext = getExtension(ptype);
				return Response.ok(stream) /// MediaType.APPLICATION_JSON_TYPE)
						.header("Content-type", rettype)
						.header("Content-Disposition", "Inline; filename=\"" + hash + ext + "\"")
						// .header("Content-Length", new
						// Long(f.length()).toString())
						.build();
			} else {
				log.debug("Retrieve the full pojo with hash {}", hash);
				PayloadDto entity = payloadService.getPayload(hash);
				if (entity == null) {
					String msg = "Cannot find payload corresponding to hash " + hash;
					ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
					return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
				}
				return Response.ok(entity, MediaType.APPLICATION_JSON_TYPE).build();
			}
		} catch (CdbServiceException e) {
			String msg = "Error retrieving payload from hash " + hash;
			ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
		}
	}

	@Override
	public Response storePayloadWithIovMultiForm(InputStream fileInputStream, FormDataContentDisposition fileDetail,
			String tag, BigDecimal since, String format, BigDecimal endtime, SecurityContext securityContext,
			UriInfo info) throws NotFoundException {
		this.log.info(
				"PayloadRestController processing request to store payload with iov in tag {} and at since {} using format {}",
				tag, since, format);
		try {
			String fdetailsname = fileDetail.getFileName();
			if (fdetailsname == null || fdetailsname.isEmpty()) {
				fdetailsname = ".blob";
			}
			String filename = cprops.getDumpdir() + File.pathSeparator + tag + "_" + since + "_" + fdetailsname;
			if (format == null) {
				format = "JSON";
			}

			PayloadDto pdto = new PayloadDto().objectType(format).streamerInfo(format.getBytes()).version("none");
			IovDto iovDto = new IovDto().payloadHash(pdto.getHash()).since(since).tagName(tag);
			IovDto saveddto = saveIovAndPayload(iovDto, filename, fileInputStream, pdto);

			// File tempfile = new File(filename);
			// Path temppath = Paths.get(filename);
			// String hash = payloadService.saveInputStreamGetHash(fileInputStream,
			// filename);
			// if (format == null) {
			// format = "JSON";
			// }
			// log.debug("Create dto with hash {}, format {}, use filename {} ", hash,
			// format, filename);
			// PayloadDto payloaddto = new
			// PayloadDto().hash(hash).objectType(format).streamerInfo(format.getBytes())
			// .version("test");
			// InputStream is = new FileInputStream(tempfile);
			// FileChannel tempchan = FileChannel.open(temppath);
			// payloaddto.setSize((int) (tempchan.size()));
			// PayloadDto saved = payloadService.insertPayloadAndInputStream(payloaddto,
			// is);
			// IovDto iovdto = new IovDto().payloadHash(hash).tagName(tag).since(since);
			// IovDto savediov = iovService.insertIov(iovdto);
			// log.debug("Create payload {} and iov {} ", saved, savediov);
			//
			// Files.deleteIfExists(temppath);
			// log.debug("Removed temporary file");
			HTTPResponse resp = new HTTPResponse().action("storePayloadWithIovMultiForm")
					.code(Response.Status.CREATED.getStatusCode()).id(saveddto.getPayloadHash())
					.message("Created new entry in tag " + tag);
			return Response.created(info.getRequestUri()).entity(resp).build();

		} catch (CdbServiceException | IOException e) {
			String msg = "Error creating payload resource using " + tag.toString() + " : " + e.getMessage();
			ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
		} catch (Exception e) {
			String msg = "Internal exception creating payload resource using storeWithIov " + tag.toString() + " : "
					+ e.getMessage();
			ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hep.crest.server.swagger.api.PayloadsApiService#
	 * storePayloadBatchWithIovMultiForm(java.util.List,
	 * org.glassfish.jersey.media.multipart.FormDataContentDisposition,
	 * java.lang.String, org.glassfish.jersey.media.multipart.FormDataBodyPart,
	 * java.lang.String, java.math.BigDecimal, javax.ws.rs.core.SecurityContext,
	 * javax.ws.rs.core.UriInfo)
	 */
	@Override
	public Response uploadPayloadBatchWithIovMultiForm(List<FormDataBodyPart> filesbodyparts,
			FormDataContentDisposition filesDetail, String tag, FormDataBodyPart iovsetupload,
			String xCrestPayloadFormat, BigDecimal endtime, SecurityContext securityContext, UriInfo info)
			throws NotFoundException {
		this.log.info("PayloadRestController processing request to upload payload batch in tag {} with multi-iov {}",
				tag, filesbodyparts.size());
		iovsetupload.setMediaType(MediaType.APPLICATION_JSON_TYPE);
		try {
			IovSetDto dto = iovsetupload.getValueAs(IovSetDto.class);
			log.info("Batch insertion of {} iovs using file formatted in {}", dto.getNiovs(), dto.getFormat());
			List<IovPayloadDto> iovlist = dto.getIovsList();
			List<IovPayloadDto> savediovlist = new ArrayList<>();
			if (xCrestPayloadFormat == null) {
				xCrestPayloadFormat = "FILE";
			}
			if (xCrestPayloadFormat.equals("FILE")) {
				for (IovPayloadDto piovDto : iovlist) {
					Map<String, Object> retmap = getDocumentStream(piovDto, filesbodyparts);
					PayloadDto pdto = new PayloadDto().objectType(dto.getFormat())
							.streamerInfo(dto.getFormat().getBytes()).version("none");
					IovDto iovDto = new IovDto().payloadHash(pdto.getHash()).since(piovDto.getSince()).tagName(tag);
					IovDto saveddto = saveIovAndPayload(iovDto, (String) retmap.get("file"),
							(InputStream) retmap.get("stream"), pdto);
					IovPayloadDto sd = new IovPayloadDto().since(saveddto.getSince())
							.payload(saveddto.getPayloadHash());
					savediovlist.add(sd);
				}
				dto.niovs((long) savediovlist.size());
				dto.iovsList(savediovlist);
				return Response.created(info.getRequestUri()).entity(dto).build();
			} else {
				throw new CdbServiceException("Wrong header parameter " + xCrestPayloadFormat);
			}

		} catch (CdbServiceException | IOException e) {
			String msg = "Error creating payload resource using " + tag.toString() + " : " + e.getMessage();
			ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
		} catch (Exception e) {
			String msg = "Internal exception creating payload resource using uploadBatch " + tag.toString() + " : "
					+ e.getMessage();
			ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hep.crest.server.swagger.api.PayloadsApiService#
	 * storePayloadBatchWithIovMultiForm(java.lang.String,
	 * org.glassfish.jersey.media.multipart.FormDataBodyPart, java.lang.String,
	 * java.math.BigDecimal, javax.ws.rs.core.SecurityContext,
	 * javax.ws.rs.core.UriInfo)
	 */
	@Override
	public Response storePayloadBatchWithIovMultiForm(String tag, FormDataBodyPart iovsetupload,
			String xCrestPayloadFormat, BigDecimal endtime, SecurityContext securityContext, UriInfo info)
			throws NotFoundException {
		this.log.info("PayloadRestController processing request to store payload batch in tag {} with multi-iov", tag);
		iovsetupload.setMediaType(MediaType.APPLICATION_JSON_TYPE);
		try {
			IovSetDto dto = iovsetupload.getValueAs(IovSetDto.class);
			log.info("Batch insertion of {} iovs using file formatted in {}", dto.getNiovs(), dto.getFormat());
			List<IovPayloadDto> iovlist = dto.getIovsList();
			List<IovPayloadDto> savediovlist = new ArrayList<>();
			if (xCrestPayloadFormat == null) {
				xCrestPayloadFormat = "JSON";
			}
			if (xCrestPayloadFormat.equalsIgnoreCase("JSON")) {
				for (IovPayloadDto piovDto : iovlist) {
					PayloadDto pdto = new PayloadDto().objectType(dto.getFormat()).hash("none")
							.streamerInfo(dto.getFormat().getBytes()).version("none")
							.data(piovDto.getPayload().getBytes());
					IovDto iovDto = new IovDto().payloadHash(pdto.getHash()).since(piovDto.getSince()).tagName(tag);
					IovDto saveddto = saveIovAndPayload(iovDto, pdto);
					IovPayloadDto sd = new IovPayloadDto().since(saveddto.getSince())
							.payload(saveddto.getPayloadHash());
					savediovlist.add(sd);
				}
				dto.niovs((long) savediovlist.size());
				dto.iovsList(savediovlist);
				return Response.created(info.getRequestUri()).entity(dto).build();

			} else {
				throw new CdbServiceException("Wrong header parameter " + xCrestPayloadFormat);
			}

		} catch (CdbServiceException | IOException e) {
			String msg = "Error creating payload resource using " + tag.toString() + " : " + e.getMessage();
			ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
		} catch (Exception e) {
			String msg = "Internal exception creating payload resource using storeBatch " + tag.toString() + " : "
					+ e.getMessage();
			ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
		}
	}

	@Transactional
	protected IovDto saveIovAndPayload(IovDto dto, String filename, InputStream fileInputStream, PayloadDto pdto)
			throws CdbServiceException, IOException {
		File tempfile = new File(filename);
		Path temppath = Paths.get(filename);
		String hash = payloadService.saveInputStreamGetHash(fileInputStream, filename);
		log.debug("Create dto with hash {},  format {}, ...", hash, pdto.getObjectType());

		pdto.hash(hash).streamerInfo(pdto.getObjectType().getBytes());
		InputStream is = new FileInputStream(tempfile);
		FileChannel tempchan = FileChannel.open(temppath);
		pdto.size((int) (tempchan.size()));
		PayloadDto saved = payloadService.insertPayloadAndInputStream(pdto, is);
		dto.payloadHash(hash);
		IovDto savediov = iovService.insertIov(dto);
		log.debug("Create payload {} and iov {} ", saved, savediov);
		Files.deleteIfExists(temppath);
		log.debug("Removed temporary file");
		return savediov;
	}

	@Transactional
	protected IovDto saveIovAndPayload(IovDto dto, PayloadDto pdto) throws CdbServiceException, IOException {
		String hash = "none";
		try (BufferedInputStream stringInputStream = new BufferedInputStream(
				new ByteArrayInputStream(pdto.getData()))) {
			hash = payloadService.getInputStreamHash(stringInputStream);
		}
		log.debug("Create dto with hash {},  format {}, ...", hash, pdto.getObjectType());
		if (hash.equals("none")) {
			throw new CdbServiceException("Could not compute hash");
		}
		pdto.hash(hash).streamerInfo(pdto.getObjectType().getBytes());
		pdto.size(pdto.getData().length);
		PayloadDto saved = payloadService.insertPayload(pdto);
		dto.payloadHash(hash);
		IovDto savediov = iovService.insertIov(dto);
		log.debug("Create payload {} and iov {} ", saved, savediov);
		return savediov;
	}

	@Override
	public Response getBlob(String hash, SecurityContext securityContext, UriInfo info) throws NotFoundException {
		this.log.info("PayloadRestController processing request to download blob {}", hash);
		StreamingOutput stream = null;
		try {
			PayloadDto pdto = payloadService.getPayloadMetaInfo(hash);
			String ptype = pdto.getObjectType();
			log.debug("Found metadata {}", pdto);
			MediaType media_type = getMediaType(ptype);

			InputStream in = payloadService.getPayloadData(hash);
			stream = new StreamingOutput() {
				@Override
				public void write(OutputStream os) throws IOException, WebApplicationException {
					try {
						int read = 0;
						byte[] bytes = new byte[2048];

						while ((read = in.read(bytes)) != -1) {
							os.write(bytes, 0, read);
							log.trace("Copying {} bytes into the output...", read);
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
			String rettype = media_type.toString();
			String ext = getExtension(ptype);
			return Response.ok(stream) /// MediaType.APPLICATION_JSON_TYPE)
					.header("Content-type", rettype)
					.header("Content-Disposition", "Inline; filename=\"" + hash + ext + "\"")
					// .header("Content-Length", new
					// Long(f.length()).toString())
					.build();

		} catch (CdbServiceException e) {
			String msg = "Error retrieving payload from hash " + hash;
			ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * hep.crest.server.swagger.api.PayloadsApiService#getPayloadMetaInfo(java.lang.
	 * String, javax.ws.rs.core.SecurityContext, javax.ws.rs.core.UriInfo)
	 */
	@Override
	public Response getPayloadMetaInfo(String hash, SecurityContext securityContext, UriInfo info)
			throws NotFoundException {
		this.log.info("PayloadRestController processing request for payload meta information for {}", hash);
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

	protected Map<String, Object> getDocumentStream(IovPayloadDto mddto, List<FormDataBodyPart> bodyParts)
			throws CdbServiceException {
		log.debug("Extracting document BLOB for file {}", mddto.getPayload());
		Map<String, Object> retmap = new HashMap<>();
		String dtofname = mddto.getPayload();
		if (dtofname.startsWith("file://")) {
			dtofname = mddto.getPayload().split("://")[1];
		}
		for (int i = 0; i < bodyParts.size(); i++) {
			BodyPartEntity test = (BodyPartEntity) bodyParts.get(i).getEntity();
			String fileName = bodyParts.get(i).getContentDisposition().getFileName();
			log.debug("Search for file {} in iovset", fileName);
			if (dtofname.contains(fileName)) {
				retmap.put("file", fileName);
				retmap.put("stream", test.getInputStream());
			}
		}
		if (retmap.isEmpty()) {
			throw new CdbServiceException("Cannot find file content in form data. File name = " + mddto.getPayload());
		}
		return retmap;
	}

	protected MediaType getMediaType(String ptype) {
		MediaType media_type = MediaType.APPLICATION_OCTET_STREAM_TYPE;

		if ("PNG".equalsIgnoreCase(ptype)) {
			media_type = new MediaType("image", "png");
		} else if (ptype.toLowerCase().contains("svg")) {
			media_type = MediaType.APPLICATION_SVG_XML_TYPE;
		} else if (ptype.toLowerCase().contains("txt")) {
			media_type = MediaType.TEXT_PLAIN_TYPE;
		} else if (ptype.toLowerCase().contains("csv")) {
			media_type = new MediaType("text", "csv");
		} else if (ptype.toLowerCase().contains("json")) {
			media_type = MediaType.APPLICATION_JSON_TYPE;
		} else if (ptype.toLowerCase().contains("xml")) {
			media_type = MediaType.APPLICATION_XML_TYPE;
		} else if (ptype.toLowerCase().contains("tgz")) {
			media_type = new MediaType("application", "x-gtar-compressed");
		} else if (ptype.toLowerCase().contains("gz")) {
			media_type = new MediaType("application", "gzip");
		} else if (ptype.toLowerCase().contains("pdf")) {
			media_type = new MediaType("application", "pdf");
		}
		return media_type;
	}

	protected String getExtension(String ptype) {
		String extension = ".blob";
		if ("PNG".equalsIgnoreCase(ptype)) {
			extension = "png";
		} else if (ptype.toLowerCase().contains("svg")) {
			extension = "svg";
		} else if (ptype.toLowerCase().contains("txt")) {
			extension = "txt";
		} else if (ptype.toLowerCase().contains("csv")) {
			extension = "csv";
		} else if (ptype.toLowerCase().contains("json")) {
			extension = "json";
		} else if (ptype.toLowerCase().contains("xml")) {
			extension = "xml";
		} else if (ptype.toLowerCase().contains("tgz")) {
			extension = "tgz";
		} else if (ptype.toLowerCase().contains("gz")) {
			extension = "gz";
		} else if (ptype.toLowerCase().contains("pdf")) {
			extension = "pdf";
		}
		return extension;
	}

}
