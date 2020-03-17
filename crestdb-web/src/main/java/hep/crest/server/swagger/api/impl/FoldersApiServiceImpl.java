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
import hep.crest.server.services.FolderService;
import hep.crest.server.swagger.api.ApiResponseMessage;
import hep.crest.server.swagger.api.FoldersApiService;
import hep.crest.server.swagger.api.NotFoundException;
import hep.crest.swagger.model.CrestBaseResponse;
import hep.crest.swagger.model.FolderDto;
import hep.crest.swagger.model.FolderSetDto;
import hep.crest.swagger.model.GenericMap;

/**
 * Rest endpoint for folder administration.
 * The folders do not exist in CMS environment.
 * They can be used in ATLAS as a way to map old COOL nodes and for authorization purposes.
 * An important element for this authorization is the base tag name pattern, which impose
 * a string for all tag names of a given system.
 *
 * @author formica
 *
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2018-05-10T14:57:11.305+02:00")
@Component
public class FoldersApiServiceImpl extends FoldersApiService {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(FoldersApiServiceImpl.class);

    /**
     * Helper.
     */
    @Autowired
    private PageRequestHelper prh;

    /**
     * Filtering.
     */
    @Autowired
    @Qualifier("folderFiltering")
    private IFilteringCriteria filtering;

    /**
     * Service.
     */
    @Autowired
    private FolderService folderService;

    /*
     * (non-Javadoc)
     *
     * @see
     * hep.crest.server.swagger.api.FoldersApiService#createFolder(hep.crest.swagger
     * .model.FolderDto, javax.ws.rs.core.SecurityContext, javax.ws.rs.core.UriInfo)
     */
    @Override
    public Response createFolder(FolderDto body, SecurityContext securityContext, UriInfo info)
            throws NotFoundException {
        log.info("FolderRestController processing request for creating a folder");
        try {
            // Insert the new folder.
            final FolderDto saved = folderService.insertFolder(body);
            return Response.created(info.getRequestUri()).entity(saved).build();
        }
        catch (final AlreadyExistsPojoException e) {
            // The folder exists, send a 303
            return Response.status(Response.Status.SEE_OTHER).entity(body).build();
        }
        catch (final CdbServiceException e) {
            // The folder insertion failed, send a 500...
            final String message = e.getMessage();
            log.error("Cannot create folder {}: {}", body, e);
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,
                    message);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * hep.crest.server.swagger.api.FoldersApiService#listFolders(java.lang.String,
     * java.lang.String, javax.ws.rs.core.SecurityContext, javax.ws.rs.core.UriInfo)
     */
    @Override
    public Response listFolders(String by, String sort, SecurityContext securityContext,
            UriInfo info) throws NotFoundException {
        try {
            log.debug("Search resource list using by={}, sort={}", by, sort);
            // Create a default page requests with 10000 size for retrieval.
            // This method does not allow to set pagination.
            final PageRequest preq = prh.createPageRequest(0, 10000, sort);
            List<FolderDto> dtolist = null;
            List<SearchCriteria> params = null;
            final GenericMap filters = new GenericMap();
            // No search conditions.
            if ("none".equals(by)) {
                // Find all folders.
                dtolist = folderService.findAllFolders(null, preq);
            }
            else {
                // A search pattern exists, create the criteria.
                params = prh.createMatcherCriteria(by);
                for (final SearchCriteria sc : params) {
                    filters.put(sc.getKey(), sc.getValue().toString());
                }
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
                // Search folder using the expression build before.
                dtolist = folderService.findAllFolders(wherepred, preq);
            }
            if (dtolist == null) {
                // Nothing found, send a 404. Should be OK with an Empty list ?
                final String message = "No resource has been found";
                final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.INFO,
                        message);
                return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
            }
            // Prepare the response set.
            final CrestBaseResponse setdto = new FolderSetDto().resources(dtolist)
                    .size((long) dtolist.size()).datatype("folders");
            // Set the filters.
            if (!filters.isEmpty()) {
                setdto.filter(filters);
            }
            return Response.ok().entity(setdto).build();

        }
        catch (final CdbServiceException e) {
            // Error occurred. Send a 500.
            final String message = e.getMessage();
            log.error("Api method listFolders got exception : {}", message);
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,
                    message);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
        }
    }
}
