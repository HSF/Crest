package hep.crest.server.swagger.api.impl;

import java.math.BigDecimal;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

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
import hep.crest.server.services.TagService;
import hep.crest.server.swagger.api.ApiResponseMessage;
import hep.crest.server.swagger.api.NotFoundException;
import hep.crest.server.swagger.api.TagsApiService;
import hep.crest.swagger.model.CrestBaseResponse;
import hep.crest.swagger.model.GenericMap;
import hep.crest.swagger.model.TagDto;
import hep.crest.swagger.model.TagSetDto;

/**
 * @author formica
 *
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2017-09-05T16:23:23.401+02:00")
@Component
public class TagsApiServiceImpl extends TagsApiService {

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
    @Qualifier("tagFiltering")
    private IFilteringCriteria filtering;

    /**
     * Service.
     */
    @Autowired
    private TagService tagService;

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
            final TagDto saved = tagService.insertTag(body);
            return Response.created(info.getRequestUri()).entity(saved).build();

        }
        catch (final AlreadyExistsPojoException e) {
            log.error("Cannot create tag {}, name already exists...", body);
            return Response.status(Response.Status.SEE_OTHER).entity(body).build();
        }
        catch (final CdbServiceException e) {
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
            final TagDto dto = tagService.findOne(name);
            if (dto == null) {
                log.debug("Cannot update null tag...." + name);
                final String message = "Tag " + name + " not found...";
                final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,
                        message);
                return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
            }
            for (final String key : body.keySet()) {
                if (key == "description") {
                    dto.setDescription(body.get(key));
                }
                if (key == "timeType") {
                    dto.setTimeType(body.get(key));
                }
                if (key == "lastValidatedTime") {
                    final BigDecimal val = new BigDecimal(body.get(key));
                    dto.setLastValidatedTime(val);
                }
                if (key == "endOfValidity") {
                    final BigDecimal val = new BigDecimal(body.get(key));
                    dto.setEndOfValidity(val);
                }
                if (key == "synchronization") {
                    dto.setSynchronization(body.get(key));
                }
                if (key == "payloadSpec") {
                    dto.setPayloadSpec(body.get(key));
                }
            }
            final TagDto saved = tagService.updateTag(dto);
            return Response.ok(info.getRequestUri()).entity(saved).build();

        }
        catch (final CdbServiceException e) {
            final String message = e.getMessage();
            log.error("Exception in updateTag : {}", e.getMessage());
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,
                    message);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
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
        this.log.info("TagRestController processing request for tag name " + name);
        try {
            final TagDto dto = tagService.findOne(name);
            if (dto == null) {
                log.debug("Entity not found for name " + name);
                final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,
                        "Entity not found for name " + name);
                return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
            }
            final TagSetDto respdto = (TagSetDto) new TagSetDto().addResourcesItem(dto).size(1L)
                    .datatype("tags");
            return Response.ok().entity(respdto).build();
        }
        catch (final Exception e) {
            e.printStackTrace();
            final String message = e.getMessage();
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,
                    message);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
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
            final PageRequest preq = prh.createPageRequest(page, size, sort);
            List<TagDto> dtolist = null;
            final GenericMap filters = new GenericMap();
            if (by.equals("none")) {
                dtolist = tagService.findAllTags(null, preq);
            }
            else {

                final List<SearchCriteria> params = prh.createMatcherCriteria(by);
                final List<BooleanExpression> expressions = filtering
                        .createFilteringConditions(params);
                BooleanExpression wherepred = null;
                for (final SearchCriteria sc : params) {
                    filters.put(sc.getKey(), sc.getValue().toString());
                }
                for (final BooleanExpression exp : expressions) {
                    if (wherepred == null) {
                        wherepred = exp;
                    }
                    else {
                        wherepred = wherepred.and(exp);
                    }
                }
                dtolist = tagService.findAllTags(wherepred, preq);
            }
            if (dtolist == null) {
                final String message = "No resource has been found";
                final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.INFO,
                        message);
                return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
            }
            final CrestBaseResponse respdto = new TagSetDto().resources(dtolist).filter(filters)
                    .format("TagSetDto")
                    .size((long) dtolist.size()).datatype("tags");
            return Response.ok().entity(respdto).build();

        }
        catch (final CdbServiceException e) {
            final String message = e.getMessage();
            log.error("Exception in listTags: {}", e.getMessage());
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,
                    message);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
        }
    }
}
