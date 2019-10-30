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
import hep.crest.server.security.FolderService;
import hep.crest.server.swagger.api.ApiResponseMessage;
import hep.crest.server.swagger.api.FoldersApiService;
import hep.crest.server.swagger.api.NotFoundException;
import hep.crest.swagger.model.CrestBaseResponse;
import hep.crest.swagger.model.FolderDto;
import hep.crest.swagger.model.FolderSetDto;
import hep.crest.swagger.model.GenericMap;

/**
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
            final FolderDto saved = folderService.insertFolder(body);
            return Response.created(info.getRequestUri()).entity(saved).build();
        }
        catch (final AlreadyExistsPojoException e) {
            return Response.status(Response.Status.SEE_OTHER).entity(body).build();
        }
        catch (final CdbServiceException e) {
            // TODO Auto-generated catch block
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
     * @see
     * hep.crest.server.swagger.api.FoldersApiService#listFolders(java.lang.String,
     * java.lang.String, javax.ws.rs.core.SecurityContext, javax.ws.rs.core.UriInfo)
     */
    @Override
    public Response listFolders(String by, String sort, SecurityContext securityContext,
            UriInfo info) throws NotFoundException {
        try {
            log.debug("Search resource list using by={}, sort={}", by, sort);
            final PageRequest preq = prh.createPageRequest(0, 10000, sort);
            List<FolderDto> dtolist = null;
            List<SearchCriteria> params = null;
            final GenericMap filters = new GenericMap();
           
            if (by.equals("none")) {
                dtolist = folderService.findAllFolders(null, preq);
            }
            else {
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
                dtolist = folderService.findAllFolders(wherepred, preq);
            }
            if (dtolist == null) {
                final String message = "No resource has been found";
                final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.INFO,
                        message);
                return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
            }
            final CrestBaseResponse setdto = new FolderSetDto().resources(dtolist)
                    .size((long) dtolist.size()).datatype("folders");
            if (!filters.isEmpty()) {
                setdto.filter(filters);
            }
            return Response.ok().entity(setdto).build();

        }
        catch (final CdbServiceException e) {
            final String message = e.getMessage();
            log.error("Api method listFolders got exception : {}", message);
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,
                    message);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
        }
    }
}
