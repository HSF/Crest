package hep.crest.server.swagger.api.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import com.querydsl.core.types.dsl.BooleanExpression;

import hep.crest.data.config.CrestProperties;
import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.repositories.querydsl.IFilteringCriteria;
import hep.crest.data.repositories.querydsl.SearchCriteria;
import hep.crest.server.caching.CachingPolicyService;
import hep.crest.server.caching.CachingProperties;
import hep.crest.server.controllers.PageRequestHelper;
import hep.crest.server.exceptions.AlreadyExistsPojoException;
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
import hep.crest.swagger.model.TagDto;
import hep.crest.swagger.model.TagSummaryDto;
import hep.crest.swagger.model.TagSummarySetDto;

/**
 * Rest endpoint for iov management. It allows to create and find iovs.
 * 
 * @author formica
 *
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
            final IovDto saved = iovService.insertIov(body);
            return Response.created(info.getRequestUri()).entity(saved).build();

        }
        catch (final NotExistsPojoException e) {
            // Exception. Send a 404.
            log.error("Exception in creating iov : {}", e.getMessage());
            return notFoundPojo(body.getTagName());
        }
        catch (final AlreadyExistsPojoException e) {
            // IOV exists already. Send a 303.
            log.error("Iov already exists : {}", e.getMessage());
            return alreadyExistsPojo("IOV already exists");
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

        // Method to upload multiple IOVs.

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
                final IovDto saved = iovService.insertIov(iovDto);
                // Add to saved list.
                savedList.add(saved);
            }
            // Prepare the Set for the response.
            final CrestBaseResponse saveddto = new IovSetDto().resources(savedList).filter(filters)
                    .format("IovSetDto").size((long) savedList.size()).datatype("iovs");
            // Send 201.
            return Response.created(info.getRequestUri()).entity(saveddto).build();

        }
        catch (final NotExistsPojoException e) {
            // Exception. Send a 404.
            return notFoundPojo(tagName);
        }
        catch (final AlreadyExistsPojoException e) {
            // IOV exists already. Send a 303.
            log.error("Iov already exists : {}", e.getMessage());
            return alreadyExistsPojo("IOV already exists");
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
            // Parse the input parameters to create pagination and
            // sorting PageRequest.
            List<SearchCriteria> params = null;
            final PageRequest preq = prh.createPageRequest(page, size, sort);
            List<IovDto> dtolist = null;
            // The tagname inside search string is mandatory.
            if (!by.matches("(.*)tag.ame(.*)")) {
                // If the tagname is not among the parameter then return a 406.
                final String message = "Cannot search iovs without a tagname selection.";
                final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,
                        message);
                return Response.status(Response.Status.NOT_ACCEPTABLE).entity(resp).build();
            }
            // Prepare the criteria.
            params = prh.createMatcherCriteria(by, dateformat);
            final List<BooleanExpression> expressions = filtering.createFilteringConditions(params);
            BooleanExpression wherepred = null;

            for (final BooleanExpression exp : expressions) {
                if (wherepred == null) {
                    wherepred = exp;
                }
                else {
                    wherepred = wherepred.and(exp);
                }
            }
            // Find all iovs with where conditions.
            dtolist = iovService.findAllIovs(wherepred, preq);
            if (dtolist == null) {
                // Empty list. Send 404. Should we use a Set ?
                final String message = "No resource has been found";
                final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.INFO,
                        message);
                return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
            }
            // Retrieve the tagname used in order to store it in the response as a filter.
            final String tagname = prh.getParam(params, "tagname");
            final GenericMap filters = new GenericMap();
            filters.put("tagName", tagname);
            // Prepare the response set.
            final CrestBaseResponse saveddto = new IovSetDto().resources(dtolist).filter(filters)
                    .format("IovSetDto").size((long) dtolist.size()).datatype("iovs");
            // Send a response and status 200.
            return Response.ok().entity(saveddto).build();

        }
        catch (final CdbServiceException e) {
            // Exception. Send a 500.
            final String message = e.getMessage();
            log.error("Internal error searching for iovs : {}", message);
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,
                    message);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
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
        catch (final Exception e) {
            // Exception, send a response with 500.
            final String message = e.getMessage();
            log.error("Error in getting size for tagname {} : {}", tagname, message);
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,
                    message);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
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
        catch (final Exception e) {
            // Exception, send a 500.
            log.error("Error in getting size by tagname {} : {}", tagname, e.getMessage());
            final String message = e.getMessage();
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,
                    message);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
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
            final TagDto tagentity = tagService.findOne(tagname);

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
            return notFoundPojo(tagname);
        }
        catch (final Exception e) {
            // Exception, send a 500.
            final String msg = "Error in selectGroups : " + tagname + ", " + snapshot;
            log.error("Exception catched by REST controller for {} : {}", msg, e.getMessage());
            final String message = msg + " -- " + e.getMessage();
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,
                    message);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
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
            List<IovDto> dtolist = null;
            // Search if tag exists.
            final TagDto tagentity = tagService.findOne(tagname);
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
            dtolist = iovService.selectIovsByTagRangeSnapshot(tagname, rsince, runtil, snap,
                    xCrestQuery);
            // Prepare the response set.
            final IovSetDto respdto = new IovSetDto();
            ((IovSetDto) respdto.datatype("iovs")).resources(dtolist).size((long) dtolist.size());
            final GenericMap filters = new GenericMap();
            filters.put("tagName", tagname);
            filters.put("snapshot", snapshot.toString());
            filters.put("since", rsince.toString());
            filters.put("until", runtil.toString());
            respdto.filter(filters);
            respdto.format("IovSetDto");
            // Send the cache control in the response.
            return Response.ok().entity(respdto).cacheControl(cc).build();

        }
        catch (final NotExistsPojoException e) {
            // The tag was not found.
            return notFoundPojo(tagname);
        }
        catch (final Exception e) {
            // Exception. Send a 500.
            final String msg = "Error in selectIovs : " + tagname + ", " + snapshot;
            log.error("Exception catched by REST controller for {} : {}", msg, e.getMessage());
            final String message = msg + " -- " + e.getMessage();
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,
                    message);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
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
            final List<IovDto> entitylist = iovService.selectSnapshotByTag(tagname, snap);
            // Create the Set for the response.
            final IovSetDto respdto = new IovSetDto();
            respdto.resources(entitylist).size((long) entitylist.size());
            final GenericMap filters = new GenericMap();
            filters.put("tagName", tagname);
            filters.put("snapshot", snapshot.toString());
            respdto.datatype("iovs").filter(filters);
            respdto.format("IovSetDto");
            // Send 200.
            return Response.ok().entity(respdto).build();

        }
        catch (final NotExistsPojoException e) {
            // The tag was not found.
            return notFoundPojo(tagname);
        }
        catch (final Exception e) {
            // Exception, send a 500.
            final String msg = "Error in selectSnapshot : " + tagname + ", " + snapshot;
            log.error("Exception catched by REST controller for {} : {}", msg, e.getMessage());
            final String message = msg + " -- " + e.getMessage();
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,
                    message);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
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
            final IovDto last = iovService.latest(tagname, since, dateformat);
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
            dtolist.add(last);
            final CrestBaseResponse saveddto = new IovSetDto().resources(dtolist).filter(filters)
                    .size(1L).datatype("iovs");
            saveddto.format("IovSetDto");
            return Response.ok().entity(saveddto).build();

        }
        catch (final NotExistsPojoException e) {
            // Tag does not exists, send a 404.
            return notFoundPojo(tagname);
        }
        catch (final CdbServiceException e) {
            // Exception, send a 500.
            final String message = e.getMessage();
            log.error("Internal error searching for iovs : {}", message);
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,
                    message);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
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
            final TagDto tagentity = tagService.findOne(tagname);
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
            return notFoundPojo(tagname);
        }
        catch (final Exception e) {
            // Exception, send a 500.
            final String msg = "Error in selectIovPayloads : " + tagname + ", " + snapshot;
            log.error("Exception catched by REST controller for {} : {}", msg, e.getMessage());
            final String message = msg + " -- " + e.getMessage();
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,
                    message);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
        }
    }

    /**
     * @param tagname
     *            the String
     * @return Response
     */
    protected Response notFoundPojo(String tagname) {
        final String msg = "Cannot find tag " + tagname;
        final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.INFO, msg);
        return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
    }

    /**
     * @param msg
     *            the String
     * @return Response
     */
    protected Response alreadyExistsPojo(String msg) {
        final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.INFO, msg);
        return Response.status(Response.Status.SEE_OTHER).entity(resp).build();
    }

}
