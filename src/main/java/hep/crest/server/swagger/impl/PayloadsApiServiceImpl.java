package hep.crest.server.swagger.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hep.crest.server.annotations.ProfileAndLog;
import hep.crest.server.caching.CachingPolicyService;
import hep.crest.server.config.CrestProperties;
import hep.crest.server.controllers.EntityDtoHelper;
import hep.crest.server.controllers.JsonStreamProcessor;
import hep.crest.server.controllers.PageRequestHelper;
import hep.crest.server.controllers.SimpleLobStreamerProvider;
import hep.crest.server.converters.PayloadHandler;
import hep.crest.server.converters.PayloadMapper;
import hep.crest.server.data.pojo.Iov;
import hep.crest.server.data.pojo.IovId;
import hep.crest.server.data.pojo.Payload;
import hep.crest.server.data.pojo.PayloadData;
import hep.crest.server.data.pojo.PayloadInfoData;
import hep.crest.server.data.pojo.Tag;
import hep.crest.server.data.repositories.args.PayloadQueryArgs;
import hep.crest.server.exceptions.AbstractCdbServiceException;
import hep.crest.server.exceptions.CdbBadRequestException;
import hep.crest.server.exceptions.CdbInternalException;
import hep.crest.server.exceptions.PayloadEncodingException;
import hep.crest.server.services.IovService;
import hep.crest.server.services.PayloadService;
import hep.crest.server.services.StorableData;
import hep.crest.server.services.TagService;
import hep.crest.server.swagger.api.NotFoundException;
import hep.crest.server.swagger.api.PayloadsApiService;
import hep.crest.server.swagger.model.CrestBaseResponse;
import hep.crest.server.swagger.model.GenericMap;
import hep.crest.server.swagger.model.PayloadDto;
import hep.crest.server.swagger.model.PayloadSetDto;
import hep.crest.server.swagger.model.RespPage;
import hep.crest.server.swagger.model.StoreDto;
import hep.crest.server.swagger.model.StoreSetDto;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.media.multipart.BodyPartEntity;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.StreamingOutput;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rest endpoint for payloads.
 *
 * @author formica
 */
@Component
@Slf4j
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
    private static final List<String> payloadlist = Arrays.asList("png", "svg", "json", "xml",
            "csv", "txt", "tgz",
            "gz", "pdf");

    /**
     * The media type for several types.
     */
    private static final String APPLICATION_MEDIA = "application";
    /**
     * Service.
     */
    private PayloadService payloadService;
    /**
     * Service.
     */
    private IovService iovService;
    /**
     * Service.
     */
    private TagService tagService;
    /**
     * Service.
     */
    private JsonStreamProcessor jsonStreamProcessor;
    /**
     * Service.
     */
    private CachingPolicyService cachesvc;
    /**
     * Properties.
     */
    private CrestProperties cprops;
    /**
     * Mapper.
     */
    private ObjectMapper jacksonMapper;

    /**
     * Helper.
     */
    private EntityDtoHelper edh;
    /**
     * Helper.
     */
    private PageRequestHelper prh;

    /**
     * Mapper.
     */
    private PayloadMapper mapper;

    /**
     * Ctor with injected services.
     * @param payloadService the payload service
     * @param tagService the tag service
     * @param cachingPolicyService caching service
     * @param jsonStreamProcessor the json stream processor
     * @param crestProperties the crest properties
     * @param entityDtoHelper the entity dto helper
     */
    public PayloadsApiServiceImpl(PayloadService payloadService,
                                  TagService tagService,
                                  CachingPolicyService cachingPolicyService,
                                  JsonStreamProcessor jsonStreamProcessor,
                                  CrestProperties crestProperties,
                                  EntityDtoHelper entityDtoHelper) {
        this.payloadService = payloadService;
        this.iovService = payloadService.getIovService();
        this.jacksonMapper = payloadService.getJsonMapper();
        this.mapper = payloadService.getPayloadMapper();
        this.tagService = tagService;
        this.cachesvc = cachingPolicyService;
        this.cprops = crestProperties;
        this.edh = entityDtoHelper;
        this.prh = iovService.getPrh();
        this.jsonStreamProcessor = jsonStreamProcessor;
    }

    @Override
    public Response listPayloads(String hash, String objectType, Integer minsize,
                                 Integer page, Integer size,
                                 String sort, SecurityContext securityContext)
            throws NotFoundException {
        log.info("PayloadController processing requests for payload metadata: {} {} {}", hash,
                objectType, minsize);
        PayloadQueryArgs args = new PayloadQueryArgs();
        args.size(minsize).hash(hash).objectType(objectType);
        // Create filters
        GenericMap filters = new GenericMap();
        filters.put("hash", hash);
        filters.put("minsize", String.valueOf(minsize));
        filters.put("objectType", objectType);
        // Create pagination request
        final PageRequest preq = prh.createPageRequest(page, size, sort);
        // Search for global tags using where conditions.
        Page<Payload> entitypage = payloadService.selectPayloadList(args, preq);
        RespPage respPage = new RespPage().size(entitypage.getSize())
                .totalElements(entitypage.getTotalElements()).totalPages(entitypage.getTotalPages())
                .number(entitypage.getNumber());

        final List<PayloadDto> dtolist = edh.entityToDtoList(entitypage.toList(),
                PayloadDto.class, PayloadMapper.class);
        Response.Status rstatus = Response.Status.OK;
        // Prepare the Set.
        final CrestBaseResponse pdto = buildSet(dtolist, filters);
        pdto.page(respPage);
        // Send a response and status 200.
        return Response.status(rstatus).entity(pdto).build();
    }

    //     @CacheControlCdb("public, max-age=604800") : this has to be set on the API class itself.
    //     For the moment we decide to use the cachecontrol filter (if active) via the method
    //     name definition, by looking for the annotation @Path
    @Override
    @ProfileAndLog
    public Response getPayload(String hash, String format, SecurityContext securityContext) {
        log.info(
                "Get payload {} using format {}",
                hash, format);
        // Get only metadata from the payload.
        final Payload entity = payloadService.getPayload(hash);
        final String ptype = entity.getObjectType();
        // Check the objectType from the payload.
        String localFormat = format;
        if (ptype.equalsIgnoreCase("triggerdb")) {
            log.info("Format is triggerdb");
            localFormat = ptype;
        }

        log.debug("Found metadata {}", entity);
        if ("META".equalsIgnoreCase(format)) {
            // Return the metadata.
            final PayloadDto dto = mapper.toDto(entity);
            return Response.status(Response.Status.OK).entity(dto).build();
        }
        // Get the media type. It utilizes the objectType field.
        final MediaType mediaType = getMediaType(ptype);

        // Set caching policy depending on snapshot argument
        // this is filling a mag-age parameter in the header
        final CacheControl cc = cachesvc.getPayloadCacheControl();
        log.debug("Set cache control to {}", cc);
        String finalLocalFormat = localFormat;
        StreamingOutput streamingOutput = edh.makeStreamingOutputFromLob(
                new SimpleLobStreamerProvider() {
                    @Override
                    public InputStream getInputStream() {
                        PayloadService.LobStream lob =
                                payloadService.getLobData(hash, finalLocalFormat);
                        return lob.getInputStream();
                    }
                }
        );
        log.debug("Send back the stream....");
        // Get media type
        final String rettype = mediaType.toString();
        // Get extension
        final String ext = getExtension(ptype);
        String fname = hash + "." + ext;
        if (!entity.getObjectName().equalsIgnoreCase("none")) {
            fname = entity.getObjectName();
        }
        // Set the content type in the response, and the file name as well.
        return Response.ok(streamingOutput) /// MediaType.APPLICATION_JSON_TYPE)
                .header("Content-type", rettype)
                .header("Content-Disposition", "Inline; filename=\"" + fname + "\"")
                .cacheControl(cc)
                .build();
    }

    @Override
    @ProfileAndLog
    public Response storePayloadBatch(String tag, String jsonstoreset, String xCrestPayloadFormat,
                                      List<FormDataBodyPart> filesBodypart, String objectType,
                                      String compressionType,
                                      String version,
                                      String endtime,
                                      SecurityContext securityContext)
            throws NotFoundException {
        log.info(
                "Store payload batch in tag {} with multi-iov ",
                tag);
        try {
            // Read endtime as a BigDecimal.
            if (endtime == null) {
                endtime = "0";
            }
            BigDecimal bendtime = new BigDecimal(endtime);
            // Read input FormData as an IovSet object.
            if (tag == null || jsonstoreset == null) {
                throw new CdbBadRequestException(
                        "Cannot upload payload in batch mode : form is missing a field, " + tag
                        + " - " + jsonstoreset);
            }
            StoreSetDto storeset = jacksonMapper.readValue(jsonstoreset, StoreSetDto.class);
            log.debug("Batch insertion of {} iovs", storeset.getSize());
            // use to send back a NotFound if the tag does not exists.
            Tag tagentity = tagService.findOne(tag);
            // Check security on tag using a fake update. This will trigger the TagSecurityAspect.
            // It controls that the user has the right to update the tag.
            tagService.updateTag(tagentity);
            // Add object type.
            if (objectType == null) {
                if (storeset.getDatatype() != null) {
                    objectType = storeset.getDatatype();
                }
                else {
                    objectType = "lob";
                }
            }
            // Add version.
            if (version == null) {
                version = "default";
            }
            // Set default for payload format.
            if (xCrestPayloadFormat == null && filesBodypart != null) {
                xCrestPayloadFormat = "FILE";
            }
            StoreSetDto outdto = null;
            if ("FILE".equalsIgnoreCase(xCrestPayloadFormat)) {
                // Check that number of files is not too much.
                if (filesBodypart == null) {
                    throw new CdbBadRequestException("Cannot use header FILE with empty list of "
                                                     + "files");
                }
                if (filesBodypart.size() > MAX_FILE_UPLOAD) {
                    throw new CdbBadRequestException(
                            "Too many files uploaded : more than " + MAX_FILE_UPLOAD);
                }
                // Only the payload format FILE is allowed here.
                // This was created to eventually merge with other methods later on.
                log.debug("Store payloads from uploaded files");
                outdto = storeData(storeset, tag, objectType, version, filesBodypart);
            }
            else if ("JSON".equalsIgnoreCase(xCrestPayloadFormat)) {
                outdto = storeData(storeset, tag, objectType, version, null);
            }
            else {
                throw new CdbBadRequestException("Bad header parameter: " + xCrestPayloadFormat);
            }
            // Change the end time in the tag.
            tagService.updateModificationTime(tag, bendtime);
            // Return the result.
            log.info("Batch insertion of {} iovs done", storeset.getSize());
            // Return the result.
            outdto.format("StoreSetDto").datatype("iovs");
            RespPage respPage = new RespPage().size(outdto.getSize().intValue())
                    .totalElements(outdto.getSize()).totalPages(1).number(0);
            outdto.page(respPage);
            // Create filters
            GenericMap filters = new GenericMap();
            filters.put("name", tag);
            outdto.filter(filters);
            log.info("Return output information: {}", outdto);
            return Response.status(Response.Status.CREATED).entity(outdto).build();
        }
        catch (IOException e) {
            log.error("Runtime exception while storing iovs and payloads....");
            throw new CdbInternalException(e);
        }
    }

    @Override
    public Response uploadJson(String tag, FormDataBodyPart storesetBodypart,
                               String objectType, String compressionType, String version,
                               String endtime, SecurityContext securityContext)
            throws NotFoundException {
        log.debug("Batch insertion of json iovs+payload stream in tag {} ", tag);
        BigDecimal bendtime = new BigDecimal(endtime);
        final BodyPartEntity inputsource = (BodyPartEntity) storesetBodypart.getEntity();
        // use to send back a NotFound if the tag does not exists.
        Tag tagentity = tagService.findOne(tag);
        // Check security on tag using a fake update. This will trigger the TagSecurityAspect.
        // It controls that the user has the right to update the tag.
        tagService.updateTag(tagentity);
        // Add object type.
        if (objectType == null) {
            objectType = "lob";
        }
        // Add version.
        if (version == null) {
            version = "default";
        }
        // Set default compression to none
        if (compressionType == null) {
            compressionType = "none";
        }
        // Now you can process the JSON data as needed.
        StoreSetDto outdto = null;
        try {
            BufferedInputStream bufferedInputStream =
                    new BufferedInputStream(inputsource.getInputStream());
            outdto = jsonStreamProcessor.processJsonStream(bufferedInputStream,
                    objectType, version, compressionType, tag);
            inputsource.getInputStream().close();
        }
        catch (RuntimeException | IOException e) {
            throw new CdbInternalException("Cannot deserialize data", e);
        }
        // Change the end time in the tag.
        tagService.updateModificationTime(tag, bendtime);
        // Return the result.
        log.info("Batch insertion of {} iovs done", outdto.getSize());
        // Return the result.
        if (outdto == null) {
            throw new CdbInternalException("No response from object deserialization...");
        }
        outdto.format("StoreSetDto").datatype("iovs");
        RespPage respPage = new RespPage().size(outdto.getSize().intValue())
                .totalElements(outdto.getSize()).totalPages(1).number(0);
        outdto.page(respPage);
        // Create filters
        GenericMap filters = new GenericMap();
        filters.put("name", tag);
        outdto.filter(filters);
        log.info("Return output information: {}", outdto);
        return Response.status(Response.Status.CREATED).entity(outdto).build();
    }

    /**
     * Store iovs and payload files.
     *
     * @param dtoset         the StoreSetDto
     * @param tag            the String
     * @param objectType     the object type
     * @param version        the version
     * @param filesbodyparts the List<FormDataBodyPart>
     * @return StoreSetDto
     * @throws PayloadEncodingException    If an Exception occurred
     * @throws IOException                 If an Exception occurred
     * @throws AbstractCdbServiceException if an exception occurred in insertion.
     */
    protected StoreSetDto storeData(StoreSetDto dtoset,
                                    String tag, String objectType,
                                    String version,
                                    List<FormDataBodyPart> filesbodyparts)
            throws IOException, AbstractCdbServiceException {
        final List<StoreDto> iovlist = dtoset.getResources();
        // Loop over iovs found in the Set.
        List<StorableData> storableDataList = new ArrayList<>();
        for (final StoreDto piovDto : iovlist) {
            String filename = null;
            log.info("Store data from dto in tag {} at {}", tag, piovDto.getSince());
            final Map<String, String> sinfomap = new HashMap<>();
            sinfomap.put("format", dtoset.getDatatype());
            sinfomap.put("insertionDate", new Date().toString());
            sinfomap.put("streamerInfo", piovDto.getStreamerInfo());

            // Here we generate objectType and version. We should probably allow for input
            // arguments.
            Payload entity =
                    new Payload().setObjectType(objectType).setHash("none").setVersion(version);
            entity.setCompressionType("none");
            entity.setSize(0);
            // Initialize the iov entity from the DTO.
            Iov iov = new Iov();
            IovId iovId = new IovId();
            iovId.setSince(BigInteger.valueOf(piovDto.getSince())).setTagName(tag);
            iov.setId(iovId).setTag(new Tag().setName(tag));
            // Initialize the payload entity from the DTO.
            StorableData data;
            if (filesbodyparts == null) {
                // There are no attached files, so the data field should represent the payload
                byte[] paylodContent = piovDto.getData().getBytes(StandardCharsets.UTF_8);
                log.debug("Use the data string, it represents the payload : length is {}",
                        paylodContent.length);
                entity.setSize(paylodContent.length);
                String outFilename = generateUploadFilename("inline", tag, iov.getId().getSince());
                log.info("Dump data in file {} to compute the hash", outFilename);
                final String hash = getHash(
                        new ByteArrayInputStream(paylodContent), outFilename);
                final Map<String, Object> retmap = new HashMap<>();
                retmap.put("uploadedFile", outFilename);
                data = buildStorable(iov, entity, sinfomap, hash);
                data.streamsMap(retmap);
            }
            else {
                // If there are attached files, then the payload will be loaded from filename.
                log.debug("Use attached file : {}", piovDto.getData());
                final Map<String, Object> retmap = getDocumentStream(piovDto, filesbodyparts);
                filename = (String) retmap.get("file");
                String outFilename = generateUploadFilename(filename, tag, iov.getId().getSince());
                final String hash = getHash((InputStream) retmap.get("stream"), outFilename);
                retmap.put("uploadedFile", outFilename);
                sinfomap.put("filename", filename);
                entity.setObjectName(filename);
                data = buildStorable(iov, entity, sinfomap, hash);
                data.streamsMap(retmap);
            }
            storableDataList.add(data);
        }
        // Store all data
        log.debug("Save IOV and Payload from list of data of size {}", storableDataList.size());
        return payloadService.saveAll(storableDataList);
    }

    /**
     * Build a StorableData object.
     *
     * @param iov
     * @param entity
     * @param sinfomap
     * @param hash
     * @return StorableData
     * @throws JsonProcessingException
     */
    protected StorableData buildStorable(Iov iov, Payload entity, Map<String, String> sinfomap,
                                         String hash) throws JsonProcessingException {
        PayloadData content = new PayloadData();
        PayloadInfoData sinfodata = new PayloadInfoData();
        StorableData data = new StorableData();
        // Set the hash into the iov and entity.
        iov.setPayloadHash(hash);
        entity.setHash(hash);
        content.setHash(hash);
        // Fill the streamer info map.
        sinfodata.hash(hash).streamerInfo(jacksonMapper.writeValueAsBytes(sinfomap));
        // Fill the StorableData object.
        data.iov(iov).payload(entity).payloadData(content).payloadInfoData(sinfodata);
        return data;
    }

    /**
     * Generate the filename for the upload.
     *
     * @param fileName
     * @param tag
     * @param since
     * @return String
     */
    protected String generateUploadFilename(String fileName, String tag, BigInteger since) {
        String fdetailsname = fileName;
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
        return cprops.getDumpdir() + SLASH + tag + "_" + since
               + fdetailsname;
    }

    /**
     * @param fileInputStream the InputStream
     * @param filename        the String
     * @return String. The computed hash from the byte stream.
     * @throws PayloadEncodingException If an Exception occurred
     * @throws IOException              If an Exception occurred
     */
    protected String getHash(InputStream fileInputStream, String filename)
            throws PayloadEncodingException, IOException {

        try (BufferedInputStream bis = new BufferedInputStream(fileInputStream)) {
            if (filename.equals("none")) {
                return PayloadHandler.getHashFromStream(bis);
            }
            return PayloadHandler.saveToFileGetHash(bis, filename);
        }
    }

    @Override
    public Response updatePayload(String hash, GenericMap body,
                                  SecurityContext securityContext) {
        log.info(
                "PayloadRestController processing request for update payload meta information for"
                + " {}",
                hash);
        // Send a bad request if body is null.
        if (body == null) {
            throw new CdbBadRequestException("Cannot update payload with null body");
        }
        // Search payload. If this is not found the method will throw an exception.
        Payload entity = payloadService.getPayload(hash);
        // Start to update the payload streamer info.
        String sinfo = null;
        // Loop over map body keys.
        for (final Map.Entry<String, String> entry : body.entrySet()) {
            if ("streamerInfo".equals(entry.getKey())) {
                // Update description.
                sinfo = entry.getValue();
            }
            else {
                log.warn("Ignored key {} in updatePayload: field does not exists", entry.getKey());
            }
        }
        payloadService.updatePayloadMetaInfo(hash, sinfo);
        PayloadDto dto = mapper.toDto(entity);
        GenericMap filters = new GenericMap();
        filters.put("hash", hash);
        List<PayloadDto> dtoList = new ArrayList<>();
        dtoList.add(dto);
        final PayloadSetDto psetdto = buildSet(dtoList, filters);
        return Response.ok()
                .header("Content-type", MediaType.APPLICATION_JSON_TYPE.toString())
                .entity(psetdto).build();
    }

    /**
     * @param mddto     the IovDto
     * @param bodyParts the List<FormDataBodyPart>
     * @return Map<String, Object>
     * @throws PayloadEncodingException If an Exception occurred
     */
    protected Map<String, Object> getDocumentStream(StoreDto mddto,
                                                    List<FormDataBodyPart> bodyParts)
            throws PayloadEncodingException {
        log.debug("Extracting document BLOB for file {}", mddto.getData());
        final Map<String, Object> retmap = new HashMap<>();
        String dtofname = mddto.getData();
        if (dtofname.startsWith("file://")) {
            dtofname = mddto.getData().split("://")[1];
        }
        for (int i = 0; i < bodyParts.size(); i++) {
            final BodyPartEntity test = (BodyPartEntity) bodyParts.get(i).getEntity();
            final String fileName = bodyParts.get(i).getContentDisposition().getFileName();
            log.debug("Search for file {} in input store set", fileName);
            if (dtofname.contains(fileName)) {
                retmap.put("file", fileName);
                retmap.put("stream", test.getInputStream());
            }
        }
        if (retmap.isEmpty()) {
            throw new PayloadEncodingException(
                    "Cannot find file content in form data. File name = " + mddto.getData());
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
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM_TYPE;
        final String comp = ptype.toLowerCase();
        switch (comp) {
            case "png":
                mediaType = new MediaType("image", "png");
                break;
            case "svg":
                mediaType = MediaType.APPLICATION_SVG_XML_TYPE;
                break;
            case "json":
                mediaType = MediaType.APPLICATION_JSON_TYPE;
                break;
            case "xml":
                mediaType = MediaType.APPLICATION_XML_TYPE;
                break;
            case "csv":
                mediaType = new MediaType("text", "csv");
                break;
            case "txt":
                mediaType = MediaType.TEXT_PLAIN_TYPE;
                break;
            case "tgz":
                mediaType = new MediaType(APPLICATION_MEDIA, "x-gtar-compressed");
                break;
            case "gz":
                mediaType = new MediaType(APPLICATION_MEDIA, "gzip");
                break;
            case "pdf":
                mediaType = new MediaType(APPLICATION_MEDIA, "pdf");
                break;
            default:
                break;
        }
        return mediaType;
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
     * @param entityList the PayloadDto list
     * @param map        the filters
     * @return PayloadSetDto
     */
    protected PayloadSetDto buildSet(List<PayloadDto> entityList, GenericMap map) {
        final PayloadSetDto psetdto = new PayloadSetDto().resources(entityList);
        psetdto.datatype("payloads").filter(map).size((long) entityList.size());
        return psetdto;
    }

}
