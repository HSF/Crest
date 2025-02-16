package hep.crest.server.swagger.impl;

import hep.crest.server.annotations.ProfileAndLog;
import hep.crest.server.caching.CachingPolicyService;
import hep.crest.server.controllers.EntityDtoHelper;
import hep.crest.server.controllers.PageRequestHelper;
import hep.crest.server.converters.TagMapper;
import hep.crest.server.converters.TagMetaMapper;
import hep.crest.server.data.pojo.Tag;
import hep.crest.server.data.pojo.TagMeta;
import hep.crest.server.data.repositories.args.TagQueryArgs;
import hep.crest.server.exceptions.CdbBadRequestException;
import hep.crest.server.services.TagMetaService;
import hep.crest.server.services.TagService;
import hep.crest.server.swagger.api.NotFoundException;
import hep.crest.server.swagger.api.TagsApiService;
import hep.crest.server.swagger.model.CrestBaseResponse;
import hep.crest.server.swagger.model.GenericMap;
import hep.crest.server.swagger.model.RespPage;
import hep.crest.server.swagger.model.TagDto;
import hep.crest.server.swagger.model.TagMetaDto;
import hep.crest.server.swagger.model.TagMetaSetDto;
import hep.crest.server.swagger.model.TagSetDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Rest endpoint for tag management.
 *
 * @author formica
 */
@Component
@Slf4j
public class TagsApiServiceImpl extends TagsApiService {
    /**
     * Resource bundle.
     */
    private final ResourceBundle bundle = ResourceBundle.getBundle("messages", new Locale("US"));
    /**
     * Helper.
     */
    EntityDtoHelper edh;
    /**
     * Helper.
     */
    private PageRequestHelper prh;
    /**
     * Service.
     */
    private CachingPolicyService cachesvc;
    /**
     * Service.
     */
    private TagService tagService;
    /**
     * Service.
     */
    private TagMetaService tagMetaService;
    /**
     * Mapper.
     */
    private TagMapper tagmapper;
    /**
     * Mapper.
     */
    private TagMetaMapper tagmetamapper;

    /**
     * Context
     */
    private JAXRSContext context;

    /**
     * Ctor with injected service.
     * @param tagService the service.
     * @param tagmapper the mapper.
     * @param tagmetamapper the meta mapper.
     * @param edh the entity dto helper.
     * @param cachesvc the caching service.
     * @param context the context.
     */
    @Autowired
    public TagsApiServiceImpl(TagService tagService, TagMapper tagmapper,
                              TagMetaMapper tagmetamapper, EntityDtoHelper edh,
                              CachingPolicyService cachesvc,
                              JAXRSContext context) {
        this.tagService = tagService;
        this.tagMetaService = tagService.getTagMetaService();
        this.tagmapper = tagmapper;
        this.tagmetamapper = tagmetamapper;
        this.prh = tagService.getPrh();
        this.edh = edh;
        this.cachesvc = cachesvc;
        this.context = context;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * hep.crest.server.swagger.api.TagsApiService#createTag(hep.crest.swagger.model
     * .TagDto, jakarta.ws.rs.core.SecurityContext, jakarta.ws.rs.core.UriInfo)
     */
    @Override
    public Response createTag(TagDto body, SecurityContext securityContext) {
        log.info("Creating a new tag {}", body.getName());
        // Create a tag.
        Tag entity = tagmapper.toEntity(body);
        final Tag saved = tagService.insertTag(entity);
        TagDto dto = tagmapper.toDto(saved);
        // Response is 201.
        log.info("Created tag {}", dto);
        return Response.created(context.getUriInfo().getRequestUri()).entity(dto).build();
    }

    /*
     * (non-Javadoc)
     *
     * @see hep.crest.server.swagger.api.TagsApiService#updateTag(java.lang.String,
     * hep.crest.swagger.model.GenericMap, jakarta.ws.rs.core.SecurityContext,
     * jakarta.ws.rs.core.UriInfo)
     */
    @Override
    public Response updateTag(String name,
                              GenericMap body,
                              SecurityContext securityContext) {
        log.info("Updating tag {}", name);
        // Search tag.
        final Tag entity = tagService.findOne(name);
        // Send a bad request if body is null.
        if (body == null) {
            throw new CdbBadRequestException("Cannot update tag with null body");
        }
        // Loop over map body keys.
        for (final String key : body.keySet()) {
            if ("description".equals(key)) {
                // Update description.
                entity.setDescription(body.get(key));
            }
            else if (Objects.equals(key, "timeType")) {
                entity.setTimeType(body.get(key));
            }
            else if (Objects.equals(key, "lastValidatedTime")) {
                final BigInteger val = new BigInteger(body.get(key));
                entity.setLastValidatedTime(val);
            }
            else if (Objects.equals(key, "endOfValidity")) {
                final BigInteger val = new BigInteger(body.get(key));
                entity.setEndOfValidity(val);
            }
            else if (Objects.equals(key, "synchronization")) {
                entity.setSynchronization(body.get(key));
            }
            else if (Objects.equals(key, "payloadSpec")) {
                entity.setObjectType(body.get(key));
            }
            else {
                log.warn("Ignored key {} in updateTag: field does not exists", key);
            }
        }
        final Tag saved = tagService.updateTag(entity);
        TagDto dto = tagmapper.toDto(saved);
        log.info("Updated tag {}", dto);
        return Response.ok(context.getUriInfo().getRequestUri()).entity(dto).build();
    }

    /*
     * (non-Javadoc)
     *
     * @see hep.crest.server.swagger.api.TagsApiService#findTag(java.lang.String,
     * jakarta.ws.rs.core.SecurityContext, jakarta.ws.rs.core.UriInfo)
     */
    @Override
    public Response findTag(String name, SecurityContext securityContext) {
        log.debug("Get tag {} ", name);
        final GenericMap filters = new GenericMap();
        filters.put("name", name);
        final Tag entity = tagService.findOne(name);
        TagDto dto = tagmapper.toDto(entity);
        // Response page
        RespPage respPage = new RespPage().size(1)
                .totalElements(1L).totalPages(1)
                .number(0);
        // Create the set.
        final TagSetDto respdto = (TagSetDto) new TagSetDto().addresourcesItem(dto).size(1L)
                .filter(filters).page(respPage).datatype("tags").format("TagSetDto");
        log.info("Retrieved tag {}: {}", name, dto);
        final CacheControl cc = cachesvc.getDefaultsCacheControl();
        cc.setMaxAge(3600); // 1 hour caching for this resource.
        return Response.ok().entity(respdto).cacheControl(cc).build();
    }

    /*
     * (non-Javadoc)
     *
     * @see hep.crest.server.swagger.api.TagsApiService#listTags(java.lang.String,
     * java.lang.Integer, java.lang.Integer, java.lang.String,
     * jakarta.ws.rs.core.SecurityContext, jakarta.ws.rs.core.UriInfo)
     */
    @Override
    @ProfileAndLog
    public Response listTags(String name, String timeType, String objectType,
                             String description, Integer page,
                             Integer size, String sort,
                             SecurityContext securityContext) {
        log.info("Search tag list using name={}, timeType={}, objectType={}, descrition={}, "
                 + "page={}, "
                 + "size={}, "
                 + "sort={}", name, timeType, objectType, description, page, size, sort);
        if (name.equalsIgnoreCase("all")) {
            name = "%";
        }
        // Create query params object
        TagQueryArgs args = new TagQueryArgs();
        args.name(name).description(description).objectType(objectType).timeType(timeType);
        // Create pagination request
        final PageRequest preq = prh.createPageRequest(page, size, sort);
        // Launch query
        // Search for global tags using where conditions.
        final Page<Tag> entitypage = tagService.selectTagList(args, preq);
        RespPage respPage = new RespPage().size(entitypage.getSize())
                .totalElements(entitypage.getTotalElements()).totalPages(entitypage.getTotalPages())
                .number(entitypage.getNumber());
        List<TagDto> dtolist = edh.entityToDtoList(entitypage.toList(),
                TagDto.class, TagMapper.class);
        // Create the Set.
        final CrestBaseResponse setdto = new TagSetDto().resources(dtolist)
                .page(respPage)
                .size((long) dtolist.size())
                .datatype("tags")
                .format("TagSetDto");
        // Create filters
        GenericMap filters = new GenericMap();
        filters.put("name", name);
        if (objectType != null) {
            filters.put("objectType", objectType);
        }
        if (description != null) {
            filters.put("description", description);
        }
        if (timeType != null) {
            filters.put("timeType", timeType);
        }
        setdto.filter(filters);
        // Response is 200.
        log.info("Retrieved tag list from filters {} size={} total={}",
                filters, setdto.getSize(), respPage.getTotalElements());
        return Response.ok().entity(setdto).build();
    }

    /* (non-Javadoc)
     * @see hep.crest.server.swagger.api.TagsApiService#createTagMeta(java.lang.String, hep.crest.swagger.model
     * .TagMetaDto, jakarta.ws.rs.core.SecurityContext, jakarta.ws.rs.core.UriInfo)
     */
    @Override
    public Response createTagMeta(String name, TagMetaDto body, SecurityContext securityContext) {
        log.debug("TagRestController processing request for creating a tag meta data entry for {}",
                name);
        final Tag tag = tagService.findOne(name);
        log.debug("Add meta information to tag {}", name);
        TagMeta entity = tagmetamapper.toEntity(body);

        final TagMeta savedEntity = tagMetaService.insertTagMeta(entity);
        TagMetaDto saved = tagmetamapper.toDto(savedEntity);
        log.info("Created tag meta data {}", saved);
        return Response.created(context.getUriInfo().getRequestUri()).entity(saved).build();
    }

    /* (non-Javadoc)
     * @see hep.crest.server.swagger.api.TagsApiService#findTagMeta(java.lang.String, jakarta.ws.rs.core
     * .SecurityContext, jakarta.ws.rs.core.UriInfo)
     */
    @Override
    @ProfileAndLog
    public Response findTagMeta(String name, SecurityContext securityContext) throws NotFoundException {
        log.info("Search tag metadata for name " + name);
        final TagMeta entity = tagMetaService.find(name);
        final TagMetaDto dto = tagmetamapper.toDto(entity);
        RespPage respPage = new RespPage().size(1)
                .totalElements(1L).totalPages(1)
                .number(0);
        // Create filters
        GenericMap filters = new GenericMap();
        if (name != null) {
            filters.put("name", name);
        }
        final TagMetaSetDto respdto = (TagMetaSetDto) new TagMetaSetDto()
                .addresourcesItem(dto)
                .page(respPage)
                .size(1L)
                .filter(filters)
                .datatype("tagmetas")
                .format("TagMetaSetDto");
        log.info("Retrieved tag meta data {}: {}", name, dto);

        final CacheControl cc = cachesvc.getDefaultsCacheControl();
        cc.setMaxAge(3600); // 1 hour caching for this resource.
        return Response.ok().entity(respdto).cacheControl(cc).build();
    }


    /* (non-Javadoc)
     * @see hep.crest.server.swagger.api.TagsApiService#updateTagMeta(java.lang.String, hep.crest.swagger.model
     * .GenericMap, jakarta.ws.rs.core.SecurityContext, jakarta.ws.rs.core.UriInfo)
     */
    @Override
    public Response updateTagMeta(String name, GenericMap body,
                                  SecurityContext securityContext)
            throws NotFoundException {
        log.info("TagRestController processing request for updating a tag meta information for "
                 + "name {}", name);
        TagMeta entity = tagMetaService.find(name);
        for (final String key : body.keySet()) {
            if (Objects.equals(key, "description")) {
                entity.setDescription(body.get(key));
            }
            if (Objects.equals(key, "chansize")) {
                entity.setChansize(Integer.valueOf(body.get(key)));
            }
            if (Objects.equals(key, "colsize")) {
                entity.setColsize(Integer.valueOf(body.get(key)));
            }
            if (Objects.equals(key, "tagInfo")) {
                // The field is a string ... this is mandatory for the moment....
                entity.setTagInfo(body.get(key).getBytes(StandardCharsets.UTF_8));
            }
        }
        final TagMeta saved = tagMetaService.updateTagMeta(entity);
        final TagMetaDto dto = tagmetamapper.toDto(saved);
        log.info("Updated tag meta data {}", dto);
        return Response.ok(context.getUriInfo().getRequestUri()).entity(dto).build();
    }
}
