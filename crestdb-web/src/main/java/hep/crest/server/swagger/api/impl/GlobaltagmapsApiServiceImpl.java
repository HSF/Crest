package hep.crest.server.swagger.api.impl;

import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.server.services.GlobalTagMapService;
import hep.crest.server.swagger.api.ApiResponseMessage;
import hep.crest.server.swagger.api.GlobaltagmapsApiService;
import hep.crest.server.swagger.api.NotFoundException;
import hep.crest.swagger.model.CrestBaseResponse;
import hep.crest.swagger.model.GenericMap;
import hep.crest.swagger.model.GlobalTagMapDto;
import hep.crest.swagger.model.GlobalTagMapSetDto;

/**
 * @author formica
 *
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2017-09-05T16:23:23.401+02:00")
@Component
public class GlobaltagmapsApiServiceImpl extends GlobaltagmapsApiService {

    /**
     * Logger.
     */
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Service.
     */
    @Autowired
    private GlobalTagMapService globaltagmapService;

    /*
     * (non-Javadoc)
     *
     * @see
     * hep.crest.server.swagger.api.GlobaltagmapsApiService#createGlobalTagMap(hep.
     * crest.swagger.model.GlobalTagMapDto, javax.ws.rs.core.SecurityContext,
     * javax.ws.rs.core.UriInfo)
     */
    @Override
    public Response createGlobalTagMap(GlobalTagMapDto body, SecurityContext securityContext,
            UriInfo info) throws NotFoundException {
        this.log.info(
                "GlobalTagMapRestController processing request for creating a global tag map entry "
                        + body);
        try {
            final GlobalTagMapDto saved = globaltagmapService.insertGlobalTagMap(body);
            return Response.created(info.getRequestUri()).entity(saved).build();

        }
        catch (final CdbServiceException e) {
            final String msg = "Error creating globaltagmap resource using " + body.toString();
            e.printStackTrace();
            final String message = e.getMessage();
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,
                    msg + " : " + message);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * hep.crest.server.swagger.api.GlobaltagmapsApiService#findGlobalTagMap(java.
     * lang.String, java.lang.String, javax.ws.rs.core.SecurityContext,
     * javax.ws.rs.core.UriInfo)
     */
    @Override
    public Response findGlobalTagMap(String name, String xCrestMapMode,
            SecurityContext securityContext, UriInfo info) throws NotFoundException {
        this.log.info("GlobalTagMapRestController processing request to get map for GlobalTag name "
                + name);
        try {
            List<GlobalTagMapDto> dtolist = null;
            if (xCrestMapMode == null) {
                xCrestMapMode = "Trace";
            }
            if (xCrestMapMode.equals("Trace")) {
                dtolist = globaltagmapService.getTagMap(name);
            }
            else {
                dtolist = globaltagmapService.getTagMapByTagName(name);
            }
            final GenericMap filters = new GenericMap();
            filters.put("name", name);
            filters.put("mode", xCrestMapMode);
            final CrestBaseResponse setdto = new GlobalTagMapSetDto().resources(dtolist)
                    .filter(filters).size((long) dtolist.size()).datatype("maps");
            if (dtolist.size() == 0) {
                return Response.status(Response.Status.NOT_FOUND).entity(setdto).build();
            }
            return Response.ok().entity(setdto).build();

        }
        catch (final CdbServiceException e) {
            final String message = e.getMessage();
            log.error("Internal error searching maps : {}", message);
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,
                    message);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
        }
    }
}
