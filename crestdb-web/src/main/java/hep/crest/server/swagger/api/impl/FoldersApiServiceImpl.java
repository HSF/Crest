package hep.crest.server.swagger.api.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import hep.crest.data.repositories.querydsl.IFilteringCriteria;
import hep.crest.data.security.pojo.CrestFolders;
import hep.crest.server.controllers.EntityDtoHelper;
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
import ma.glasnost.orika.MapperFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.util.List;

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
     * Helper.
     */
    @Autowired
    EntityDtoHelper edh;

    /**
     * Mapper.
     */
    @Autowired
    @Qualifier("mapper")
    private MapperFacade mapper;

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
            CrestFolders entity = mapper.map(body, CrestFolders.class);
            final CrestFolders saved = folderService.insertFolder(entity);
            FolderDto dto = mapper.map(saved, FolderDto.class);
            return Response.created(info.getRequestUri()).entity(dto).build();
        }
        catch (final AlreadyExistsPojoException e) {
            // The folder exists, send a 303
            log.warn("createFolder resource exists : {}", e);
            final String msg = "CrestFolder already exists for name : " + body.getNodeFullpath();
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.INFO, msg);
            return Response.status(Response.Status.SEE_OTHER).entity(resp).build();
        }
        catch (final RuntimeException e) {
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
            // Create filters
            GenericMap filters = prh.getFilters(prh.createMatcherCriteria(by));
            // Create a default page requests with 10000 size for retrieval.
            // This method does not allow to set pagination.
            final PageRequest preq = prh.createPageRequest(0, 10000, sort);
            BooleanExpression wherepred = null;
            if (!"none".equals(by)) {
                // Create search conditions for where statement in SQL
                wherepred = prh.buildWhere(filtering, by);
            }
            // Search for global tags using where conditions.
            Iterable<CrestFolders> entitylist = folderService.findAllFolders(wherepred, preq);
            final List<FolderDto> dtolist = edh.entityToDtoList(entitylist, FolderDto.class);
            Response.Status rstatus = Response.Status.OK;
            final CrestBaseResponse setdto = new FolderSetDto().resources(dtolist)
                    .format("FodlerSetDto").size((long) dtolist.size()).datatype("folders");
            if (filters != null) {
                setdto.filter(filters);
            }
            return Response.status(rstatus).entity(setdto).build();
        }
        catch (final RuntimeException e) {
            // Error occurred. Send a 500.
            final String message = e.getMessage();
            log.error("Api method listFolders got exception : {}", message);
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,
                    message);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
        }
    }
}
