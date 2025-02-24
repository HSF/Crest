package hep.crest.server.swagger.impl;

import hep.crest.server.controllers.EntityDtoHelper;
import hep.crest.server.converters.FolderMapper;
import hep.crest.server.data.pojo.CrestFolders;
import hep.crest.server.services.FolderService;
import hep.crest.server.swagger.api.FoldersApiService;
import hep.crest.server.swagger.api.NotFoundException;
import hep.crest.server.swagger.model.CrestBaseResponse;
import hep.crest.server.swagger.model.FolderDto;
import hep.crest.server.swagger.model.FolderSetDto;
import hep.crest.server.swagger.model.GenericMap;
import hep.crest.server.swagger.model.RespPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;

/**
 * Rest endpoint for folder administration.
 * The folders do not exist in CMS environment.
 * They can be used in ATLAS as a way to map old COOL nodes and for authorization purposes.
 * An important element for this authorization is the base tag name pattern, which impose
 * a string for all tag names of a given system.
 *
 * @author formica
 */
@Component
@Slf4j
public class FoldersApiServiceImpl extends FoldersApiService {

    /**
     * Helper.
     */
    EntityDtoHelper edh;

    /**
     * Mapper.
     */
    private FolderMapper mapper;

    /**
     * Service.
     */
    private FolderService folderService;

    /**
     * Context.
     */
    private JAXRSContext context;

    /**
     * Ctor with injected service.
     * @param folderService the service.
     * @param edh the EntityDtoHelper
     * @param context the context.
     */
    @Autowired
    public FoldersApiServiceImpl(FolderService folderService,
                                 EntityDtoHelper edh, JAXRSContext context) {
        this.folderService = folderService;
        this.edh = edh;
        this.context = context;
    }


    /*
     * (non-Javadoc)
     *
     * @see
     * hep.crest.server.swagger.api.FoldersApiService#createFolder(hep.crest.swagger
     * .model.FolderDto, jakarta.ws.rs.core.SecurityContext, jakarta.ws.rs.core.UriInfo)
     */
    @Override
    public Response createFolder(FolderDto body, SecurityContext securityContext)
            throws NotFoundException {
        log.info("FolderRestController processing request for creating a folder");
        // Insert the new folder.
        CrestFolders entity = mapper.toEntity(body);
        final CrestFolders saved = folderService.insertFolder(entity);
        FolderDto dto = mapper.toDto(saved);
        return Response.created(context.getUriInfo().getRequestUri()).entity(dto).build();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * hep.crest.server.swagger.api.FoldersApiService#listFolders(java.lang.String,
     * java.lang.String, jakarta.ws.rs.core.SecurityContext, jakarta.ws.rs.core.UriInfo)
     */
    @Override
    public Response listFolders(String schema, SecurityContext securityContext)
            throws NotFoundException {
        log.debug("Search resource list using schema={}", schema);
        // Create filters
        GenericMap filters = new GenericMap();
        if (schema != null) {
            filters.put("schema", schema);
        }
        // Search for folders using schema where condition.
        List<CrestFolders> entitypage = folderService.findFoldersBySchema(schema);
        RespPage respPage = new RespPage().size(entitypage.size())
                .totalElements(Long.valueOf(entitypage.size())).totalPages(1)
                .number(0);
        // Now pass back the dto list.
        final List<FolderDto> dtolist = edh.entityToDtoList(entitypage, FolderDto.class,
                FolderMapper.class);
        Response.Status rstatus = Response.Status.OK;
        // Create the response object using also the page.
        final CrestBaseResponse setdto = new FolderSetDto().resources(dtolist)
                .page(respPage)
                .size((long) dtolist.size()).datatype("folders");
        if (filters != null) {
            setdto.filter(filters);
        }
        return Response.status(rstatus).entity(setdto).build();
    }
}
