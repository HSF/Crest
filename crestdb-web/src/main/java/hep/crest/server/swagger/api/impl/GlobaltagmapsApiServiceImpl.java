package hep.crest.server.swagger.api.impl;

import hep.crest.data.pojo.GlobalTagMap;
import hep.crest.server.controllers.EntityDtoHelper;
import hep.crest.server.exceptions.AlreadyExistsPojoException;
import hep.crest.server.exceptions.NotExistsPojoException;
import hep.crest.server.services.GlobalTagMapService;
import hep.crest.server.swagger.api.ApiResponseMessage;
import hep.crest.server.swagger.api.GlobaltagmapsApiService;
import hep.crest.server.swagger.api.NotFoundException;
import hep.crest.swagger.model.CrestBaseResponse;
import hep.crest.swagger.model.GenericMap;
import hep.crest.swagger.model.GlobalTagMapDto;
import hep.crest.swagger.model.GlobalTagMapSetDto;
import ma.glasnost.orika.MapperFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import java.util.List;

/**
 * Rest endpoint to deal with mappings between tags and global tags. Allows to
 * create and find mappings.
 *
 * @author formica
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-09-05T16:23:23.401+02:00")
@Component
public class GlobaltagmapsApiServiceImpl extends GlobaltagmapsApiService {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(GlobaltagmapsApiServiceImpl.class);

    /**
     * Service.
     */
    @Autowired
    private GlobalTagMapService globaltagmapService;
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

    /*
     * (non-Javadoc)
     * @see
     * hep.crest.server.swagger.api.GlobaltagmapsApiService#createGlobalTagMap(hep.
     * crest.swagger.model.GlobalTagMapDto, javax.ws.rs.core.SecurityContext,
     * javax.ws.rs.core.UriInfo)
     */
    @Override
    public Response createGlobalTagMap(GlobalTagMapDto body, SecurityContext securityContext, UriInfo info)
            throws NotFoundException {
        log.info("GlobalTagMapRestController processing request for creating a global tag map entry " + body);
        try {
            // Insert new mapping resource.
            GlobalTagMap entity = mapper.map(body, GlobalTagMap.class);
            final GlobalTagMap saved = globaltagmapService.insertGlobalTagMap(entity);
            GlobalTagMapDto dto = mapper.map(saved, GlobalTagMapDto.class);

            return Response.created(info.getRequestUri()).entity(dto).build();
        }
        catch (final RuntimeException e) {
            // Error in creation. Send a 500.
            final String message = e.getMessage();
            log.error("Api method createGlobalTagMap got exception : {}", message);
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, message);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
        }
        catch (NotExistsPojoException e) {
            // Not found. Send a 404.
            log.warn("Api method createGlobalTagMap cannot find resource : {}", body);
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.INFO,
                    "Cannot find GlobalTag or Tag to create the mapping");
            return Response.status(Response.Status.BAD_REQUEST).entity(resp).build();
        }
        catch (AlreadyExistsPojoException e) {
            // See other. Send a 303.
            log.warn("createGlobalTagMap resource exists : {}", e);
            final String msg = "GlobalTagMap already exists : " + body;
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.INFO, msg);
            return Response.status(Response.Status.SEE_OTHER).entity(resp).build();
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * hep.crest.server.swagger.api.GlobaltagmapsApiService#findGlobalTagMap(java.
     * lang.String, java.lang.String, javax.ws.rs.core.SecurityContext,
     * javax.ws.rs.core.UriInfo)
     */
    @Override
    public Response findGlobalTagMap(String name, String xCrestMapMode, SecurityContext securityContext, UriInfo info)
            throws NotFoundException {
        log.info("GlobalTagMapRestController processing request to get map for GlobalTag name " + name);
        // Prepare filters
        final GenericMap filters = new GenericMap();
        filters.put("name", name);
        filters.put("mode", xCrestMapMode);
        try {
            Iterable<GlobalTagMap> entitylist = null;
            // If there is no header then set it to Trace mode. Implies that you search tags
            // associated with a global tag. The input name will be considered as a
            // GlobalTag name.
            if (xCrestMapMode == null) {
                xCrestMapMode = "Trace";
            }
            if ("trace".equalsIgnoreCase(xCrestMapMode)) {
                // The header is Trace, so search for tags associated to a global tag.
                entitylist = globaltagmapService.getTagMap(name);
            }
            else {
                // The header is not Trace, so search for global tags associated to a tag.
                // The input name is considered a Tag name.
                entitylist = globaltagmapService.getTagMapByTagName(name);
            }
            List<GlobalTagMapDto> dtolist = edh.entityToDtoList(entitylist, GlobalTagMapDto.class);
            final CrestBaseResponse setdto = new GlobalTagMapSetDto().resources(dtolist).filter(filters)
                    .format("GlobalTagMapSetDto").size((long) dtolist.size()).datatype("maps");
            Response.Status status = Response.Status.OK;
            return Response.status(status).entity(setdto).build();
        }
        catch (final RuntimeException e) {
            // Error in finding mappings. Send a 500.
            final String message = e.getMessage();
            log.error("Internal error searching maps : {}", message);
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, message);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
        }
    }

    @Override
    public Response deleteGlobalTagMap(String name, @NotNull String label, @NotNull String tagname, String record,
            SecurityContext securityContext, UriInfo info) throws NotFoundException {
        log.info("GlobalTagMapRestController processing request to delete map for GlobalTag name " + name);
        // Prepare filters
        final GenericMap filters = new GenericMap();
        filters.put("globaltagname", name);
        filters.put("label", label);
        filters.put("tagname", tagname);
        if (record != null) {
            filters.put("record", record);
        }
        try {
            Iterable<GlobalTagMap> entitylist = null;
            // If there is no header then set it to Trace mode. Implies that you search tags
            // associated with a global tag. The input name will be considered as a
            // GlobalTag name.
            entitylist = globaltagmapService.findMapsByGlobalTagLabelTag(name, label, tagname, record);
            // Delete the full list inside a transaction.
            List<GlobalTagMap> deletedlist = globaltagmapService.deleteMapList(entitylist);
            // Return the deleted list.
            List<GlobalTagMapDto> dtolist = edh.entityToDtoList(deletedlist, GlobalTagMapDto.class);
            final CrestBaseResponse setdto = new GlobalTagMapSetDto().resources(dtolist).filter(filters)
                    .format("GlobalTagMapSetDto").size((long) dtolist.size()).datatype("maps");
            Response.Status status = Response.Status.OK;
            return Response.status(status).entity(setdto).build();
        }
        catch (final RuntimeException e) {
            // Error in finding mappings. Send a 500.
            final String message = e.getMessage();
            log.error("Internal error searching maps : {}", message);
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, message);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
        }
    }

}
