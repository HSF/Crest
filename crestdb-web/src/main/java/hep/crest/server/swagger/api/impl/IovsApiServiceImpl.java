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
    private final Logger log = LoggerFactory.getLogger(this.getClass());

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
            final IovDto saved = iovService.insertIov(body);
            return Response.created(info.getRequestUri()).entity(saved).build();

        }
        catch (final CdbServiceException e) {
            log.error("Exception in creating iov : {}", e.getMessage());
            final String message = e.getMessage();
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,
                    message);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
        }
        catch (final AlreadyExistsPojoException e) {
            log.error("Iov already exists : {}", e.getMessage());
            final String message = e.getMessage();
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,
                    message);
            return Response.status(Response.Status.SEE_OTHER).entity(resp).build();
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
        this.log.info("IovRestController processing request to upload iovs batch {}", dto);
        final GenericMap filters = dto.getFilter();
        String tagName = "unknown";

        if (filters != null && !filters.containsKey("tagName")) {
            final String msg = "Error creating multi iov resource because tagName is not defined ";
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity(resp).build();
        }
        else if (filters != null) {
            tagName = filters.get("tagName");
        }
        try {
            log.info("Batch insertion of {} iovs using file formatted in {} for tag {}",
                    dto.getSize(), dto.getFormat(), tagName);
            final List<IovDto> iovlist = dto.getResources();
            final List<IovDto> savedList = new ArrayList<>();
            for (final IovDto iovDto : iovlist) {
                if (!tagName.equals("unknown")
                        && (iovDto.getTagName() == null || !iovDto.getTagName().equals(tagName))) {
                    iovDto.setTagName(tagName);
                }
                else if (iovDto.getTagName() == null) {
                    final String msg = "Error creating multi iov resource because tagName is not defined ";
                    final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,
                            msg);
                    return Response.status(Response.Status.NOT_ACCEPTABLE).entity(resp).build();
                }
                final IovDto saved = iovService.insertIov(iovDto);
                savedList.add(saved);
            }
            final CrestBaseResponse saveddto = new IovSetDto().resources(savedList).filter(filters)
                    .format("IovSetDto").size((long) savedList.size()).datatype("iovs");
            return Response.created(info.getRequestUri()).entity(saveddto).build();

        }
        catch (final Exception e) {
            final String msg = "Internal exception creating payload resource using uploadBatch "
                    + tagName + " : " + e.getMessage();
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
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

            if (dateformat == null) {
                dateformat = "ms";
            }
            List<SearchCriteria> params = null;
            final PageRequest preq = prh.createPageRequest(page, size, sort);
            List<IovDto> dtolist = null;
            if (!by.matches("(.*)tag.ame(.*)")) {
                final String message = "Cannot search iovs without a tagname selection.";
                final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,
                        message);
                return Response.status(Response.Status.NOT_ACCEPTABLE).entity(resp).build();
            }
            else {

                params = prh.createMatcherCriteria(by, dateformat);
                final List<BooleanExpression> expressions = filtering
                        .createFilteringConditions(params);
                BooleanExpression wherepred = null;

                for (final BooleanExpression exp : expressions) {
                    if (wherepred == null) {
                        wherepred = exp;
                    }
                    else {
                        wherepred = wherepred.and(exp);
                    }
                }
                dtolist = iovService.findAllIovs(wherepred, preq);
            }
            if (dtolist == null) {
                final String message = "No resource has been found";
                final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.INFO,
                        message);
                return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
            }
            final String tagname = prh.getParam(params, "tagname");
            final GenericMap filters = new GenericMap();
            filters.put("tagName", tagname);
            final CrestBaseResponse saveddto = new IovSetDto().resources(dtolist).filter(filters)
                    .format("IovSetDto").size((long) dtolist.size()).datatype("iovs");
            return Response.ok().entity(saveddto).build();

        }
        catch (final CdbServiceException e) {
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
            Long size = 0L;
            if (snapshot != 0L) {
                Date snap = null;
                snap = new Date(snapshot);
                size = iovService.getSizeByTagAndSnapshot(tagname, snap);
            }
            else {
                size = iovService.getSizeByTag(tagname);
            }
            final CrestBaseResponse respdto = new CrestBaseResponse();
            final GenericMap filters = new GenericMap();
            filters.put("tagName", tagname);
            filters.put("snapshot", snapshot.toString());
            respdto.size(size).datatype("count").filter(filters);
            respdto.format("IovSetDto");
            return Response.ok().entity(respdto).build();

        }
        catch (final Exception e) {
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
            final List<TagSummaryDto> entitylist = iovService.getTagSummaryInfo(tagname);
            final TagSummarySetDto respdto = new TagSummarySetDto();
            final GenericMap filters = new GenericMap();
            filters.put("tagName", tagname);
            if (entitylist == null) {
                final String message = "No resource has been found";
                final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.INFO,
                        message);
                return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
            }
            ((TagSummarySetDto) respdto.size((long) entitylist.size()).datatype("count")
                    .filter(filters)).resources(entitylist);

            return Response.ok().entity(respdto).build();

        }
        catch (final Exception e) {
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
        this.log.info("IovRestController processing request for iovs groups using tag name {}",
                tagname);
        try {
            // Search for tag in order to load the time type:
            final TagDto tagentity = tagService.findOne(tagname);
            if (tagentity == null) {
                throw new CdbServiceException("Cannot find tag for name " + tagname);
            }
            final ResponseBuilder builder = cachesvc.verifyLastModified(request, tagentity);
            if (builder != null) {
                // Get request headers: this is just to dump the If-Modified-Since
                final String ifmodsince = headers.getHeaderString("If-Modified-Since");
                log.debug("The output data are not modified since " + ifmodsince);
                return builder.build();
            }
            Long groupsize = null;
            final String timetype = tagentity.getTimeType();
            if (timetype.equalsIgnoreCase("run")) {
                groupsize = new Long(cprops.getRuntypeGroupsize());
            }
            else if (timetype.equalsIgnoreCase("run-lumi")) {
                groupsize = new Long(cprops.getRuntypeGroupsize());
                groupsize = groupsize * 4294967296L; // transform to COOL run-lumi
            }
            else {
                // Assume COOL time format...
                groupsize = new Long(cprops.getTimetypeGroupsize());
                groupsize = groupsize * 1000000000L; // transform to COOL nanosec
            }
            // Set caching policy depending on snapshot argument
            // this is filling a mag-age parameter in the header
            final CacheControl cc = cachesvc.getGroupsCacheControl(snapshot);
            // Retrieve all iovs groups
            Date snap = null;
            if (snapshot != 0L) {
                snap = new Date(snapshot);
            }
            final CrestBaseResponse respdto = iovService
                    .selectGroupDtoByTagNameAndSnapshotTime(tagname, snap, groupsize);
            final GenericMap filters = new GenericMap();
            filters.put("tagName", tagname);
            filters.put("snapshot", snapshot.toString());
            filters.put("groupsize", groupsize.toString());
            respdto.datatype("groups").filter(filters);
            respdto.format("IovSetDto");
            return Response.ok().entity(respdto).cacheControl(cc).build();

        }
        catch (final Exception e) {
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
            List<IovDto> dtolist = null;
            // Retrieve all iovs
            final TagDto tagentity = tagService.findOne(tagname);
            if (tagentity == null) {
                throw new CdbServiceException("Cannot find tag for name " + tagname);
            }
            log.debug("Found tag " + tagentity);
            final ResponseBuilder builder = cachesvc.verifyLastModified(request, tagentity);
            if (builder != null) {
                // Get request headers: this is just to dump the If-Modified-Since
                final String ifmodsince = headers.getHeaderString("If-Modified-Since");
                log.debug("The output data are not modified since " + ifmodsince);
                return builder.build();
            }

            log.debug("Setting iov range to : {}, {}", since, until);
            BigDecimal runtil = null;
            if (until.equals("INF")) {
                log.debug("The end time will be set to : {}", CrestProperties.INFINITY);
                runtil = CrestProperties.INFINITY;
            }
            else {
                runtil = new BigDecimal(until);
                log.debug("The end time will be set to : " + runtil);
            }
            final BigDecimal rsince = new BigDecimal(since);
            Date snap = null;
            if (snapshot != 0L) {
                snap = new Date(snapshot);
            }
            // Set caching policy depending on snapshot argument
            // this is filling a mag-age parameter in the header
            final CacheControl cc = cachesvc.getIovsCacheControlForUntil(snapshot, runtil);
            // Retrieve all iovs
            if (xCrestQuery == null) {
                xCrestQuery = "groups";
            }
            dtolist = iovService.selectIovsByTagRangeSnapshot(tagname, rsince, runtil, snap,
                    xCrestQuery);
            final IovSetDto respdto = new IovSetDto();
            ((IovSetDto) respdto.datatype("iovs")).resources(dtolist).size((long) dtolist.size());
            final GenericMap filters = new GenericMap();
            filters.put("tagName", tagname);
            filters.put("snapshot", snapshot.toString());
            filters.put("since", rsince.toString());
            filters.put("until", runtil.toString());
            respdto.filter(filters);
            respdto.format("IovSetDto");
            return Response.ok().entity(respdto).cacheControl(cc).build();

        }
        catch (final Exception e) {
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
                snap = new Date(snapshot);
            }
            final List<IovDto> entitylist = iovService.selectSnapshotByTag(tagname, snap);
            final IovSetDto respdto = new IovSetDto();
            respdto.resources(entitylist).size((long) entitylist.size());
            final GenericMap filters = new GenericMap();
            filters.put("tagName", tagname);
            filters.put("snapshot", snapshot.toString());
            respdto.datatype("iovs").filter(filters);
            respdto.format("IovSetDto");
            return Response.ok().entity(respdto).build();

        }
        catch (final Exception e) {
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
            log.debug("Search resource list using tagname={} and before since={}", tagname, since);

            if (dateformat == null) {
                dateformat = "ms";
            }

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
            final List<IovDto> dtolist = new ArrayList<>();
            dtolist.add(last);
            final CrestBaseResponse saveddto = new IovSetDto().resources(dtolist).filter(filters)
                    .size(1L).datatype("iovs");
            saveddto.format("IovSetDto");
            return Response.ok().entity(saveddto).build();

        }
        catch (final CdbServiceException e) {
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
                "IovRestController processing request for iovs and payload using tag name {} and range {} - {} ",
                tagname, since, until);
        try {
            List<IovPayloadDto> dtolist = null;
            // Retrieve all iovs
            final TagDto tagentity = tagService.findOne(tagname);
            if (tagentity == null) {
                throw new CdbServiceException("Cannot find tag for name " + tagname);
            }
            log.debug("Found tag " + tagentity);

            log.debug("Setting iov range to : {}, {}", since, until);
            BigDecimal runtil = null;
            if (until.equals("INF")) {
                log.debug("The end time will be set to : {}", CrestProperties.INFINITY);
                runtil = CrestProperties.INFINITY;
            }
            else {
                runtil = new BigDecimal(until);
                log.debug("The end time will be set to : " + runtil);
            }
            final BigDecimal rsince = new BigDecimal(since);
            Date snap = new Date();
            if (snapshot != 0L) {
                snap = new Date(snapshot);
            }

            dtolist = iovService.selectIovPayloadsByTagRangeSnapshot(tagname, rsince, runtil, snap);
            final IovPayloadSetDto respdto = new IovPayloadSetDto();
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
        catch (final Exception e) {
            final String msg = "Error in selectIovPayloads : " + tagname + ", " + snapshot;
            log.error("Exception catched by REST controller for {} : {}", msg, e.getMessage());
            final String message = msg + " -- " + e.getMessage();
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,
                    message);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
        }
    }

}
