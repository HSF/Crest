package hep.crest.server.swagger.api.impl;

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
import hep.crest.server.exceptions.EmptyPojoException;
import hep.crest.server.services.GlobalTagService;
import hep.crest.server.swagger.api.ApiResponseMessage;
import hep.crest.server.swagger.api.GlobaltagsApiService;
import hep.crest.server.swagger.api.NotFoundException;
import hep.crest.swagger.model.CrestBaseResponse;
import hep.crest.swagger.model.GenericMap;
import hep.crest.swagger.model.GlobalTagDto;
import hep.crest.swagger.model.GlobalTagSetDto;
import hep.crest.swagger.model.TagDto;
import hep.crest.swagger.model.TagSetDto;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2017-09-05T16:23:23.401+02:00")
@Component
public class GlobaltagsApiServiceImpl extends GlobaltagsApiService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    PageRequestHelper prh;

    @Autowired
    @Qualifier("globalTagFiltering")
    private IFilteringCriteria filtering;

    @Autowired
    GlobalTagService globaltagService;

    @Override
    public Response createGlobalTag(GlobalTagDto body, String force,
            SecurityContext securityContext, UriInfo info) throws NotFoundException {
        log.info("GlobalTagRestController processing request for creating a global tag");
        try {
            if (force.equals("false")) {
                body.setInsertionTime(null);
            }
            final GlobalTagDto saved = globaltagService.insertGlobalTag(body);
            return Response.created(info.getRequestUri()).entity(saved).build();
        }
        catch (final AlreadyExistsPojoException e) {
            return Response.status(Response.Status.SEE_OTHER).entity(body).build();
        }
        catch (final CdbServiceException e) {
            final String message = e.getMessage();
            log.error("Api method createGlobalTag got exception : {}", message);
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,
                    message);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
        }
    }

    @Override
    public Response findGlobalTag(String name, SecurityContext securityContext, UriInfo info)
            throws NotFoundException {
        this.log.info("GlobalTagRestController processing request for global tag name " + name);
        try {
            final GlobalTagDto dto = globaltagService.findOne(name);
            if (dto != null) {
                log.debug("Found GlobalTag " + name);
            }
            else {
                final String message = "Global tag " + name + " not found...";
                final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,
                        message);
                return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
            }
            final GenericMap filters = new GenericMap();
            filters.put("name", name);
            final CrestBaseResponse setdto = new GlobalTagSetDto().addResourcesItem(dto)
                    .filter(filters).size(1L).datatype("globaltags");
            return Response.ok().entity(setdto).build();

        }
        catch (final Exception e) {
            final String message = e.getMessage();
            log.error("Api method findGlobalTag got exception : {}", message);
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,
                    message);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
        }
    }

    @Override
    public Response findGlobalTagFetchTags(String name, String record, String label,
            SecurityContext securityContext, UriInfo info) throws NotFoundException {
        try {
            if (record == null) {
                record = "";
            }
            if (label == null) {
                label = "";
            }
            this.log.info("GlobalTagRestController processing request for global tag name " + name);
            final List<TagDto> dtolist = globaltagService.getGlobalTagByNameFetchTags(name, record,
                    label);
            log.debug("Found list of tags of length " + (dtolist == null ? "0" : dtolist.size()));
            if (dtolist == null || dtolist.isEmpty()) {
                final String message = "No tag resource has been found";
                final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.INFO,
                        message);
                return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
            }
            final GenericMap filters = new GenericMap();
            filters.put("name", name);
            final CrestBaseResponse setdto = new TagSetDto().resources(dtolist).filter(filters)
                    .size((long) dtolist.size()).datatype("tags");
            return Response.ok().entity(setdto).build();
        }
        catch (final EmptyPojoException e) {
            final String message = "No tags are associated to " + name;
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,
                    message);
            return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
        }
        catch (final CdbServiceException e) {
            final String message = e.getMessage();
            log.error("Internal error when getting tag list from global tag: {}", message);
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,
                    message);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
        }
    }

    @Override
    public Response listGlobalTags(String by, Integer page, Integer size, String sort,
            SecurityContext securityContext, UriInfo info) throws NotFoundException {
        try {
            log.debug("Search resource list using by={}, page={}, size={}, sort={}", by, page, size,
                    sort);
            final PageRequest preq = prh.createPageRequest(page, size, sort);
            List<GlobalTagDto> dtolist = null;
            List<SearchCriteria> params = null;
            GenericMap filters = null;
            if (by.equals("none")) {
                dtolist = globaltagService.findAllGlobalTags(null, preq);
            }
            else {

                params = prh.createMatcherCriteria(by);
                filters = prh.getFilters(params);
                final List<BooleanExpression> expressions = filtering
                        .createFilteringConditions(params);
                final BooleanExpression wherepred = prh.getWhere(expressions);
                dtolist = globaltagService.findAllGlobalTags(wherepred, preq);
            }
            if (dtolist == null) {
                final String message = "No resource has been found";
                final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.INFO,
                        message);
                return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
            }
            final CrestBaseResponse respdto = new GlobalTagSetDto().resources(dtolist)
                    .size((long) dtolist.size()).datatype("globaltags");
            if (filters != null) {
                respdto.filter(filters);
            }
            return Response.ok().entity(respdto).build();

        }
        catch (final CdbServiceException e) {
            final String message = e.getMessage();
            log.error("Internal error in loading global tags : {}", message);
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,
                    message);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
        }
    }
}
