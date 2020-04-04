package hep.crest.server.swagger.api.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import hep.crest.data.pojo.Tag;
import hep.crest.server.controllers.EntityDtoHelper;
import hep.crest.swagger.model.*;
import ma.glasnost.orika.MapperFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import com.querydsl.core.types.dsl.BooleanExpression;

import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.repositories.querydsl.IFilteringCriteria;
import hep.crest.data.repositories.querydsl.SearchCriteria;
import hep.crest.server.controllers.PageRequestHelper;
import hep.crest.server.exceptions.AlreadyExistsPojoException;
import hep.crest.server.exceptions.NotExistsPojoException;
import hep.crest.server.services.TagService;
import hep.crest.server.swagger.api.ApiResponseMessage;
import hep.crest.server.swagger.api.NotFoundException;
import hep.crest.server.swagger.api.TagsApiService;

/**
 * Rest endpoint for tag management.
 *
 * @author formica
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2017-09-05T16:23:23.401+02:00")
@Component
public class TagsApiServiceImpl extends TagsApiService {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(TagsApiServiceImpl.class);

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
    @Qualifier("tagFiltering")
    private IFilteringCriteria filtering;

    /**
     * Service.
     */
    @Autowired
    private TagService tagService;
    /**
     * Mapper.
     */
    @Autowired
    @Qualifier("mapper")
    private MapperFacade mapper;
    /**
     * Resource bundle.
     */
    private final ResourceBundle bundle = ResourceBundle.getBundle("messages", new Locale("US"));

    /*
     * (non-Javadoc)
     *
     * @see
     * hep.crest.server.swagger.api.TagsApiService#createTag(hep.crest.swagger.model
     * .TagDto, javax.ws.rs.core.SecurityContext, javax.ws.rs.core.UriInfo)
     */
    @Override
    public Response createTag(TagDto body, SecurityContext securityContext, UriInfo info)
            throws NotFoundException {
        log.info("TagRestController processing request for creating a tag");
        try {
            // Create a tag.
            Tag entity = mapper.map(body, Tag.class);
            final Tag saved = tagService.insertTag(entity);
            TagDto dto = mapper.map(saved, TagDto.class);
            // Response is 201.
            return Response.created(info.getRequestUri()).entity(dto).build();
        }
        catch (final AlreadyExistsPojoException e) {
            // Exception, resource exists, send 303.
            log.error("Cannot create tag {}, name already exists...", body);
            return Response.status(Response.Status.SEE_OTHER).entity(body).build();
        }
        catch (final RuntimeException e) {
            // Exception, send 500.
            final String message = e.getMessage();
            log.error("Api method createTag got exception {}", message);
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,
                    message);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see hep.crest.server.swagger.api.TagsApiService#updateTag(java.lang.String,
     * hep.crest.swagger.model.GenericMap, javax.ws.rs.core.SecurityContext,
     * javax.ws.rs.core.UriInfo)
     */
    @Override
    public Response updateTag(String name, GenericMap body, SecurityContext securityContext,
                              UriInfo info) throws NotFoundException {
        log.info("TagRestController processing request for creating a tag");
        try {
            // Search tag.
            final Tag entity = tagService.findOne(name);
            // Loop over map body keys.
            for (final String key : body.keySet()) {
                if ("description".equals(key)) {
                    // Update description.
                    entity.setDescription(body.get(key));
                }
                if (key == "timeType") {
                    entity.setTimeType(body.get(key));
                }
                if (key == "lastValidatedTime") {
                    final BigDecimal val = new BigDecimal(body.get(key));
                    entity.setLastValidatedTime(val);
                }
                if (key == "endOfValidity") {
                    final BigDecimal val = new BigDecimal(body.get(key));
                    entity.setEndOfValidity(val);
                }
                if (key == "synchronization") {
                    entity.setSynchronization(body.get(key));
                }
                if (key == "payloadSpec") {
                    entity.setObjectType(body.get(key));
                }
            }
            final Tag saved = tagService.updateTag(entity);
            TagDto dto = mapper.map(saved, TagDto.class);
            return Response.ok(info.getRequestUri()).entity(dto).build();

        }
        catch (final NotExistsPojoException e) {
            // Exception, tag not found, send 404.
            final String message = "No tag resource has been found for " + name;
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.INFO,
                    message);
            return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see hep.crest.server.swagger.api.TagsApiService#findTag(java.lang.String,
     * javax.ws.rs.core.SecurityContext, javax.ws.rs.core.UriInfo)
     */
    @Override
    public Response findTag(String name, SecurityContext securityContext, UriInfo info)
            throws NotFoundException {
        log.info("TagRestController processing request for tag name " + name);
        final GenericMap filters = new GenericMap();
        filters.put("name", name);
        try {
            final Tag entity = tagService.findOne(name);
            TagDto dto = mapper.map(entity, TagDto.class);
            // Create the set.
            final TagSetDto respdto = (TagSetDto) new TagSetDto().addResourcesItem(dto).size(1L)
                    .filter(filters).datatype("tags");
            return Response.ok().entity(respdto).build();
        }
        catch (final NotExistsPojoException e) {
            // Not found. Send a 404.
            log.warn("Api method findGlobalTag cannot find resource : {}", name);
            final CrestBaseResponse resp = new TagSetDto()
                    .format("TagSetDto").filter(filters).size(0L).datatype("tags");
            return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see hep.crest.server.swagger.api.TagsApiService#listTags(java.lang.String,
     * java.lang.Integer, java.lang.Integer, java.lang.String,
     * javax.ws.rs.core.SecurityContext, javax.ws.rs.core.UriInfo)
     */
    @Override
    public Response listTags(String by, Integer page, Integer size, String sort,
                             SecurityContext securityContext, UriInfo info) throws NotFoundException {
        try {
            log.debug("Search resource list using by={}, page={}, size={}, sort={}", by, page, size,
                    sort);
            // Create filters
            GenericMap filters = prh.getFilters(prh.createMatcherCriteria(by));
            // Create pagination request.
            final PageRequest preq = prh.createPageRequest(page, size, sort);
            BooleanExpression wherepred = null;
            if (!"none".equals(by)) {
                // Create search conditions for where statement in SQL
                wherepred = prh.buildWhere(filtering, by);
            }
            // Retrieve tag list using filtering.
            Iterable<Tag> entitylist = tagService.findAllTags(wherepred, preq);
            List<TagDto> dtolist = edh.entityToDtoList(entitylist, TagDto.class);
            // Create the Set.
            final CrestBaseResponse setdto = new TagSetDto().resources(dtolist)
                    .format("TagSetDto")
                    .size((long) dtolist.size()).datatype("tags");
            if (filters != null) {
                setdto.filter(filters);
            }
            // Response is 200.
            return Response.ok().entity(setdto).build();
        }
        catch (final RuntimeException e) {
            // Exception, send a 500.
            // Error from server. Send a 500.
            final String message = e.getMessage();
            log.error("listTags service exception : {}", message);
            final String error = bundle.getString("log.tag.notfound");
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,
                    message);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
        }
    }
}
