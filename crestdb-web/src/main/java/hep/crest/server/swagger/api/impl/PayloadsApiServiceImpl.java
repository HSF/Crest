package hep.crest.server.swagger.api.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import hep.crest.data.config.CrestProperties;
import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.exceptions.PayloadEncodingException;
import hep.crest.data.handlers.PayloadHandler;
import hep.crest.server.annotations.CacheControlCdb;
import hep.crest.server.exceptions.NotExistsPojoException;
import hep.crest.server.services.IovService;
import hep.crest.server.services.PayloadService;
import hep.crest.server.swagger.api.ApiResponseMessage;
import hep.crest.server.swagger.api.NotFoundException;
import hep.crest.server.swagger.api.PayloadsApiService;
import hep.crest.swagger.model.GenericMap;
import hep.crest.swagger.model.HTTPResponse;
import hep.crest.swagger.model.IovDto;
import hep.crest.swagger.model.IovSetDto;
import hep.crest.swagger.model.PayloadDto;
import hep.crest.swagger.model.PayloadSetDto;
import ma.glasnost.orika.MapperFacade;
import org.glassfish.jersey.media.multipart.BodyPartEntity;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rest endpoint for payloads.
 *
 * @author formica
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2017-09-05T16:23:23.401+02:00")
@Component
public class PayloadsApiServiceImpl extends PayloadsApiService {

    /**
     * Set separator.
     */
    private static final String SLASH = File.pathSeparator.equals(":") ? "/" : File.pathSeparator;
    /**
     * Maximum number of files.
     */
    private static final int MAX_FILE_UPLOAD = 1000;
    /**
     * The list of payload types for download.
     */
    private static final List<String> payloadlist = Arrays.asList("png", "svg", "json", "xml", "csv", "txt", "tgz",
            "gz", "pdf");
    /**
     * Logger.
     */
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    /**
     * Service.
     */
    @Autowired
    private PayloadService payloadService;
    /**
     * Service.
     */
    @Autowired
    private IovService iovService;
    /**
     * Properties.
     */
    @Autowired
    private CrestProperties cprops;
    /**
     * Mapper.
     */
    @Inject
    private ObjectMapper jacksonMapper;
    /**
     * Mapper.
     */
    @Autowired
    @Qualifier("mapper")
    private MapperFacade mapper;


    /* (non-Javadoc)
     * @see hep.crest.server.swagger.api.PayloadsApiService#createPayload(hep.crest.swagger.model.PayloadDto, javax.ws.rs.core.SecurityContext, javax.ws.rs.core.UriInfo)
     */
    @Override
    public Response createPayload(PayloadDto body, SecurityContext securityContext, UriInfo info)
            throws NotFoundException {
        try {
            // Create a new payload using the body in the request.
            final PayloadDto saved = payloadService.insertPayload(body);
            log.debug("Saved PayloadDto {}", saved);
            return Response.created(info.getRequestUri()).entity(saved).build();
        }
        catch (final CdbServiceException e) {
            // Exception, send a 500.
            log.error("Error saving PayloadDto {}", body);
            final String msg = "Error creating payload resource using " + body.toString();
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * hep.crest.server.swagger.api.PayloadsApiService#createPayloadMultiForm(java.
     * io.InputStream,
     * org.glassfish.jersey.media.multipart.FormDataContentDisposition,
     * org.glassfish.jersey.media.multipart.FormDataBodyPart,
     * javax.ws.rs.core.SecurityContext, javax.ws.rs.core.UriInfo)
     */
    @Override
    public Response createPayloadMultiForm(InputStream fileInputStream,
                                           FormDataContentDisposition fileDetail, FormDataBodyPart payload,
                                           SecurityContext securityContext, UriInfo info) throws NotFoundException {
        this.log.info("PayloadRestController processing request to upload payload from stream");
        // PayloadDto payload = new PayloadDto();
        try {
            // Assume the FormDataBodyPart is a JSON string.
            payload.setMediaType(MediaType.APPLICATION_JSON_TYPE);
            // Get the DTO.
            final PayloadDto payloaddto = payload.getValueAs(PayloadDto.class);
            log.debug("Received body json " + payloaddto);
            // Create the payload taking binary content from the input stream.
            final PayloadDto saved = payloadService.insertPayloadAndInputStream(payloaddto,
                    fileInputStream);
            return Response.created(info.getRequestUri()).entity(saved).build();
        }
        catch (final CdbServiceException | NullPointerException e) {
            // Exception, send 500.
            final String msg = "Error creating payload resource : " + e.getCause();
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * hep.crest.server.swagger.api.PayloadsApiService#getPayload(java.lang.String,
     * java.lang.String, javax.ws.rs.core.SecurityContext, javax.ws.rs.core.UriInfo)
     */
    @Override
    @CacheControlCdb("public, max-age=604800")
    public Response getPayload(String hash, String format, SecurityContext securityContext,
                               UriInfo info) throws NotFoundException {
        this.log.info(
                "PayloadRestController processing request to download payload {} using format {}",
                hash, format);
        try {
            // Get only metadata from the payload.
            final PayloadDto pdto = payloadService.getPayloadMetaInfo(hash);
            final String ptype = pdto.getObjectType();
            log.debug("Found metadata {}", pdto);
            // Get the media type. It utilize the objectType field.
            final MediaType media_type = getMediaType(ptype);

            if (format == null || format.equalsIgnoreCase("BLOB")
                    || format.equalsIgnoreCase("BIN")) {
                // The client requested to get binary data.
                // Get the payload data.
                final InputStream in = payloadService.getPayloadData(hash);
                // Stream data in output.
                final StreamingOutput stream = new StreamingOutput() {
                    @Override
                    public void write(OutputStream os) throws IOException, WebApplicationException {
                        try {
                            int read = 0;
                            final byte[] bytes = new byte[2048];
                            // Read input bytes and write in output stream
                            while ((read = in.read(bytes)) != -1) {
                                os.write(bytes, 0, read);
                                log.trace("Copying {} bytes into the output...", read);
                            }
                            // Flush data
                            os.flush();
                        }
                        catch (final Exception e) {
                            throw new WebApplicationException(e);
                        }
                        finally {
                            // Close all streames to avoid memory leaks.
                            log.debug("closing streams...");
                            if (os != null) {
                                os.close();
                            }
                            if (in != null) {
                                in.close();
                            }
                        }
                    }
                };
                log.debug("Send back the stream....");
                // Get media type
                final String rettype = media_type.toString();
                // Get extension
                final String ext = getExtension(ptype);
                final String fname = hash + "." + ext;
                // Set the content type in the response, and the file name as well.
                return Response.ok(stream) /// MediaType.APPLICATION_JSON_TYPE)
                        .header("Content-type", rettype)
                        .header("Content-Disposition", "Inline; filename=\"" + fname + "\"")
                        // .header("Content-Length", new
                        // Long(f.length()).toString())
                        .build();
            }
            else {
                // The client requested to get a DTO.
                log.debug("Retrieve the full pojo with hash {}", hash);
                final PayloadDto entity = payloadService.getPayload(hash);
                // Build the DTO Set for the response.
                final PayloadSetDto psetdto = buildSet(entity, hash);
                return Response.ok()
                        .header("Content-type", MediaType.APPLICATION_JSON_TYPE.toString())
                        .entity(psetdto).build();
            }
        }
        catch (final NotExistsPojoException e) {
            // The Payload hash was not found.
            return notFoundPojo(hash);
        }
        catch (final CdbServiceException e) {
            // Exception, send a 500.
            final String msg = "Error retrieving payload from hash " + hash;
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
        }
    }


    /*
     * (non-javadoc)
     * storePayloadWithIovMultiForm: @see hep.crest.server.swagger.api.PayloadsApiService#storePayloadWithIovMultiForm(
     * java.io.InputStream, org.glassfish.jersey.media.multipart.FormDataContentDisposition,
     * java.lang.String, java.math.BigDecimal, java.lang.String, java.lang.String,
     * java.lang.String, java.math.BigDecimal, javax.ws.rs.core.SecurityContext, javax.ws.rs.core.UriInfo)
     */
    @Override
    public Response storePayloadWithIovMultiForm(InputStream fileInputStream,
                                                 FormDataContentDisposition fileDetail, String tag, BigDecimal since, String format,
                                                 String objectType, String version, BigDecimal endtime,
                                                 SecurityContext securityContext, UriInfo info)
            throws NotFoundException {
        this.log.info(
                "PayloadRestController processing request to store payload with iov in tag {} and at since {} using format {}",
                tag, since, format);
        try {
            // Store payload with multi form
            String fdetailsname = fileDetail.getFileName();
            if (fdetailsname == null || fdetailsname.isEmpty()) {
                // Generate a fake filename.
                fdetailsname = ".blob";
            }
            else {
                // Get the filename from the input request.
                final Path p = Paths.get(fdetailsname);
                fdetailsname = "_" + p.getFileName().toString();
            }
            // Create a temporary file name from tag name and time of validity.
            final String filename = cprops.getDumpdir() + SLASH + tag + "_" + since
                    + fdetailsname;
            if (format == null) {
                format = "JSON";
            }
            // Add object type
            if (objectType == null) {
                objectType = format;
            }
            // Add version
            if (version == null) {
                version = "default";
            }
            // Create the streamer info object as a map with metadata like format and filename for the moment. In
            // future this could have further informations on payload metadata (author, checksum, etc).
            final Map<String, String> sinfomap = new HashMap<>();
            // Add the filename, depending on the input information
            sinfomap.put("filename", (fileDetail.getFileName() != null && !fileDetail.getFileName().isEmpty()) ?
                    fileDetail.getFileName() : filename);
            sinfomap.put("format", format);
            // Create the DTO, the version here is ignored. It could be added from the Form data.
            final PayloadDto pdto = new PayloadDto().objectType(objectType)
                    .streamerInfo(jacksonMapper.writeValueAsBytes(sinfomap)).version(version);
            final String hash = getHash(fileInputStream, filename);
            pdto.hash(hash);
            final IovDto iovDto = new IovDto().payloadHash(hash).since(since).tagName(tag);
            // Save iov and payload and get the response object in return.
            final HTTPResponse resp = payloadService.saveIovAndPayload(iovDto, pdto, filename);
            resp.action("storePayloadWithIovMultiForm");
            return Response.created(info.getRequestUri()).entity(resp).build();
        }
        catch (final RuntimeException | IOException e) {
            final String msg = "Internal exception creating payload resource using storePayloadWithIovMultiForm for " +
                    "tag " + tag.toString() + " : " + e.getMessage();
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
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
                                                       String xCrestPayloadFormat, String objectType, String version,
                                                       BigDecimal endtime, SecurityContext securityContext,
                                                       UriInfo info) throws NotFoundException {
        this.log.info(
                "PayloadRestController processing request to upload payload batch in tag {} with multi-iov {} and body {}",
                tag, filesbodyparts.size(), iovsetupload.getValue());
        iovsetupload.setMediaType(MediaType.APPLICATION_JSON_TYPE);
        try {
            // Read input FormData as an IovSet object.
            final IovSetDto dto = iovsetupload.getValueAs(IovSetDto.class);
            log.info("Batch insertion of {} iovs using file formatted in {}", dto.getSize(),
                    dto.getFormat());
            // Add object type.
            if (objectType == null) {
                objectType = dto.getFormat();
            }
            // Add version.
            if (version == null) {
                version = "default";
            }
            // Set default for payload format.
            if (xCrestPayloadFormat == null) {
                xCrestPayloadFormat = "FILE";
            }
            // Check that number of files is not too much.
            if (filesbodyparts.size() > MAX_FILE_UPLOAD) {
                final String msg = "Too many files attached to the request...> MAX_FILE_UPLOAD = "
                        + MAX_FILE_UPLOAD;
                final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,
                        msg);
                return Response.status(Response.Status.BAD_REQUEST).entity(resp).build();
            }
            // Only the payload format FILE is allowed here.
            // This was created to eventually merge with other methods later on.
            if (xCrestPayloadFormat.equals("FILE")) {
                final IovSetDto outdto = storeIovs(dto, tag, objectType, version, filesbodyparts);
                return Response.created(info.getRequestUri()).entity(outdto).build();
            }
            else {
                throw new CdbServiceException("Wrong header parameter " + xCrestPayloadFormat);
            }
        }
        catch (final RuntimeException | IOException e) {
            final String msg =
                    "Internal exception creating payload resource using uploadPayloadBatchWithIovMultiForm for " +
                    "tag " + tag.toString() + " : " + e.getMessage();
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
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
                                                      String xCrestPayloadFormat, String objectType, String version,
                                                      BigDecimal endtime, SecurityContext securityContext,
                                                      UriInfo info) throws NotFoundException {
        this.log.info(
                "PayloadRestController processing request to store payload batch in tag {} with multi-iov",
                tag);
        iovsetupload.setMediaType(MediaType.APPLICATION_JSON_TYPE);
        try {
            // Read the FormData as a IovSet object.
            final IovSetDto dto = iovsetupload.getValueAs(IovSetDto.class);
            log.info("Batch insertion of {} iovs using file formatted in {}", dto.getSize(),
                    dto.getFormat());
            // Add object type.
            if (objectType == null) {
                objectType = dto.getFormat();
            }
            // Add version.
            if (version == null) {
                version = "default";
            }
            // Set default for payload format.
            if (xCrestPayloadFormat == null) {
                xCrestPayloadFormat = "JSON";
            }
            // This method only accept JSON in header format.
            // It can probably be merged with the previous method.
            if (xCrestPayloadFormat.equalsIgnoreCase("JSON")) {
                final IovSetDto outdto = storeIovs(dto, tag, objectType, version,null);
                return Response.created(info.getRequestUri()).entity(outdto).build();
            }
            else {
                throw new CdbServiceException("Wrong header parameter " + xCrestPayloadFormat);
            }
        }
        catch (final RuntimeException | IOException e) {
            final String msg =
                    "Internal exception creating payload resource using storePayloadBatchWithIovMultiForm for " +
                            "tag " + tag.toString() + " : " + e.getMessage();
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
        }
    }

    /**
     * Store iovs and payload files.
     *
     * @param dto            the IovSetDto
     * @param tag            the String
     * @param filesbodyparts the List<FormDataBodyPart>
     * @return IovSetDto
     * @throws PayloadEncodingException If an Exception occurred
     * @throws IOException         If an Exception occurred
     */
    protected IovSetDto storeIovs(IovSetDto dto, String tag, String objectType, String version, List<FormDataBodyPart> filesbodyparts)
            throws PayloadEncodingException, IOException {
        final List<IovDto> iovlist = dto.getResources();
        final List<IovDto> savediovlist = new ArrayList<>();
        // Loop over iovs found in the Set.
        for (final IovDto piovDto : iovlist) {
            String filename = null;
            final Map<String, String> sinfomap = new HashMap<>();
            sinfomap.put("format", dto.getFormat());
            // Here we generate objectType and version. We should probably allow for input arguments.
            final PayloadDto pdto = new PayloadDto().objectType(objectType).hash("none")
                    .version(version);
            if (filesbodyparts == null) {
                // If there are no attached files, then the payloadHash contains the payload itself.
                pdto.data(piovDto.getPayloadHash().getBytes());
                final String hash = getHash(new ByteArrayInputStream(pdto.getData()), "none");
                pdto.hash(hash);
                sinfomap.put("filename", hash);
            }
            else {
                // If there are attached files, then the payload will be loaded from filename.
                final Map<String, Object> retmap = getDocumentStream(piovDto, filesbodyparts);
                filename = (String) retmap.get("file");
                final String hash = getHash((InputStream) retmap.get("stream"), filename);
                sinfomap.put("filename", filename);
                pdto.hash(hash);
            }
            pdto.streamerInfo(jacksonMapper.writeValueAsBytes(sinfomap));
            final IovDto iovDto = new IovDto().payloadHash(pdto.getHash()).since(piovDto.getSince())
                    .tagName(tag);
            try {
                payloadService.saveIovAndPayload(iovDto, pdto, filename);
                savediovlist.add(iovDto);
            }
            catch (final CdbServiceException e1) {
                log.error("Cannot insert iov {}", piovDto);
            }
        }
        dto.size((long) savediovlist.size());
        dto.resources(savediovlist);
        return dto;
    }

    /**
     * @param fileInputStream the InputStream
     * @param filename        the String
     * @return String. The computed hash from the byte stream.
     * @throws PayloadEncodingException If an Exception occurred
     * @throws IOException         If an Exception occurred
     */
    protected String getHash(InputStream fileInputStream, String filename)
            throws PayloadEncodingException, IOException {
        try (BufferedInputStream bis = new BufferedInputStream(fileInputStream)) {
            if (filename.equals("none")) {
                return PayloadHandler.getHashFromStream(bis);
            }
            return PayloadHandler.saveToFileGetHash(bis, filename);
        }
        catch (final PayloadEncodingException e) {
            log.error("Cannot get hash from {}: payload encoding exception {}", filename, e.getMessage());
            throw e;
        }
    }

//    /**
//     * @param dto      the IovDto
//     * @param pdto     the PayloadDto
//     * @param filename the String
//     * @return HTTPResponse
//     * @throws CdbServiceException         If an Exception occurred
//     */
//    @Transactional
//    protected HTTPResponse saveIovAndPayload(IovDto dto, PayloadDto pdto, String filename)
//            throws CdbServiceException {
//        log.debug("Create dto with hash {},  format {}, ...", dto.getPayloadHash(),
//                pdto.getObjectType());
//        if (filename == null) {
//            try {
//                // pdto.hash(hash).streamerInfo(pdto.getObjectType().getBytes());
//                pdto.size(pdto.getData().length);
//                final PayloadDto saved = payloadService.insertPayload(pdto);
//                String tagname = dto.getTagName();
//                Iov entity = mapper.map(dto, Iov.class);
//                entity.setTag(new Tag(tagname));
//                final Iov savediov = iovService.insertIov(entity);
//                IovDto saveddto = mapper.map(savediov, IovDto.class);
//                dto.tagName(tagname);
//
//                log.debug("Created payload {} and iov {} ", saved, savediov);
//                return new HTTPResponse().code(Response.Status.CREATED.getStatusCode())
//                        .id(savediov.getPayloadHash()).message("Iov created in tag "
//                                + dto.getTagName() + " with time " + saveddto.getSince());
//            }
//            catch (final NotExistsPojoException e) {
//                return new HTTPResponse().code(Response.Status.NOT_FOUND.getStatusCode())
//                        .id(dto.getPayloadHash()).message("Tag not found "
//                                + dto.getTagName());
//            }
//            catch (RuntimeException e) {
//                final String msg = "Api method saveIovAndPayload got exception " + e.getMessage();
//                new HTTPResponse().code(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
//                        .id(dto.getPayloadHash()).message("Runtime error for "
//                        + dto.getTagName());
//            }
//        }
//        final Path temppath = Paths.get(filename);
//        try (InputStream is = new FileInputStream(filename);) {
//            // pdto.streamerInfo(pdto.getObjectType().getBytes());
//            final FileChannel tempchan = FileChannel.open(temppath);
//            pdto.size((int) tempchan.size());
//            tempchan.close();
//            final PayloadDto saved = payloadService.insertPayloadAndInputStream(pdto, is);
//            String tagname = dto.getTagName();
//            Iov entity = mapper.map(dto, Iov.class);
//            entity.setTag(new Tag(tagname));
//            final Iov savediov = iovService.insertIov(entity);
//            IovDto saveddto = mapper.map(savediov, IovDto.class);
//            dto.tagName(tagname);
//            log.debug("Create payload {} and iov {} ", saved, saveddto);
//            return new HTTPResponse().code(Response.Status.CREATED.getStatusCode())
//                    .id(savediov.getPayloadHash()).message("Iov created in tag " + dto.getTagName()
//                            + " with time " + saveddto.getSince());
//        }
//        catch (final NotExistsPojoException e) {
//            return new HTTPResponse().code(Response.Status.NOT_FOUND.getStatusCode())
//                    .id(dto.getPayloadHash()).message("Tag not found "
//                            + dto.getTagName());
//        }
//        catch (RuntimeException e) {
//            final String msg = "Api method saveIovAndPayload got exception " + e.getMessage();
//            new HTTPResponse().code(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
//                    .id(dto.getPayloadHash()).message("Runtime error for "
//                    + dto.getTagName());
//        }
//        catch (IOException e) {
//            final String msg = "Api method saveIovAndPayload got exception " + e.getMessage();
//            new HTTPResponse().code(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
//                    .id(dto.getPayloadHash()).message("IO error for "
//                    + dto.getTagName());
//        }
//        finally {
//            try {
//                Files.deleteIfExists(temppath);
//            }
//            catch (IOException e) {
//                log.error("Cannot delete temporary file");
//            }
//            log.debug("Removed temporary file");
//        }
//        return new HTTPResponse().code(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
//                .id(dto.getPayloadHash()).message("Internal error for "
//                        + dto.getTagName());
//    }

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
        this.log.info(
                "PayloadRestController processing request for payload meta information for {}",
                hash);
        try {
            final PayloadDto entity = payloadService.getPayloadMetaInfo(hash);
            final PayloadSetDto psetdto = buildSet(entity, hash);
            return Response.ok()
                    .header("Content-type", MediaType.APPLICATION_JSON_TYPE.toString())
                    .entity(psetdto).build();

        }
        catch (final NotExistsPojoException e) {
            return notFoundPojo(hash);
        }
        catch (final CdbServiceException e) {
            final String msg = "Error retrieving payload from hash " + hash;
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
        }
    }

    /**
     * @param mddto     the IovDto
     * @param bodyParts the List<FormDataBodyPart>
     * @return Map<String, Object>
     * @throws PayloadEncodingException If an Exception occurred
     */
    protected Map<String, Object> getDocumentStream(IovDto mddto, List<FormDataBodyPart> bodyParts)
            throws PayloadEncodingException {
        log.debug("Extracting document BLOB for file {}", mddto.getPayloadHash());
        final Map<String, Object> retmap = new HashMap<>();
        String dtofname = mddto.getPayloadHash();
        if (dtofname.startsWith("file://")) {
            dtofname = mddto.getPayloadHash().split("://")[1];
        }
        for (int i = 0; i < bodyParts.size(); i++) {
            final BodyPartEntity test = (BodyPartEntity) bodyParts.get(i).getEntity();
            final String fileName = bodyParts.get(i).getContentDisposition().getFileName();
            log.debug("Search for file {} in iovset", fileName);
            if (dtofname.contains(fileName)) {
                retmap.put("file", fileName);
                retmap.put("stream", test.getInputStream());
            }
        }
        if (retmap.isEmpty()) {
            throw new PayloadEncodingException(
                    "Cannot find file content in form data. File name = " + mddto.getPayloadHash());
        }
        return retmap;
    }

    /**
     * Utility class to better download payload data using the type.
     *
     * @param ptype the String
     * @return MediaType
     */
    protected MediaType getMediaType(String ptype) {
        MediaType media_type = MediaType.APPLICATION_OCTET_STREAM_TYPE;
        final String comp = ptype.toLowerCase();
        switch (comp) {
            case "png":
                media_type = new MediaType("image", "png");
                break;
            case "svg":
                media_type = MediaType.APPLICATION_SVG_XML_TYPE;
                break;
            case "json":
                media_type = MediaType.APPLICATION_JSON_TYPE;
                break;
            case "xml":
                media_type = MediaType.APPLICATION_XML_TYPE;
                break;
            case "csv":
                media_type = new MediaType("text", "csv");
                break;
            case "txt":
                media_type = MediaType.TEXT_PLAIN_TYPE;
                break;
            case "tgz":
                media_type = new MediaType("application", "x-gtar-compressed");
                break;
            case "gz":
                media_type = new MediaType("application", "gzip");
                break;
            case "pdf":
                media_type = new MediaType("application", "pdf");
                break;
            default:
                break;
        }
        return media_type;
    }

    /**
     * Set file extension in dowload.
     *
     * @param ptype the String
     * @return String
     */
    protected String getExtension(String ptype) {
        String extension = "blob";
        final String comp = ptype.toLowerCase();
        final boolean match = payloadlist.stream().anyMatch(comp::contains);
        if (match) {
            extension = comp;
        }
        return extension;
    }

    /**
     * @param entity the PayloadDto
     * @param hash   the String
     * @return PayloadSetDto
     */
    protected PayloadSetDto buildSet(PayloadDto entity, String hash) {
        final GenericMap map = new GenericMap();
        map.put("hash", hash);
        final PayloadSetDto psetdto = new PayloadSetDto().addResourcesItem(entity);
        psetdto.datatype(entity.getObjectType()).filter(map).size(1L);
        return psetdto;
    }

    protected Response notFoundPojo(String hash) {
        final String msg = "Cannot find payload corresponding to hash " + hash;
        final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
        return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
    }

}
