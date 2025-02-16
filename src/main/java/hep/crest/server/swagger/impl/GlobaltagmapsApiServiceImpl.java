package hep.crest.server.swagger.impl;

import hep.crest.server.controllers.EntityDtoHelper;
import hep.crest.server.converters.GlobalTagMapMapper;
import hep.crest.server.data.pojo.GlobalTagMap;
import hep.crest.server.services.GlobalTagMapService;
import hep.crest.server.swagger.api.GlobaltagmapsApiService;
import hep.crest.server.swagger.model.CrestBaseResponse;
import hep.crest.server.swagger.model.GenericMap;
import hep.crest.server.swagger.model.GlobalTagMapDto;
import hep.crest.server.swagger.model.GlobalTagMapSetDto;
import hep.crest.server.swagger.model.RespPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;

/**
 * Rest endpoint to deal with mappings between tags and global tags. Allows to
 * create and find mappings.
 *
 * @author formica
 */
@Component
@Slf4j
public class GlobaltagmapsApiServiceImpl extends GlobaltagmapsApiService {
    /**
     * Service.
     */
    @Autowired
    private GlobalTagMapService globaltagmapService;

    /**
     * Helper.
     */
    private EntityDtoHelper edh;

    /**
     * Mapper.
     */
    private GlobalTagMapMapper mapper;

    /**
     * Context
     *
     */
    @Autowired
    private JAXRSContext context;

    /**
     * Ctor with injected service.
     * @param globaltagmapService the global tag map service.
     * @param mapper the global tag map mapper.
     * @param edh the entity dto helper.
     *
     */
    @Autowired
    public GlobaltagmapsApiServiceImpl(GlobalTagMapService globaltagmapService,
                                       GlobalTagMapMapper mapper,
                                       EntityDtoHelper edh) {
        this.globaltagmapService = globaltagmapService;
        this.mapper = mapper;
        this.edh = edh;
    }


    /*
     * (non-Javadoc)
     * @see
     * hep.crest.server.swagger.api.GlobaltagmapsApiService#createGlobalTagMap(hep.
     * crest.swagger.model.GlobalTagMapDto, jakarta.ws.rs.core.SecurityContext,
     * jakarta.ws.rs.core.UriInfo)
     */
    @Override
    public Response createGlobalTagMap(GlobalTagMapDto body, SecurityContext securityContext) {
        log.info("Associate tag {} to globaltag {}", body.getTagName(), body.getGlobalTagName());
        // Insert new mapping resource.
        GlobalTagMap entity = mapper.toEntity(body);
        final GlobalTagMap saved = globaltagmapService.insertGlobalTagMap(entity);
        GlobalTagMapDto dto = mapper.toDto(saved);
        return Response.created(context.getUriInfo().getRequestUri()).entity(dto).build();
    }

    /*
     * (non-Javadoc)
     * @see
     * hep.crest.server.swagger.api.GlobaltagmapsApiService#findGlobalTagMap(java.
     * lang.String, java.lang.String, jakarta.ws.rs.core.SecurityContext,
     * jakarta.ws.rs.core.UriInfo)
     */
    @Override
    public Response findGlobalTagMap(String name, String xCrestMapMode, SecurityContext securityContext) {
        log.info("Get tags for globaltag {} ", name);
        // Prepare filters
        final GenericMap filters = new GenericMap();
        if (name != null) {
            filters.put("name", name);
        }
        // Set cache control to 5 minutes.
        CacheControl cc = new CacheControl();
        cc.setMaxAge(600);
        Iterable<GlobalTagMap> entitylist = null;
        // If there is no header then set it to Trace mode. Implies that you search tags
        // associated with a global tag. The input name will be considered as a
        // GlobalTag name.
        if (xCrestMapMode == null) {
            xCrestMapMode = "Trace";
        }
        filters.put("mode", xCrestMapMode);

        if ("trace".equalsIgnoreCase(xCrestMapMode)) {
            // The header is Trace, so search for tags associated to a global tag.
            entitylist = globaltagmapService.getTagMap(name);
        }
        else {
            // The header is not Trace, so search for global tags associated to a tag.
            // The input name is considered a Tag name.
            entitylist = globaltagmapService.getTagMapByTagName(name);
        }
        List<GlobalTagMapDto> dtolist = edh.entityToDtoList(entitylist, GlobalTagMapDto.class,
                GlobalTagMapMapper.class);
        RespPage respPage = new RespPage().size(dtolist.size())
                .totalElements((long)dtolist.size()).totalPages(1)
                .number(0);
        final CrestBaseResponse setdto = new GlobalTagMapSetDto().resources(dtolist).filter(filters)
                .size((long) dtolist.size())
                .page(respPage)
                .datatype("maps").format("GlobalTagMapSetDto");
        Response.Status status = Response.Status.OK;
        return Response.status(status).cacheControl(cc).entity(setdto).build();
    }

    @Override
    public Response deleteGlobalTagMap(String name, @NotNull String label,
                                       @NotNull String tagname, String mrecord,
                                       SecurityContext securityContext) {
        log.info("Remove association of tag {} for globaltag {} ", tagname, name);
        // Prepare filters
        final GenericMap filters = new GenericMap();
        if (name != null) {
            filters.put("globaltagname", name);
        }
        filters.put("label", label);
        filters.put("tagname", tagname);
        if (mrecord != null) {
            filters.put("record", mrecord);
        }
        Iterable<GlobalTagMap> entitylist = null;
        // If there is no header then set it to Trace mode. Implies that you search tags
        // associated with a global tag. The input name will be considered as a
        // GlobalTag name.
        entitylist = globaltagmapService.findMapsByGlobalTagLabelTag(name, label, tagname, mrecord);
        // Delete the full list inside a transaction.
        List<GlobalTagMap> deletedlist = globaltagmapService.deleteMapList(entitylist);
        RespPage respPage = new RespPage().size(deletedlist.size())
                .totalElements((long)deletedlist.size()).totalPages(1)
                .number(0);
        // Return the deleted list.
        List<GlobalTagMapDto> dtolist = edh.entityToDtoList(deletedlist, GlobalTagMapDto.class,
                GlobalTagMapMapper.class);
        final CrestBaseResponse setdto = new GlobalTagMapSetDto().resources(dtolist).filter(filters)
                .size((long) dtolist.size())
                .page(respPage)
                .datatype("maps")
                .format("GlobalTagMapSetDto");
        return Response.status(Response.Status.OK).entity(setdto).build();
    }

}
