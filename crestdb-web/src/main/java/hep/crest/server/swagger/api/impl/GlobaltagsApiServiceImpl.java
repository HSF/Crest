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
import hep.crest.server.exceptions.NotExistsPojoException;
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

/**
 * Rest endpoint to manage global tags. It is used for creation or search of
 * global tags.
 *
 * @author formica
 *
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2017-09-05T16:23:23.401+02:00")
@Component
public class GlobaltagsApiServiceImpl extends GlobaltagsApiService {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(GlobaltagsApiServiceImpl.class);

    /**
     * Helper.
     */
    @Autowired
    PageRequestHelper prh;

    /**
     * Filtering.
     */
    @Autowired
    @Qualifier("globalTagFiltering")
    private IFilteringCriteria filtering;

    /**
     * Service.
     */
    @Autowired
    GlobalTagService globaltagService;

    /*
     * (non-Javadoc)
     * 
     * @see
     * hep.crest.server.swagger.api.GlobaltagsApiService#createGlobalTag(hep.crest.
     * swagger.model.GlobalTagDto, java.lang.String,
     * javax.ws.rs.core.SecurityContext, javax.ws.rs.core.UriInfo)
     */
    @Override
    public Response createGlobalTag(GlobalTagDto body, String force,
            SecurityContext securityContext, UriInfo info) throws NotFoundException {
        log.info("GlobalTagRestController processing request for creating a global tag");
        try {
            // If the force mode is active, the insertion time is imposed by the client.
            if (force.equals("false")) {
                // Set to null so that is automatically generated.
                body.setInsertionTime(null);
            }
            // Insert a new global tag.
            final GlobalTagDto saved = globaltagService.insertGlobalTag(body);
            // Send the created status.
            return Response.created(info.getRequestUri()).entity(saved).build();
        }
        catch (final AlreadyExistsPojoException e) {
            // Global tag resource exists already. Send a 303.
            return Response.status(Response.Status.SEE_OTHER).entity(body).build();
        }
        catch (final CdbServiceException e) {
            // Error in creation. Send a 500.
            final String message = e.getMessage();
            log.error("Api method createGlobalTag got exception : {}", message);
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,
                    message);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * hep.crest.server.swagger.api.GlobaltagsApiService#findGlobalTag(java.lang.
     * String, javax.ws.rs.core.SecurityContext, javax.ws.rs.core.UriInfo)
     */
    @Override
    public Response findGlobalTag(String name, SecurityContext securityContext, UriInfo info)
            throws NotFoundException {
        log.info("GlobalTagRestController processing request for global tag name " + name);
        try {
            // Search for a global tag resource.
            final GlobalTagDto dto = globaltagService.findOne(name);
            log.debug("Found GlobalTag " + name);
            // Prepare filters.
            final GenericMap filters = new GenericMap();
            filters.put("name", name);
            // Prepare response set.
            final CrestBaseResponse setdto = new GlobalTagSetDto().addResourcesItem(dto)
                    .format("GlobalTagSetDto").filter(filters).size(1L).datatype("globaltags");
            return Response.ok().entity(setdto).build();

        }
        catch (final NotExistsPojoException e) {
            // Not found. Send a 404.
            final String message = "Global tag " + name + " not found...";
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,
                    message);
            return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
        }
        catch (final Exception e) {
            // Error from server. Send a 500.
            final String message = e.getMessage();
            log.error("Api method findGlobalTag got exception : {}", message);
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,
                    message);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
        }
    }

    /* (non-Javadoc)
     * @see hep.crest.server.swagger.api.GlobaltagsApiService#findGlobalTagFetchTags(java.lang.String, java.lang.String, java.lang.String, javax.ws.rs.core.SecurityContext, javax.ws.rs.core.UriInfo)
     */
    @Override
    public Response findGlobalTagFetchTags(String name, String record, String label,
            SecurityContext securityContext, UriInfo info) throws NotFoundException {
        try {
            // Search for a global tag and associated tags. Use record and label.
            // Preset null record to empty string.
            if (record == null) {
                record = "";
            }
            // Preset null label to empty string.
            if (label == null) {
                label = "";
            }
            log.info("GlobalTagRestController processing request for global tag name " + name);
            // Fetch tags via record and label.
            final List<TagDto> dtolist = globaltagService.getGlobalTagByNameFetchTags(name, record,
                    label);
            log.debug("Found list of tags of length " + (dtolist == null ? "0" : dtolist.size()));
            // Check if dto list is null.
            if (dtolist == null) {
                // Send a 404.
                final String message = "No tag resource has been found";
                final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.INFO,
                        message);
                return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
            }
            // Prepare filters.
            final GenericMap filters = new GenericMap();
            filters.put("name", name);
            final CrestBaseResponse setdto = new TagSetDto().resources(dtolist).format("TagSetDto")
                    .filter(filters).size((long) dtolist.size()).datatype("tags");
            return Response.ok().entity(setdto).build();
        }
        catch (final EmptyPojoException e) {
            // No tags found. Send a 404.
            final String message = "No tags are associated to " + name;
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,
                    message);
            return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
        }
        catch (final CdbServiceException e) {
            // Error from server. Send a 500.
            final String message = e.getMessage();
            log.error("Internal error when getting tag list from global tag: {}", message);
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,
                    message);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
        }
    }

    /* (non-Javadoc)
     * @see hep.crest.server.swagger.api.GlobaltagsApiService#listGlobalTags(java.lang.String, java.lang.Integer, java.lang.Integer, java.lang.String, javax.ws.rs.core.SecurityContext, javax.ws.rs.core.UriInfo)
     */
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
                // Find all without any search condition. Only pagination.
                dtolist = globaltagService.findAllGlobalTags(null, preq);
            }
            else {
                // Some search criteria have been found. Build the boolean expression.
                params = prh.createMatcherCriteria(by);
                filters = prh.getFilters(params);
                final List<BooleanExpression> expressions = filtering
                        .createFilteringConditions(params);
                final BooleanExpression wherepred = prh.getWhere(expressions);
                // Search for global tags using where conditions.
                dtolist = globaltagService.findAllGlobalTags(wherepred, preq);
            }
            if (dtolist == null) {
                // No global tag found. Send a 404. It should send an empty Set ?
                // FIXME verify if an empty set is more coherent with other API methods.
                final String message = "No resource has been found";
                final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.INFO,
                        message);
                return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
            }
            final CrestBaseResponse setdto = new GlobalTagSetDto().resources(dtolist)
                    .format("GlobalTagSetDto").size((long) dtolist.size()).datatype("globaltags");
            if (filters != null) {
                setdto.filter(filters);
            }
            return Response.ok().entity(setdto).build();

        }
        catch (final CdbServiceException e) {
            // Error from server. Send a 500.
            final String message = e.getMessage();
            log.error("Internal error in loading global tags : {}", message);
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,
                    message);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
        }
    }
}
