package hep.crest.server.swagger.impl;

import hep.crest.server.annotations.ProfileAndLog;
import hep.crest.server.caching.CachingPolicyService;
import hep.crest.server.caching.CachingProperties;
import hep.crest.server.config.CrestProperties;
import hep.crest.server.controllers.EntityDtoHelper;
import hep.crest.server.controllers.PageRequestHelper;
import hep.crest.server.converters.IovMapper;
import hep.crest.server.data.pojo.Iov;
import hep.crest.server.data.pojo.Tag;
import hep.crest.server.data.repositories.args.IovModeEnum;
import hep.crest.server.data.repositories.args.IovQueryArgs;
import hep.crest.server.exceptions.CdbBadRequestException;
import hep.crest.server.serializers.ArgTimeUnit;
import hep.crest.server.services.IovService;
import hep.crest.server.services.TagService;
import hep.crest.server.swagger.api.IovsApiService;
import hep.crest.server.swagger.api.NotFoundException;
import hep.crest.server.swagger.model.CrestBaseResponse;
import hep.crest.server.swagger.model.GenericMap;
import hep.crest.server.swagger.model.IovDto;
import hep.crest.server.swagger.model.IovPayloadDto;
import hep.crest.server.swagger.model.IovPayloadSetDto;
import hep.crest.server.swagger.model.IovSetDto;
import hep.crest.server.swagger.model.RespPage;
import hep.crest.server.swagger.model.TagSummaryDto;
import hep.crest.server.swagger.model.TagSummarySetDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.SecurityContext;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Rest endpoint for iov management. It allows to create and find iovs.
 *
 * @author formica
 */
@Component
@Slf4j
public class IovsApiServiceImpl extends IovsApiService {
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
    private IovMapper mapper;

    /**
     * The context from the request.
     */
    @Autowired
    private JAXRSContext context;

    /*
     * (non-Javadoc)
     *
     * @see
     * hep.crest.server.swagger.api.IovsApiService#createIov(hep.crest.swagger.model
     * .IovDto, jakarta.ws.rs.core.SecurityContext, jakarta.ws.rs.core.UriInfo)
     */
    @Override
    public Response storeIovOne(IovDto body, SecurityContext securityContext) {
        log.info("IovRestController processing request for creating an iov");
        // Create a new IOV.
        String tagname = body.getTagName();
        Iov entity = mapper.toEntity(body);
        final Iov saved = iovService.insertIov(entity);
        // Change the modification time in the tag.
        Tag tagEntity = tagService.findOne(tagname);
        tagEntity.setModificationTime(Date.from(Instant.now()));
        tagService.updateTag(tagEntity);
        // Generate response.
        IovDto dto = mapper.toDto(saved);
        dto.tagName(tagname);
        List<IovDto> dtoList = new ArrayList<>();
        dtoList.add(dto);
        CrestBaseResponse resp = buildEntityResponse(dtoList, new GenericMap());
        return Response.created(context.getUriInfo().getRequestUri()).entity(resp).build();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * hep.crest.server.swagger.api.IovsApiService#storeBatchIovMultiForm(hep.crest.
     * swagger.model.IovSetDto, jakarta.ws.rs.core.SecurityContext,
     * jakarta.ws.rs.core.UriInfo)
     */
    @Override
    @ProfileAndLog
    public Response storeIovBatch(IovSetDto dto, SecurityContext securityContext) {
        log.info("Upload iovs batch of size {}",
                dto.getSize());
        // Prepare the iov list to insert and a list representing iovs really inserted.
        final List<IovDto> iovlist = dto.getResources();
        final List<IovDto> savedList = new ArrayList<>();
        if (iovlist == null) {
            throw new BadRequestException("Cannot store null list of iovs");
        }
        // Check if tag exists.
        String tagName = iovlist.get(0).getTagName();
        Tag tagEntity = tagService.findOne(tagName);
        if (tagEntity == null) {
            throw new BadRequestException("Cannot store iovs for non existing tag");
        }
        log.info("Store iovs in tag {}", tagName);
        // Loop over resources uploaded.
        for (final IovDto iovDto : iovlist) {
            log.debug("Create iov from dto {}", iovDto);
            // Create new iov.
            Iov entity = mapper.toEntity(iovDto);
            final Iov saved = iovService.insertIov(entity);
            IovDto saveddto = mapper.toDto(saved);
            saveddto.tagName(tagName);
            // Add to saved list.
            savedList.add(saveddto);
        }
        // Change the modification time in the tag.
        tagEntity.setModificationTime(Date.from(Instant.now()));
        tagService.updateTag(tagEntity);
        // Prepare the Set for the response.
        final CrestBaseResponse saveddto = buildEntityResponse(savedList, new GenericMap());
        // Send 201.
        return Response.created(context.getUriInfo().getRequestUri()).entity(saveddto).build();
    }

    @Override
    @ProfileAndLog
    public Response findAllIovs(String method, String tagname, Long snapshot, String since,
                                String until,
                                String timeformat,
                                Long groupsize,
                                String hash,
                                Integer page, Integer size, String sort,
                                String xCrestQuery, String xCrestSince,
                                SecurityContext securityContext) {
        log.info("Search iovs list using method={}, tag={}, since={}, until={}, timeformat={}, "
                 + "page={}, size={}, "
                 + "sort={}", method,
                tagname,
                since,
                until,
                timeformat,
                page,
                size, sort);
        // Date format. Default is milliseconds.
        if (timeformat == null) {
            timeformat = "MS";
        }
        if (xCrestSince == null) {
            xCrestSince = "NUMBER";
        }
        if (xCrestQuery == null) {
            xCrestQuery = "IOVS";
        }
        if (tagname == null || tagname.contains("%")) {
            throw new CdbBadRequestException("Cannot search iovs with tag " + tagname);
        }
        log.debug("Use input time format: {}", timeformat);
        log.debug("Use iov query mode: {}", xCrestQuery);
        ArgTimeUnit inputformat = ArgTimeUnit.valueOf(timeformat);
        ArgTimeUnit outformat = ArgTimeUnit.valueOf(xCrestSince);
        IovModeEnum queryMode = IovModeEnum.valueOf(xCrestQuery);

        // If it is a group method query, immediately call the method
        if (IovModeEnum.GROUPS.mode().equalsIgnoreCase(method)) {
            return this.selectGroups(tagname, snapshot, groupsize);
        }

        // From now on, it is an iovs or monitor or attime query.
        IovQueryArgs args = new IovQueryArgs();
        Timestamp snap = null;
        if (snapshot != null && snapshot > 0) {
            Instant inst = Instant.ofEpochMilli(snapshot);
            snap = Timestamp.from(inst);
            log.debug("Use snapshot {}", snap);
        }
        // Set the since and until times in query.
        BigInteger rsince = prh.getTimeFromArg(since, inputformat, outformat, null);
        BigInteger runtil = prh.getTimeFromArg(until, inputformat, outformat, null);
        // Set arguments for query.
        if (queryMode.equals(IovModeEnum.AT)) {
            runtil = null;
        }
        args.mode(queryMode).hash(hash).tagName(tagname).snapshot(snap)
                .since(rsince).until(runtil);
        if (args.checkArgsNull(method)) {
            throw new CdbBadRequestException("Arguments not compatible with method " + method);
        }
        // Create filters
        GenericMap filters = new GenericMap();
        filters.put("tagName", tagname);
        if (rsince != null) {
            filters.put("since", rsince.toString());
        }
        if (runtil != null) {
            filters.put("until", runtil.toString());
        }
        filters.put("timeformat", timeformat);
        filters.put("method", method);
        filters.put("mode", xCrestQuery);
        // Create pagination request
        final PageRequest preq = prh.createPageRequest(page, size, sort);

        // Search for global tags using where conditions.
        Page<Iov> entitypage = iovService.selectIovList(args, preq);
        RespPage respPage = new RespPage().size(entitypage.getSize())
                .totalElements(entitypage.getTotalElements()).totalPages(entitypage.getTotalPages())
                .number(entitypage.getNumber());

        final List<IovDto> dtolist = edh.entityToDtoList(entitypage.toList(), IovDto.class,
                IovMapper.class);
        Response.Status rstatus = Response.Status.OK;
        final CacheControl cc = cachesvc.getIovsCacheControlForUntil(0L, CrestProperties.INFINITY);
        // Prepare the response.
        final CrestBaseResponse saveddto = buildEntityResponse(dtolist, filters);
        saveddto.page(respPage);
        // Send a response and status 200.
        return Response.status(rstatus).entity(saveddto).cacheControl(cc).build();
    }


    /*
     * (non-Javadoc)
     *
     * @see
     * hep.crest.server.swagger.api.IovsApiService#getSizeByTag(java.lang.String,
     * jakarta.ws.rs.core.SecurityContext, jakarta.ws.rs.core.UriInfo)
     */
    @Override
    public Response getSizeByTag(@NotNull String tagname, SecurityContext securityContext) {
        log.info("Get size of iovs for tag {}", tagname);
        // Check if the tagname is a pattern or a real tag.
        if (!tagname.contains("%")) {
            tagService.findOne(tagname);
        }
        // The tag string is a pattern or an existing tag.
        // Get the tag summary list corresponding to the tagname pattern.
        // The method in the service sends back always a list, eventually empty.
        final List<TagSummaryDto> entitylist = iovService.getTagSummaryInfo(tagname);
        final TagSummarySetDto respdto = new TagSummarySetDto();
        final GenericMap filters = new GenericMap();
        filters.put("tagName", tagname);
        // Prepare the Set.
        ((TagSummarySetDto) respdto.size((long) entitylist.size()).datatype("count")
                .filter(filters)).resources(entitylist)
                .format("TagSummarySetDto");
        // Send a response 200. Even if the result is an empty list.
        return Response.ok().entity(respdto).build();
    }

    /**
     * Custom query to return groups.
     *
     * @param tagname
     * @param snapshot
     * @return
     */
    protected Response selectGroups(String tagname, Long snapshot, Long groupsize) {
        final Tag tagentity = tagService.findOne(tagname);
        HttpHeaders headers = context.getHttpHeaders();
        Request request = context.getRequest();
        // Apply caching on iov groups selections.
        // Use cache service to detect if a tag was modified.
        final ResponseBuilder builder = cachesvc.verifyLastModified(request, tagentity);
        if (builder != null) {
            // Get request headers: this is just to dump the If-Modified-Since
            final String ifmodsince = headers.getHeaderString("If-Modified-Since");
            log.debug("The output data are not modified since " + ifmodsince);
            return builder.build();
        }
        // Get the time type to apply different group selections.
        // This are typical values representative for COOL types (NANO_SEC).
        // The groupsize can be provided in input.
        final String timetype = tagentity.getTimeType();
        if (groupsize == null) {
            groupsize = iovService.getOptimalGroupSize(timetype);
        }
        // Set caching policy depending on snapshot argument
        // this is filling a max-age parameter in the header
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
        if (snapshot != null) {
            filters.put("snapshot", snapshot.toString());
        }
        if (groupsize != null) {
            filters.put("groupsize", groupsize.toString());
        }
        respdto.datatype("groups").format("IovSetDto").filter(filters);
        // In the response set the cachecontrol flag as well.
        return Response.ok().entity(respdto).cacheControl(cc)
                .lastModified(tagentity.getModificationTime()).build();
    }

    @Override
    public Response selectIovPayloads(@NotNull String tagname, String since, String until,
                                      String timeformat,
                                      Integer page, Integer size, String sort,
                                      SecurityContext securityContext)
            throws NotFoundException {

        log.info(
                "Search iovs and payloads metadata using tag {} and range {} - {} ",
                tagname, since, until);
        List<IovPayloadDto> dtolist = null;
        if (timeformat == null) {
            timeformat = "MS";
        }
        log.debug("Use input time format: {}", timeformat.toUpperCase());
        ArgTimeUnit inputformat = ArgTimeUnit.valueOf(timeformat.toUpperCase());
        ArgTimeUnit outformat = ArgTimeUnit.valueOf("COOL");

        // Retrieve all iovs
        final Tag tagentity = tagService.findOne(tagname);
        log.debug("Found tag " + tagentity);
        BigInteger rsince = prh.getTimeFromArg(since, inputformat, outformat, null);
        BigInteger runtil = prh.getTimeFromArg(until, inputformat, outformat, null);
        log.debug("Setting iov range to : {}, {}", since, until);
        if (rsince == null || runtil == null) {
            throw new CdbBadRequestException("Invalid time range");
        }
        Date snap = new Date();
        log.debug("Use snapshot time NOW: {}", snap);
        // Get the IOV list.
        dtolist = iovService.selectIovPayloadsByTagRangeSnapshot(tagname, rsince, runtil, snap);
        final IovPayloadSetDto respdto = new IovPayloadSetDto();
        // Create the Set for the response.
        ((IovPayloadSetDto) respdto.datatype("iovpayloads")).resources(dtolist)
                .size((long) dtolist.size());
        final GenericMap filters = new GenericMap();
        if (tagname != null) {
            filters.put("tagName", tagname);
        }
        if (rsince != null) {
            filters.put("since", rsince.toString());
        }
        if (runtil != null) {
            filters.put("until", runtil.toString());
        }
        respdto.filter(filters);
        return Response.ok().entity(respdto).build();
    }

    /**
     * Factorise code to build the IovSetDto.
     *
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
