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
import hep.crest.data.exceptions.PayloadEncodingException;
import hep.crest.data.handlers.PayloadHandler;
import hep.crest.server.annotations.CacheControlCdb;
import hep.crest.server.exceptions.AlreadyExistsPojoException;
import hep.crest.server.services.IovService;
import hep.crest.server.services.PayloadService;
import hep.crest.server.swagger.api.ApiResponseMessage;
import hep.crest.server.swagger.api.NotFoundException;
import hep.crest.server.swagger.api.PayloadsApiService;
import hep.crest.swagger.model.HTTPResponse;
import hep.crest.swagger.model.IovDto;
import hep.crest.swagger.model.IovSetDto;
import hep.crest.swagger.model.PayloadDto;

/**
 * @author formica
 *
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2017-09-05T16:23:23.401+02:00")
@Component
public class PayloadsApiServiceImpl extends PayloadsApiService {

    /**
     * Logger.
     */
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Set separator.
     */
    private static final String SLASH = File.pathSeparator.equals(":") ? "/" : File.pathSeparator;

    /**
     * Maximum number of files.
     */
    private static final int MAX_FILE_UPLOAD = 1000;

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
     * Handler.
     */
    @Autowired
    private PayloadHandler payloadHandler;

    /**
     * Properties.
     */
    @Autowired
    private CrestProperties cprops;

    @Override
    public Response createPayload(PayloadDto body, SecurityContext securityContext, UriInfo info)
            throws NotFoundException {
        try {
            final PayloadDto saved = payloadService.insertPayload(body);
            log.debug("Saved PayloadDto {}", saved);
            return Response.created(info.getRequestUri()).entity(saved).build();
        }
        catch (final CdbServiceException e) {
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
        this.log.info("PayloadRestController processing request to upload payload ");
        // PayloadDto payload = new PayloadDto();
        try {
            payload.setMediaType(MediaType.APPLICATION_JSON_TYPE);
            final PayloadDto payloaddto = payload.getValueAs(PayloadDto.class);
            log.debug("Received body json " + payloaddto);
            final PayloadDto saved = payloadService.insertPayloadAndInputStream(payloaddto,
                    fileInputStream);
            return Response.created(info.getRequestUri()).entity(saved).build();

        }
        catch (final CdbServiceException e) {
            final String msg = "Error creating payload resource using " + payload.toString();
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
            final PayloadDto pdto = payloadService.getPayloadMetaInfo(hash);
            final String ptype = pdto.getObjectType();
            log.debug("Found metadata {}", pdto);

            final MediaType media_type = getMediaType(ptype);

            if (format == null || format.equalsIgnoreCase("BLOB")
                    || format.equalsIgnoreCase("BIN")) {
                final InputStream in = payloadService.getPayloadData(hash);
                final StreamingOutput stream = new StreamingOutput() {
                    @Override
                    public void write(OutputStream os) throws IOException, WebApplicationException {
                        try {
                            int read = 0;
                            final byte[] bytes = new byte[2048];

                            while ((read = in.read(bytes)) != -1) {
                                os.write(bytes, 0, read);
                                log.trace("Copying {} bytes into the output...", read);
                            }
                            os.flush();
                        }
                        catch (final Exception e) {
                            throw new WebApplicationException(e);
                        }
                        finally {
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
                final String rettype = media_type.toString();
                final String ext = getExtension(ptype);
                return Response.ok(stream) /// MediaType.APPLICATION_JSON_TYPE)
                        .header("Content-type", rettype)
                        .header("Content-Disposition", "Inline; filename=\"" + hash + ext + "\"")
                        // .header("Content-Length", new
                        // Long(f.length()).toString())
                        .build();
            }
            else {
                log.debug("Retrieve the full pojo with hash {}", hash);
                final PayloadDto entity = payloadService.getPayload(hash);
                if (entity == null) {
                    final String msg = "Cannot find payload corresponding to hash " + hash;
                    final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,
                            msg);
                    return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
                }
                return Response.ok(entity, MediaType.APPLICATION_JSON_TYPE).build();
            }
        }
        catch (final CdbServiceException e) {
            final String msg = "Error retrieving payload from hash " + hash;
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * hep.crest.server.swagger.api.PayloadsApiService#storePayloadWithIovMultiForm(
     * java.io.InputStream,
     * org.glassfish.jersey.media.multipart.FormDataContentDisposition,
     * java.lang.String, java.math.BigDecimal, java.lang.String,
     * java.math.BigDecimal, javax.ws.rs.core.SecurityContext,
     * javax.ws.rs.core.UriInfo)
     */
    @Override
    public Response storePayloadWithIovMultiForm(InputStream fileInputStream,
            FormDataContentDisposition fileDetail, String tag, BigDecimal since, String format,
            BigDecimal endtime, SecurityContext securityContext, UriInfo info)
            throws NotFoundException {
        this.log.info(
                "PayloadRestController processing request to store payload with iov in tag {} and at since {} using format {}",
                tag, since, format);
        try {
            String fdetailsname = fileDetail.getFileName();
            if (fdetailsname == null || fdetailsname.isEmpty()) {
                fdetailsname = ".blob";
            }
            final String filename = cprops.getDumpdir() + SLASH + tag + "_" + since + "_"
                    + fdetailsname;
            if (format == null) {
                format = "JSON";
            }

            final PayloadDto pdto = new PayloadDto().objectType(format)
                    .streamerInfo(format.getBytes()).version("none");
            final String hash = getHash(fileInputStream, filename);
            pdto.hash(hash);
            final IovDto iovDto = new IovDto().payloadHash(hash).since(since).tagName(tag);
            final HTTPResponse resp = saveIovAndPayload(iovDto, pdto, filename);
            resp.action("storePayloadWithIovMultiForm");
            return Response.created(info.getRequestUri()).entity(resp).build();

        }
        catch (final Exception e) {
            final String msg = "Internal exception creating payload resource using storeWithIov "
                    + tag.toString() + " : " + e.getMessage();
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
            String xCrestPayloadFormat, BigDecimal endtime, SecurityContext securityContext,
            UriInfo info) throws NotFoundException {
        this.log.info(
                "PayloadRestController processing request to upload payload batch in tag {} with multi-iov {} and body {}",
                tag, filesbodyparts.size(), iovsetupload.getValue());
        iovsetupload.setMediaType(MediaType.APPLICATION_JSON_TYPE);
        try {
            final IovSetDto dto = iovsetupload.getValueAs(IovSetDto.class);
            log.info("Batch insertion of {} iovs using file formatted in {}", dto.getSize(),
                    dto.getFormat());
            final List<IovDto> iovlist = dto.getResources();
            final List<IovDto> savediovlist = new ArrayList<>();
            if (xCrestPayloadFormat == null) {
                xCrestPayloadFormat = "FILE";
            }
            if (filesbodyparts.size() > MAX_FILE_UPLOAD) {
                final String msg = "Too many files attached to the request...> MAX_FILE_UPLOAD = "
                        + MAX_FILE_UPLOAD;
                final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,
                        msg);
                return Response.status(Response.Status.BAD_REQUEST).entity(resp).build();
            }
            if (xCrestPayloadFormat.equals("FILE")) {
                for (final IovDto piovDto : iovlist) {
                    final Map<String, Object> retmap = getDocumentStream(piovDto, filesbodyparts);
                    final PayloadDto pdto = new PayloadDto().objectType(dto.getFormat())
                            .streamerInfo(dto.getFormat().getBytes()).version("none");
                    final String filename = (String) retmap.get("file");
                    log.debug("Use input filename for hash generation and later storage...{}",
                            filename);
                    final String hash = getHash((InputStream) retmap.get("stream"), filename);
                    pdto.hash(hash);
                    final IovDto iovDto = new IovDto().payloadHash(hash).since(piovDto.getSince())
                            .tagName(tag);
                    try {
                        saveIovAndPayload(iovDto, pdto, filename);
                        savediovlist.add(iovDto);
                    }
                    catch (final CdbServiceException e1) {
                        log.error("Cannot insert iov {}", piovDto);
                    }
                }
                dto.size((long) savediovlist.size());
                dto.resources(savediovlist);
                return Response.created(info.getRequestUri()).entity(dto).build();
            }
            else {
                throw new CdbServiceException("Wrong header parameter " + xCrestPayloadFormat);
            }

        }
        catch (final Exception e) {
            final String msg = "Internal exception creating payload resource using uploadBatch "
                    + tag.toString() + " : " + e.getMessage();
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
            String xCrestPayloadFormat, BigDecimal endtime, SecurityContext securityContext,
            UriInfo info) throws NotFoundException {
        this.log.info(
                "PayloadRestController processing request to store payload batch in tag {} with multi-iov",
                tag);
        iovsetupload.setMediaType(MediaType.APPLICATION_JSON_TYPE);
        try {
            final IovSetDto dto = iovsetupload.getValueAs(IovSetDto.class);
            log.info("Batch insertion of {} iovs using file formatted in {}", dto.getSize(),
                    dto.getFormat());
            final List<IovDto> iovlist = dto.getResources();
            final List<IovDto> savediovlist = new ArrayList<>();
            if (xCrestPayloadFormat == null) {
                xCrestPayloadFormat = "JSON";
            }
            if (xCrestPayloadFormat.equalsIgnoreCase("JSON")) {
                for (final IovDto piovDto : iovlist) {
                    final PayloadDto pdto = new PayloadDto().objectType(dto.getFormat())
                            .hash("none").streamerInfo(dto.getFormat().getBytes()).version("none")
                            .data(piovDto.getPayloadHash().getBytes());
                    final String hash = getHash(new ByteArrayInputStream(pdto.getData()), "none");
                    pdto.hash(hash);
                    final IovDto iovDto = new IovDto().payloadHash(hash).since(piovDto.getSince())
                            .tagName(tag);
                    try {
                        saveIovAndPayload(iovDto, pdto, null);
                        savediovlist.add(iovDto);
                    }
                    catch (final CdbServiceException e1) {
                        log.error("Cannot insert iov {}", piovDto);
                    }
                }
                dto.size((long) savediovlist.size());
                dto.resources(savediovlist);
                return Response.created(info.getRequestUri()).entity(dto).build();

            }
            else {
                throw new CdbServiceException("Wrong header parameter " + xCrestPayloadFormat);
            }

        }
        catch (final Exception e) {
            final String msg = "Internal exception creating payload resource using storeBatch "
                    + tag.toString() + " : " + e.getMessage();
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
        }
    }

    /**
     * @param fileInputStream
     *            the InputStream
     * @param filename
     *            the String
     * @return String. The computed hash from the byte stream.
     * @throws CdbServiceException
     *             If an Exception occurred
     * @throws IOException
     *             If an Exception occurred
     */
    protected String getHash(InputStream fileInputStream, String filename)
            throws CdbServiceException, IOException {
        try (BufferedInputStream bis = new BufferedInputStream(fileInputStream)) {
            if (filename.equals("none")) {
                return payloadHandler.getHashFromStream(bis);
            }
            return payloadHandler.saveToFileGetHash(bis, filename);
        }
        catch (final PayloadEncodingException e) {
            throw new CdbServiceException("Cannot compute the hash : " + e.getMessage());
        }
    }

    /**
     * @param dto
     *            the IovDto
     * @param pdto
     *            the PayloadDto
     * @param filename
     *            the String
     * @return HTTPResponse
     * @throws CdbServiceException
     *             If an Exception occurred
     * @throws IOException
     *             If an Exception occurred
     */
    @Transactional
    protected HTTPResponse saveIovAndPayload(IovDto dto, PayloadDto pdto, String filename)
            throws CdbServiceException, IOException {
        log.debug("Create dto with hash {},  format {}, ...", dto.getPayloadHash(),
                pdto.getObjectType());
        if (filename == null) {
            try {
                // pdto.hash(hash).streamerInfo(pdto.getObjectType().getBytes());
                pdto.size(pdto.getData().length);
                final PayloadDto saved = payloadService.insertPayload(pdto);
                final IovDto savediov = iovService.insertIov(dto);
                log.debug("Created payload {} and iov {} ", saved, savediov);
                return new HTTPResponse().code(Response.Status.CREATED.getStatusCode())
                        .id(savediov.getPayloadHash()).message("Iov created in tag "
                                + dto.getTagName() + " with time " + savediov.getSince());
            }
            catch (final AlreadyExistsPojoException e) {
                return new HTTPResponse().code(Response.Status.NOT_MODIFIED.getStatusCode())
                        .id(dto.getPayloadHash()).message("Iov already exists in tag "
                                + dto.getTagName() + " with time " + dto.getSince());
            }
        }
        final Path temppath = Paths.get(filename);
        try (InputStream is = new FileInputStream(filename);) {
            // pdto.streamerInfo(pdto.getObjectType().getBytes());
            final FileChannel tempchan = FileChannel.open(temppath);
            pdto.size((int) tempchan.size());
            tempchan.close();
            final PayloadDto saved = payloadService.insertPayloadAndInputStream(pdto, is);
            final IovDto savediov = iovService.insertIov(dto);
            log.debug("Create payload {} and iov {} ", saved, savediov);
            return new HTTPResponse().code(Response.Status.CREATED.getStatusCode())
                    .id(savediov.getPayloadHash()).message("Iov created in tag " + dto.getTagName()
                            + " with time " + savediov.getSince());
        }
        catch (final AlreadyExistsPojoException e) {
            return new HTTPResponse().code(Response.Status.NOT_MODIFIED.getStatusCode())
                    .id(dto.getPayloadHash()).message("Iov already exists in tag "
                            + dto.getTagName() + " with time " + dto.getSince());
        }
        finally {
            Files.deleteIfExists(temppath);
            log.debug("Removed temporary file");
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
        this.log.info(
                "PayloadRestController processing request for payload meta information for {}",
                hash);
        try {

            final PayloadDto entity = payloadService.getPayloadMetaInfo(hash);
            if (entity == null) {
                final String msg = "Cannot find payload corresponding to hash " + hash;
                final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,
                        msg);
                return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
            }
            return Response.ok().entity(entity).build();
        }
        catch (final CdbServiceException e) {
            final String msg = "Error retrieving payload from hash " + hash;
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
        }
    }

    /**
     * @param mddto
     *            the IovDto
     * @param bodyParts
     *            the List<FormDataBodyPart>
     * @return Map<String, Object>
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    protected Map<String, Object> getDocumentStream(IovDto mddto, List<FormDataBodyPart> bodyParts)
            throws CdbServiceException {
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
            throw new CdbServiceException(
                    "Cannot find file content in form data. File name = " + mddto.getPayloadHash());
        }
        return retmap;
    }

    /**
     * Utility class to better download payload data using the type.
     *
     * @param ptype
     *            the String
     * @return MediaType
     */
    protected MediaType getMediaType(String ptype) {
        MediaType media_type = MediaType.APPLICATION_OCTET_STREAM_TYPE;

        if ("PNG".equalsIgnoreCase(ptype)) {
            media_type = new MediaType("image", "png");
        }
        else if (ptype.toLowerCase().contains("svg")) {
            media_type = MediaType.APPLICATION_SVG_XML_TYPE;
        }
        else if (ptype.toLowerCase().contains("txt")) {
            media_type = MediaType.TEXT_PLAIN_TYPE;
        }
        else if (ptype.toLowerCase().contains("csv")) {
            media_type = new MediaType("text", "csv");
        }
        else if (ptype.toLowerCase().contains("json")) {
            media_type = MediaType.APPLICATION_JSON_TYPE;
        }
        else if (ptype.toLowerCase().contains("xml")) {
            media_type = MediaType.APPLICATION_XML_TYPE;
        }
        else if (ptype.toLowerCase().contains("tgz")) {
            media_type = new MediaType("application", "x-gtar-compressed");
        }
        else if (ptype.toLowerCase().contains("gz")) {
            media_type = new MediaType("application", "gzip");
        }
        else if (ptype.toLowerCase().contains("pdf")) {
            media_type = new MediaType("application", "pdf");
        }
        return media_type;
    }

    /**
     * Set file extension in dowload.
     *
     * @param ptype
     *            the String
     * @return String
     */
    protected String getExtension(String ptype) {
        String extension = ".blob";
        if ("PNG".equalsIgnoreCase(ptype)) {
            extension = "png";
        }
        else if (ptype.toLowerCase().contains("svg")) {
            extension = "svg";
        }
        else if (ptype.toLowerCase().contains("txt")) {
            extension = "txt";
        }
        else if (ptype.toLowerCase().contains("csv")) {
            extension = "csv";
        }
        else if (ptype.toLowerCase().contains("json")) {
            extension = "json";
        }
        else if (ptype.toLowerCase().contains("xml")) {
            extension = "xml";
        }
        else if (ptype.toLowerCase().contains("tgz")) {
            extension = "tgz";
        }
        else if (ptype.toLowerCase().contains("gz")) {
            extension = "gz";
        }
        else if (ptype.toLowerCase().contains("pdf")) {
            extension = "pdf";
        }
        return extension;
    }

}
