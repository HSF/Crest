package hep.crest.server.swagger.impl;

import hep.crest.server.controllers.EntityDtoHelper;
import hep.crest.server.controllers.PageRequestHelper;
import hep.crest.server.converters.GlobalTagMapper;
import hep.crest.server.converters.TagMapper;
import hep.crest.server.data.pojo.GlobalTag;
import hep.crest.server.data.pojo.Tag;
import hep.crest.server.data.repositories.args.GtagQueryArgs;
import hep.crest.server.services.GlobalTagService;
import hep.crest.server.swagger.api.GlobaltagsApiService;
import hep.crest.server.swagger.api.NotFoundException;
import hep.crest.server.swagger.model.CrestBaseResponse;
import hep.crest.server.swagger.model.GenericMap;
import hep.crest.server.swagger.model.GlobalTagDto;
import hep.crest.server.swagger.model.GlobalTagSetDto;
import hep.crest.server.swagger.model.RespPage;
import hep.crest.server.swagger.model.TagDto;
import hep.crest.server.swagger.model.TagSetDto;
import jakarta.ws.rs.core.CacheControl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.math.BigDecimal;
import java.util.List;

/**
 * Rest endpoint to manage global tags. It is used for creation or search of
 * global tags.
 *
 * @author formica
 */
@Component
@Slf4j
public class GlobaltagsApiServiceImpl extends GlobaltagsApiService {
    /**
     * Helper.
     */
    private PageRequestHelper prh;

    /**
     * Helper.
     */
    private EntityDtoHelper edh;

    /**
     * Service.
     */
    private GlobalTagService globaltagService;

    /**
     * Mapper.
     */
    private GlobalTagMapper mapper;

    /**
     * Context
     */
    private JAXRSContext context;

    /**
     * Ctor with injected service.
     *
     * @param globaltagService the global tag service.
     * @param prh              the PageRequestHelper
     * @param edh              the EntityDtoHelper
     * @param mapper           the global tag mapper.
     * @param context          the context.
     */
    @Autowired
    public GlobaltagsApiServiceImpl(GlobalTagService globaltagService, PageRequestHelper prh,
                                    EntityDtoHelper edh, GlobalTagMapper mapper,
                                    JAXRSContext context) {
        this.globaltagService = globaltagService;
        this.prh = prh;
        this.edh = edh;
        this.mapper = mapper;
        this.context = context;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * hep.crest.server.swagger.api.GlobaltagsApiService#createGlobalTag(hep.crest.
     * swagger.model.GlobalTagDto, java.lang.String,
     * jakarta.ws.rs.core.SecurityContext, jakarta.ws.rs.core.UriInfo)
     */
    @Override
    public Response createGlobalTag(String force, GlobalTagDto body,
                                    SecurityContext securityContext) {
        log.info("Create a global tag {}", body.getName());
        // If the force mode is active, the insertion time is imposed by the client.
        if (force.equals("false")) {
            // Set to null so that is automatically generated.
            body.setInsertionTime(null);
        }
        // Insert a new global tag.
        final GlobalTag entity = mapper.toEntity(body);
        final GlobalTag saved = globaltagService.insertGlobalTag(entity);
        final GlobalTagDto dto = mapper.toDto(saved);
        // Send the created status.
        return Response.created(context.getUriInfo().getRequestUri()).entity(dto).build();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * hep.crest.server.swagger.api.GlobaltagsApiService#findGlobalTag(java.lang.
     * String, jakarta.ws.rs.core.SecurityContext, jakarta.ws.rs.core.UriInfo)
     */
    @Override
    public Response findGlobalTag(String name, SecurityContext securityContext) {
        log.info("GlobalTagRestController processing request for global tag name " + name);
        // Prepare filters.
        final GenericMap filters = new GenericMap();
        if (name != null) {
            filters.put("name", name);
        }
        // Search for a global tag resource.
        final GlobalTag entity = globaltagService.findOne(name);
        final GlobalTagDto dto = mapper.toDto(entity);
        RespPage respPage = new RespPage().size(1)
                .totalElements(1L).totalPages(1).number(0);
        log.debug("Found GlobalTag " + name);
        // Prepare response set.
        final CrestBaseResponse setdto = new GlobalTagSetDto().addresourcesItem(dto)
                .filter(filters).size(1L)
                .page(respPage)
                .datatype("globaltags")
                .format("GlobalTagSetDto");
        // Set cache control to 5 minutes.
        CacheControl cc = new CacheControl();
        cc.setMaxAge(600);
        return Response.ok().entity(setdto).cacheControl(cc).build();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * hep.crest.server.swagger.api.GlobaltagsApiService#findGlobalTagFetchTags(java
     * .lang.String, java.lang.String, java.lang.String,
     * jakarta.ws.rs.core.SecurityContext, jakarta.ws.rs.core.UriInfo)
     */
    @Override
    public Response findGlobalTagFetchTags(String name, String record, String label,
                                           SecurityContext securityContext) {
        log.info("Get tags for globaltag {} ", name);
        // Prepare filters.
        final GenericMap filters = new GenericMap();
        if (name != null) {
            filters.put("name", name);
        }
        if (record != null) {
            filters.put("record", record);
        }
        if (label != null) {
            filters.put("label", label);
        }
        // Search for a global tag and associated tags. Use record and label.
        // Presets for record and label is "none".
        // Fetch tags via record and label.
        final List<Tag> entitylist = globaltagService.getGlobalTagByNameFetchTags(name, record,
                label);
        final List<TagDto> dtolist = edh.entityToDtoList(entitylist, TagDto.class, TagMapper.class);
        final long listsize = dtolist == null ? 0L : dtolist.size();
        log.debug("Found list of {} tags for globaltag {}", listsize, name);
        RespPage respPage = new RespPage().size((int)listsize)
                .totalElements(listsize).totalPages(1).number(0);
        final CrestBaseResponse setdto = new TagSetDto().resources(dtolist)
                .format("TagSetDto")
                .filter(filters).size(listsize)
                .page(respPage)
                .datatype("tags");
        return Response.ok().entity(setdto).build();
    }

    @Override
    public Response listGlobalTags(String name, String workflow, String scenario, String release,
                                   Long validity,
                                   String description,
                                   Integer page, Integer size, String sort,
                                   SecurityContext securityContext)
            throws NotFoundException {
        log.info("Search global tag list using name={}, page={}, size={}, sort={}", name, page,
                size,
                sort);
        if (name.equalsIgnoreCase("all")) {
            name = "%";
        }
        // Create query params object
        BigDecimal vd = (validity != null) ? BigDecimal.valueOf(validity) : null;
        GtagQueryArgs args = new GtagQueryArgs();
        args.name(name).validity(vd)
                .release(release).workflow(workflow).description(description).scenario(scenario);
        // Create pagination request
        final PageRequest preq = prh.createPageRequest(page, size, sort);
        // Launch query
        // Search for global tags using where conditions.
        final Page<GlobalTag> entitypage = globaltagService.selectGlobalTagList(args, preq);
        log.debug("Retrieved pages of global tags: {}", entitypage.getTotalPages());
        RespPage respPage = new RespPage().size(entitypage.getSize())
                .totalElements(entitypage.getTotalElements()).totalPages(entitypage.getTotalPages())
                .number(entitypage.getNumber());

        log.debug("Transform to dto global tags");
        final List<GlobalTagDto> dtolist = edh.entityToDtoList(entitypage.toList(),
                GlobalTagDto.class, GlobalTagMapper.class);
        final Response.Status rstatus = Response.Status.OK;
        log.debug("Prepare response dto set global tags : {}", dtolist.size());
        final CrestBaseResponse setdto = new GlobalTagSetDto().resources(dtolist)
                .size((long) dtolist.size())
                .page(respPage)
                .datatype("globaltags")
                .format("GlobalTagSetDto");
        // Create filters
        GenericMap filters = new GenericMap();
        if (name != null) {
            filters.put("name", name);
        }
        if (workflow != null) {
            filters.put("workflow", workflow);
        }
        if (scenario != null) {
            filters.put("scenario", scenario);
        }
        if (release != null) {
            filters.put("release", release);
        }
        if (validity != null) {
            filters.put("validity", validity.toString());
        }
        setdto.filter(filters);
        return Response.status(rstatus).entity(setdto).build();
    }
}
