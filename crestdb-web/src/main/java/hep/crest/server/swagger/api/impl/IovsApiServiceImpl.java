package hep.crest.server.swagger.api.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import hep.crest.data.config.CrestProperties;
import hep.crest.data.pojo.Iov;
import hep.crest.data.pojo.Tag;
import hep.crest.data.repositories.querydsl.IFilteringCriteria;
import hep.crest.server.caching.CachingPolicyService;
import hep.crest.server.caching.CachingProperties;
import hep.crest.server.controllers.EntityDtoHelper;
import hep.crest.server.controllers.PageRequestHelper;
import hep.crest.server.exceptions.NotExistsPojoException;
import hep.crest.server.services.IovService;
import hep.crest.server.services.TagService;
import hep.crest.server.swagger.api.ApiResponseMessage;
import hep.crest.server.swagger.api.IovsApiService;
import hep.crest.server.swagger.api.NotFoundException;
import hep.crest.swagger.model.CrestBaseResponse;
import hep.crest.swagger.model.GenericMap;
import hep.crest.swagger.model.IovDto;
import hep.crest.swagger.model.IovPayloadDto;
import hep.crest.swagger.model.IovPayloadSetDto;
import hep.crest.swagger.model.IovSetDto;
import hep.crest.swagger.model.TagSummaryDto;
import hep.crest.swagger.model.TagSummarySetDto;
import ma.glasnost.orika.MapperFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Rest endpoint for iov management. It allows to create and find iovs.
 *
 * @author formica
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2017-09-05T16:23:23.401+02:00")
@Component
public class IovsApiServiceImpl extends IovsApiService {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(IovsApiServiceImpl.class);

    /**
     * Helper.
     */
    @Autowired
    private PageRequestHelper prh;
    /**
     * Helper.
     */
    @Autowired
    EntityDtoHelper edh;

    /**
     * Filtering.
     */
    @Autowired
    @Qualifier("iovFiltering")
    private IFilteringCriteria filtering;

    /**
     * Service.
     */
    @Autowired
    private CachingPolicyService cachesvc;

    /**
     * Service.
     */
    @Autowired
    private IovService iovService;

    /**
     * Service.
     */
    @Autowired
    private TagService tagService;

    /**
     * Properties.
     */
    @Autowired
    private CachingProperties cprops;
    /**
     * Mapper.
     */
    @Autowired
    @Qualifier("mapper")
    private MapperFacade mapper;

    /*
     * (non-Javadoc)
     *
     * @see
     * hep.crest.server.swagger.api.IovsApiService#createIov(hep.crest.swagger.model
     * .IovDto, javax.ws.rs.core.SecurityContext, javax.ws.rs.core.UriInfo)
     */
    @Override
    public Response createIov(IovDto body, SecurityContext securityContext, UriInfo info)
            throws NotFoundException {
        log.info("IovRestController processing request for creating an iov");
        try {
            // Create a new IOV.
            String tagname = body.getTagName();
            Iov entity = mapper.map(body, Iov.class);
            entity.setTag(new Tag(tagname));
            final Iov saved = iovService.insertIov(entity);
            IovDto dto = mapper.map(saved, IovDto.class);
            dto.tagName(tagname);
            return Response.created(info.getRequestUri()).entity(dto).build();

        }
        catch (final NotExistsPojoException e) {
            // Exception. Send a 404.
            log.error("Exception in creating iov, tag resource does not exists : {}", e.getMessage());
            return notFoundPojo(body.getTagName());
        }
        catch (final RuntimeException e) {
            // Exception. Send a 500.
            final String message = e.getMessage();
            log.error("Api method createIov got exception creating resource {}: {}", body, message);
            return internalError(message);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * hep.crest.server.swagger.api.IovsApiService#storeBatchIovMultiForm(hep.crest.
     * swagger.model.IovSetDto, javax.ws.rs.core.SecurityContext,
     * javax.ws.rs.core.UriInfo)
     */
    @Override
    public Response storeBatchIovMultiForm(IovSetDto dto, SecurityContext securityContext,
                                           UriInfo info) throws NotFoundException {
        log.info("IovRestController processing request to upload iovs batch {}", dto);
        // Get filters map and initializa tag name.
        final GenericMap filters = dto.getFilter();
        String tagName = "unknown";

        // Check the filter map. If it exists it should contain the tag name.
        if (filters != null && filters.containsKey("tagName")) {
            // the tag name is in the filter map.
            tagName = filters.get("tagName");
        }
        // If no tag name was found in the filter we search for it in the resources
        // list.
        // This check is performed below.
        // Now start going through uploaded iovs.
        try {
            log.info("Batch insertion of {} iovs using file formatted in {} for tag {}",
                    dto.getSize(), dto.getFormat(), tagName);
            // Prepare the iov list to insert and a list representing iovs really inserted.
            final List<IovDto> iovlist = dto.getResources();
            final List<IovDto> savedList = new ArrayList<>();

            // Loop over resources uploaded.
            for (final IovDto iovDto : iovlist) {
                // Verify if tagname should be taken inside the iovdto.
                if (!"unknown".equals(tagName)
                        && (iovDto.getTagName() == null || !iovDto.getTagName().equals(tagName))) {
                    iovDto.setTagName(tagName);
                }
                else if (iovDto.getTagName() == null) {
                    // Tag name is not available, send a response 406.
                    final String msg = "Error creating multi iov resource because tagName is not defined ";
                    final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,
                            msg);
                    return Response.status(Response.Status.NOT_ACCEPTABLE).entity(resp).build();
                }
                // Create new iov.
                Iov entity = mapper.map(iovDto, Iov.class);
                entity.setTag(new Tag(iovDto.getTagName()));
                final Iov saved = iovService.insertIov(entity);
                IovDto saveddto = mapper.map(saved, IovDto.class);
                saveddto.tagName(iovDto.getTagName());
                // Add to saved list.
                savedList.add(saveddto);
            }
            // Prepare the Set for the response.
            final CrestBaseResponse saveddto = buildEntityResponse(savedList, filters);
            // Send 201.
            return Response.created(info.getRequestUri()).entity(saveddto).build();
        }
        catch (final NotExistsPojoException e) {
            // Exception. Send a 404.
            log.error("Api method storeBatchIovMultiForm tag not found {}", tagName);
            return notFoundPojo(tagName);
        }
        catch (final RuntimeException e) {
            // Exception. Send a 500.
            final String message = e.getMessage();
            log.error("Api method storeBatchIovMultiForm got exception creating resources: {}", message);
            return internalError(message);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * hep.crest.server.swagger.api.IovsApiService#findAllIovs(java.lang.String,
     * java.lang.Integer, java.lang.Integer, java.lang.String,
     * javax.ws.rs.core.SecurityContext, javax.ws.rs.core.UriInfo)
     */
    @Override
    public Response findAllIovs(String by, Integer page, Integer size, String sort,
                                String dateformat, SecurityContext securityContext, UriInfo info)
            throws NotFoundException {
        try {
            log.debug("Search resource list using by={}, page={}, size={}, sort={}", by, page, size,
                    sort);
            // Date format. Default is milliseconds.
            if (dateformat == null) {
                dateformat = "ms";
            }
            // A filter on tag name should be mandatory in by.
            if (!by.matches("(.*)tag.ame(.*)")) {
                // If the tagname is not among the parameter then return a 406.
                final String message = "Cannot search iovs without a tagname selection.";
                final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,
                        message);
                return Response.status(Response.Status.NOT_ACCEPTABLE).entity(resp).build();
            }
            // Create filters
            GenericMap filters = prh.getFilters(prh.createMatcherCriteria(by));
            // Create pagination request
            final PageRequest preq = prh.createPageRequest(page, size, sort);
            BooleanExpression wherepred = null;
            if (!"none".equals(by)) {
                // Create search conditions for where statement in SQL
                wherepred = prh.buildWhere(filtering, by);
            }
            // Search for global tags using where conditions.
            Iterable<Iov> entitylist = iovService.findAllIovs(wherepred, preq);
            final List<IovDto> dtolist = edh.entityToDtoList(entitylist, IovDto.class);
            Response.Status rstatus = Response.Status.OK;
            // Prepare the Set.
            final CrestBaseResponse saveddto = buildEntityResponse(dtolist, filters);
            // Send a response and status 200.
            return Response.status(rstatus).entity(saveddto).build();
        }
        catch (final RuntimeException e) {
            // Exception. Send a 500.
            final String message = e.getMessage();
            log.error("Api method findAllIovs got exception : {}", message);
            return internalError(message);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see hep.crest.server.swagger.api.IovsApiService#getSize(java.lang.String,
     * java.lang.Long, javax.ws.rs.core.SecurityContext, javax.ws.rs.core.UriInfo)
     */
    @Override
    public Response getSize(@NotNull String tagname, Long snapshot, SecurityContext securityContext,
                            UriInfo info) throws NotFoundException {
        try {
            // Get the size of a tag (i.e. the total number of IOVs).
            Long size = 0L;
            if (snapshot != 0L) {
                // snapshot is not null, use it for the request.
                Date snap = null;
                snap = new Date(snapshot);
                size = iovService.getSizeByTagAndSnapshot(tagname, snap);
            }
            else {
                // snapshot is null, ignore it for the request, get simply all iovs.
                size = iovService.getSizeByTag(tagname);
            }
            // Prepare the response set.
            final CrestBaseResponse respdto = new CrestBaseResponse();
            final GenericMap filters = new GenericMap();
            filters.put("tagName", tagname);
            filters.put("snapshot", snapshot.toString());

            // The size is filled in this response. There are no resources.
            respdto.size(size).datatype("count").filter(filters);
            respdto.format("IovSetDto");

            // Send a response and status 200.
            return Response.ok().entity(respdto).build();
        }
        catch (final RuntimeException e) {
            // Exception, send a response with 500.
            final String message = e.getMessage();
            log.error("Api method getSize got exception counting iov for tag {}: {}", tagname, message);
            return internalError(message);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * hep.crest.server.swagger.api.IovsApiService#getSizeByTag(java.lang.String,
     * javax.ws.rs.core.SecurityContext, javax.ws.rs.core.UriInfo)
     */
    @Override
    public Response getSizeByTag(@NotNull String tagname, SecurityContext securityContext,
                                 UriInfo info) throws NotFoundException {
        try {
            // Get the tag summary list corresponding to the tagname pattern.
            // The method in the service sends back always a list, eventually empty.
            final List<TagSummaryDto> entitylist = iovService.getTagSummaryInfo(tagname);
            final TagSummarySetDto respdto = new TagSummarySetDto();
            final GenericMap filters = new GenericMap();
            filters.put("tagName", tagname);
            // Prepare the Set.
            ((TagSummarySetDto) respdto.size((long) entitylist.size()).datatype("count")
                    .filter(filters)).resources(entitylist);
            // Send a response 200. Even if the result is an empty list.
            return Response.ok().entity(respdto).build();
        }
        catch (final RuntimeException e) {
            // Exception, send a 500.
            final String message = e.getMessage();
            log.error("Api method getSizeByTag got exception in summary for tag {}: {}", tagname, message);
            return internalError(message);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * hep.crest.server.swagger.api.IovsApiService#selectGroups(java.lang.String,
     * java.lang.Long, javax.ws.rs.core.SecurityContext, javax.ws.rs.core.UriInfo,
     * javax.ws.rs.core.Request, javax.ws.rs.core.HttpHeaders)
     */
    @Override
    public Response selectGroups(@NotNull String tagname, Long snapshot,
                                 SecurityContext securityContext, UriInfo info, Request request, HttpHeaders headers)
            throws NotFoundException {
        log.info("IovRestController processing request for iovs groups using tag name {}", tagname);
        try {
            // Search for tag in order to load the time type:
            final Tag tagentity = tagService.findOne(tagname);

            // Apply caching on iov groups selections.
            // Use cache service to detect if a tag was modified.
            final ResponseBuilder builder = cachesvc.verifyLastModified(request, tagentity);
            if (builder != null) {
                // Get request headers: this is just to dump the If-Modified-Since
                final String ifmodsince = headers.getHeaderString("If-Modified-Since");
                log.debug("The output data are not modified since " + ifmodsince);
                return builder.build();
            }
            Long groupsize = null;
            // Get the time type to apply different group selections.
            final String timetype = tagentity.getTimeType();
            if (timetype.equalsIgnoreCase("run")) {
                // The iov is of type RUN. Use the group size from properties.
                groupsize = new Long(cprops.getRuntypeGroupsize());
            }
            else if (timetype.equalsIgnoreCase("run-lumi")) {
                // The iov is of type RUN-LUMI. Use the group size from properties.
                groupsize = new Long(cprops.getRuntypeGroupsize());
                // transform to COOL run-lumi
                groupsize = groupsize * 4294967296L;
            }
            else {
                // Assume COOL time format...
                groupsize = new Long(cprops.getTimetypeGroupsize());
                // transform to COOL nanosec
                groupsize = groupsize * 1000000000L;
            }
            // Set caching policy depending on snapshot argument
            // this is filling a mag-age parameter in the header
            final CacheControl cc = cachesvc.getGroupsCacheControl(snapshot);
            // Retrieve all iovs groups
            Date snap = null;
            if (snapshot != 0L) {
                // Set the snapshot.
                snap = new Date(snapshot);
            }
            // Get the iov groups from the DB, use eventually snapshot.
            final CrestBaseResponse respdto = iovService
                    .selectGroupDtoByTagNameAndSnapshotTime(tagname, snap, groupsize);
            final GenericMap filters = new GenericMap();
            filters.put("tagName", tagname);
            filters.put("snapshot", snapshot.toString());
            filters.put("groupsize", groupsize.toString());
            respdto.datatype("groups").filter(filters);
            respdto.format("IovSetDto");
            // In the response set the cachecontrol flag as well.
            return Response.ok().entity(respdto).cacheControl(cc).build();

        }
        catch (final NotExistsPojoException e) {
            // The tag was not found.
            log.error("Api method selectGroups tag not found: {}", tagname);
            return notFoundPojo(tagname);
        }
        catch (final RuntimeException e) {
            // Exception, send a 500.
            final String msg = "groups for " + tagname + ", " + snapshot;
            final String message = msg + " -- " + e.getMessage();
            log.error("Api method selectGroups got exception: {}", message);
            return internalError(message);
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see hep.crest.server.swagger.api.IovsApiService#selectIovs(java.lang.String,
     * java.lang.String, java.lang.String, java.lang.String, java.lang.Long,
     * javax.ws.rs.core.SecurityContext, javax.ws.rs.core.UriInfo,
     * javax.ws.rs.core.Request, javax.ws.rs.core.HttpHeaders)
     */
    @Override
    public Response selectIovs(String xCrestQuery, String tagname, String since, String until,
                               Long snapshot, SecurityContext securityContext, UriInfo info, Request request,
                               HttpHeaders headers) throws NotFoundException {
        log.info(
                "IovRestController processing request for iovs using tag name {} and range {} - {} ",
                tagname, since, until);
        try {
            // Start IOV selection in the given time range.
            // Search if tag exists.
            final Tag tagentity = tagService.findOne(tagname);
            log.debug("Found tag " + tagentity);
            // Apply caching on iov selections.
            // Use cache service to detect if a tag was modified.
            final ResponseBuilder builder = cachesvc.verifyLastModified(request, tagentity);
            if (builder != null) {
                // Get request headers: this is just to dump the If-Modified-Since
                final String ifmodsince = headers.getHeaderString("If-Modified-Since");
                log.debug("The output data are not modified since " + ifmodsince);
                // Send back the response via the builder.
                return builder.build();
            }
            // The query was not cached.
            log.debug("Setting iov range to : {}, {}", since, until);
            BigDecimal runtil = null;
            if (until.equals("INF")) {
                // Until time is INF.
                log.debug("The end time will be set to : {}", CrestProperties.INFINITY);
                runtil = CrestProperties.INFINITY;
            }
            else {
                // set until time.
                runtil = new BigDecimal(until);
                log.debug("The end time will be set to : " + runtil);
            }
            final BigDecimal rsince = new BigDecimal(since);
            Date snap = null;
            if (snapshot != 0L) {
                // Set the snapshot.
                snap = new Date(snapshot);
            }
            // Set caching policy depending on snapshot argument
            // this is filling a mag-age parameter in the header
            final CacheControl cc = cachesvc.getIovsCacheControlForUntil(snapshot, runtil);
            // Retrieve all iovs
            if (xCrestQuery == null) {
                // Set the default header parameters.
                xCrestQuery = "groups";
            }
            // Retrieve IOV list.
            Iterable<Iov> entitylist = iovService.selectIovsByTagRangeSnapshot(tagname, rsince, runtil, snap,
                    xCrestQuery);
            // create dto list
            List<IovDto> dtolist = edh.entityToDtoList(entitylist, IovDto.class);

            // Prepare the response set.
            final GenericMap filters = new GenericMap();
            filters.put("tagName", tagname);
            filters.put("snapshot", snapshot.toString());
            filters.put("since", rsince.toString());
            filters.put("until", runtil.toString());
            final CrestBaseResponse respdto = buildEntityResponse(dtolist, filters);
            // Send the cache control in the response.
            return Response.ok().entity(respdto).cacheControl(cc).build();
        }
        catch (final NotExistsPojoException e) {
            // The tag was not found.
            log.error("Api method selectIovs tag not found: {}", tagname);
            return notFoundPojo(tagname);
        }
        catch (final RuntimeException e) {
            // Exception. Send a 500.
            final String msg = "iovs for " + tagname + ", " + snapshot;
            final String message = msg + " -- " + e.getMessage();
            log.error("Api method selectIovs got exception: {}", message);
            return internalError(message);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * hep.crest.server.swagger.api.IovsApiService#selectSnapshot(java.lang.String,
     * java.lang.Long, javax.ws.rs.core.SecurityContext, javax.ws.rs.core.UriInfo)
     */
    @Override
    public Response selectSnapshot(@NotNull String tagname, @NotNull Long snapshot,
                                   SecurityContext securityContext, UriInfo info) throws NotFoundException {
        try {
            Date snap = new Date();
            if (snapshot != 0L) {
                // Set the snapshot
                snap = new Date(snapshot);
            }
            // Search if tag exists.
            tagService.findOne(tagname);
            // Select IOVs using tag and snapshot. All IOVs will be retrieved.
            final Iterable<Iov> entitylist = iovService.selectSnapshotByTag(tagname, snap);
            // Create dto list
            List<IovDto> dtolist = edh.entityToDtoList(entitylist, IovDto.class);
            // Create the Set for the response.
            final GenericMap filters = new GenericMap();
            filters.put("tagName", tagname);
            filters.put("snapshot", snapshot.toString());
            final CrestBaseResponse respdto = buildEntityResponse(dtolist, filters);

            // Send 200.
            return Response.ok().entity(respdto).build();

        }
        catch (final NotExistsPojoException e) {
            // The tag was not found.
            log.error("Api method selectSnapshot tag not found: {}", tagname);
            return notFoundPojo(tagname);
        }
        catch (final RuntimeException e) {
            // Exception, send a 500.
            final String msg = "iovs for " + tagname + ", " + snapshot;
            final String message = msg + " -- " + e.getMessage();
            log.error("Api method selectSnapshot got exception: {}", message);
            return internalError(message);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see hep.crest.server.swagger.api.IovsApiService#lastIov(java.lang.String,
     * java.lang.String, java.lang.Long, javax.ws.rs.core.SecurityContext,
     * javax.ws.rs.core.UriInfo)
     */
    @Override
    public Response lastIov(String tagname, String since, Long snapshot, String dateformat,
                            SecurityContext securityContext, UriInfo info) throws NotFoundException {
        try {
            log.debug(
                    "Search resource list using tagname={} and before since={}, using date format {}",
                    tagname, since, dateformat);

            if (dateformat == null) {
                // Set default date format to milliseconds.
                dateformat = "ms";
            }
            // Search if tag exists.
            tagService.findOne(tagname);
            // Get the last IOV.
            final Iov last = iovService.latest(tagname, since, dateformat);
            if (last == null) {
                final String message = "No resource has been found";
                final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.INFO,
                        message);
                return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
            }
            final GenericMap filters = new GenericMap();
            filters.put("tagName", tagname);
            filters.put("since", since);
            // Store the last IOV into a list for Set creation.
            final List<IovDto> dtolist = new ArrayList<>();
            IovDto dto = mapper.map(last, IovDto.class);
            dtolist.add(dto);
            final CrestBaseResponse saveddto = buildEntityResponse(dtolist, filters);
            return Response.ok().entity(saveddto).build();

        }
        catch (final NotExistsPojoException e) {
            // Tag does not exists, send a 404.
            log.error("Api method lastIov tag not found: {}", tagname);
            return notFoundPojo(tagname);
        }
        catch (final RuntimeException e) {
            // Exception, send a 500.
            final String msg = "iovs for " + tagname + ", " + snapshot;
            final String message = msg + " -- " + e.getMessage();
            log.error("Api method lastIov got exception: {}", message);
            return internalError(message);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see hep.crest.server.swagger.api.IovsApiService#selectIovPayloads(java.lang.
     * String, java.lang.String, java.lang.String, java.lang.String, java.lang.Long,
     * javax.ws.rs.core.SecurityContext, javax.ws.rs.core.UriInfo)
     */
    @Override
    public Response selectIovPayloads(String xCrestQuery, String tagname, String since,
                                      String until, Long snapshot, SecurityContext securityContext, UriInfo info)
            throws NotFoundException {
        log.info(
                "IovRestController processing request for iovs and payloads meta using tag name {} and range {} - {} ",
                tagname, since, until);
        try {
            List<IovPayloadDto> dtolist = null;
            // Retrieve all iovs
            final Tag tagentity = tagService.findOne(tagname);
            log.debug("Found tag " + tagentity);

            log.debug("Setting iov range to : {}, {}", since, until);
            BigDecimal runtil = null;
            if (until.equals("INF")) {
                // Until is INF.
                log.debug("The end time will be set to : {}", CrestProperties.INFINITY);
                runtil = CrestProperties.INFINITY;
            }
            else {
                // Set the until time.
                runtil = new BigDecimal(until);
                log.debug("The end time will be set to : " + runtil);
            }
            final BigDecimal rsince = new BigDecimal(since);
            Date snap = new Date();
            if (snapshot != 0L) {
                // Set the snapshot.
                snap = new Date(snapshot);
            }
            // Get the IOV list.
            dtolist = iovService.selectIovPayloadsByTagRangeSnapshot(tagname, rsince, runtil, snap);
            final IovPayloadSetDto respdto = new IovPayloadSetDto();
            // Create the Set for the response.
            ((IovPayloadSetDto) respdto.datatype("iovpayloads")).resources(dtolist)
                    .size((long) dtolist.size());
            final GenericMap filters = new GenericMap();
            filters.put("tagName", tagname);
            filters.put("snapshot", snapshot.toString());
            filters.put("since", rsince.toString());
            filters.put("until", runtil.toString());
            respdto.filter(filters);
            respdto.format("IovPayloadSetDto");
            return Response.ok().entity(respdto).build();

        }
        catch (final NotExistsPojoException e) {
            log.error("Api method selectIovPayloads tag not found: {}", tagname);
            return notFoundPojo(tagname);
        }
        catch (final RuntimeException e) {
            // Exception, send a 500.
            final String msg = "iovs for " + tagname + ", " + snapshot;
            final String message = msg + " -- " + e.getMessage();
            log.error("Api method selectIovPayloads got exception: {}", message);
            return internalError(message);
        }
    }

    /**
     * @param tagname the String
     * @return Response
     */
    protected Response notFoundPojo(String tagname) {
        final String msg = "Cannot find tag " + tagname;
        final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.INFO, msg);
        return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
    }

    /**
     * @param msg the String
     * @return Response
     */
    protected Response alreadyExistsPojo(String msg) {
        final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.INFO, msg);
        return Response.status(Response.Status.SEE_OTHER).entity(resp).build();
    }

    /**
     * @param msg the String
     * @return Response
     */
    protected Response internalError(String msg) {
        // Exception, send a 500.
        final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
    }

    /**
     * Factorise code to build the IovSetDto.
     * @param dtolist the List<IovDto>
     * @param filters the GenericMap
     * @return IovSetDto
     */
    protected IovSetDto buildEntityResponse(List<IovDto> dtolist, GenericMap filters) {
        final IovSetDto respdto = new IovSetDto();
        // Create the Set for the response.
        ((IovSetDto) respdto.datatype("iovs")).resources(dtolist)
                .size((long) dtolist.size());
        respdto.filter(filters);
        respdto.format("IovSetDto");
        return respdto;
    }

}
