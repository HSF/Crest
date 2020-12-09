package hep.crest.server.swagger.api.impl;

import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.pojo.GlobalTag;
import hep.crest.server.exceptions.NotExistsPojoException;
import hep.crest.server.services.GlobalTagService;
import hep.crest.server.services.TagService;
import hep.crest.server.swagger.api.AdminApiService;
import hep.crest.server.swagger.api.NotFoundException;
import hep.crest.swagger.model.GlobalTagDto;
import hep.crest.swagger.model.TagMetaDto;
import ma.glasnost.orika.MapperFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

/**
 * Rest endpoint for administration task. Essentially allows to remove
 * resources.
 *
 * @author formica
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-09-05T16:23"
                                                                                                   + ":23.401+02:00")
@Component
public class AdminApiServiceImpl extends AdminApiService {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(AdminApiServiceImpl.class);

    /**
     * Service.
     */
    @Autowired
    private GlobalTagService globalTagService;

    /**
     * Service.
     */
    @Autowired
    private TagService tagService;

    /**
     * Mapper.
     */
    @Autowired
    @Qualifier("mapper")
    private MapperFacade mapper;

    /*
     * (non-Javadoc)
     * @see hep.crest.server.swagger.api.AdminApiService#removeGlobalTag(java.lang.
     * String, javax.ws.rs.core.SecurityContext, javax.ws.rs.core.UriInfo)
     */
    @Override
    public Response removeGlobalTag(String name, SecurityContext securityContext, UriInfo info)
            throws NotFoundException {
        log.info("AdminRestController processing request for removing a global tag");
        try {
            // Remove the global tag identified by name.
            globalTagService.removeGlobalTag(name);
            return Response.ok().build();
        }
        catch (final RuntimeException e) {
            final String msg = "Error removing globaltag resource using " + name;
            log.error("removeGlobalTag service exception : {}", msg);
            return ResponseFormatHelper.internalError("removeGlobalTag error: " + msg);
        }
    }

    /*
     * (non-Javadoc)
     * @see hep.crest.server.swagger.api.AdminApiService#removeTag(java.lang.String,
     * javax.ws.rs.core.SecurityContext, javax.ws.rs.core.UriInfo)
     */
    @Override
    public Response removeTag(String name, SecurityContext securityContext, UriInfo info) throws NotFoundException {
        log.info("AdminRestController processing request for removing a tag");
        try {
            // Remove the tag with name.
            TagMetaDto metadto;
            try {
                metadto = tagService.findMeta(name);
                if (metadto != null) {
                    tagService.removeTagMeta(name);
                }
            }
            catch (CdbServiceException e) {
                log.warn("No meta information available for the tag {}", name);
            }

            tagService.removeTag(name);
            return Response.ok().build();
        }
        catch (final RuntimeException e) {
            final String msg = "Error removing tag resource using " + name + " : " + e.getMessage();
            log.error("removeTag service exception : {}", msg);
            return ResponseFormatHelper.internalError("removeTag error: " + msg);
        }
    }

    /*
     * (non-Javadoc)
     * @see hep.crest.server.swagger.api.AdminApiService#updateGlobalTag(java.lang.
     * String, hep.crest.swagger.model.GlobalTagDto,
     * javax.ws.rs.core.SecurityContext, javax.ws.rs.core.UriInfo)
     */
    @Override
    public Response updateGlobalTag(String name, GlobalTagDto body, SecurityContext securityContext, UriInfo info)
            throws NotFoundException {
        log.info("AdminRestController processing request for updating a global tag using " + body);
        try {
            final char type = body.getType() != null ? body.getType().charAt(0) : 'N';

            // Find the global tag corresponding to input name.
            final GlobalTag entity = globalTagService.findOne(name);
//	        final char type = entity.getType() != null ? entity.getType() : 'N';

            // Compare fields to set them from the input body object provided by the client.
            if (entity.getDescription() != body.getDescription()) {
                // change description.
                entity.setDescription(body.getDescription());
            }
            if (entity.getRelease() != body.getRelease()) {
                // change release.
                entity.setRelease(body.getRelease());
            }
            if (entity.getWorkflow() != body.getWorkflow()) {
                // change workflow.
                entity.setWorkflow(body.getWorkflow());
            }
            if (entity.getScenario() != body.getScenario()) {
                // change scenario.
                entity.setScenario(body.getScenario());
            }
            if (entity.getType() != type) {
                // change type.
                entity.setType(type);
            }
            // Update the global tag.
            final GlobalTag saved = globalTagService.updateGlobalTag(entity);
            final GlobalTagDto dto = mapper.map(saved, GlobalTagDto.class);
            return Response.ok().entity(dto).build();
        }
        catch (final NotExistsPojoException e) {
            final String msg = "Error updating GlobalTag resource using " + body;
            log.warn("updateGlobalTag cannot update resource {}", msg);
            return ResponseFormatHelper.notFoundPojo("updateGlobalTag error: " + msg);
        }
        catch (final RuntimeException e) {
            final String msg = "Error updating GlobalTag resource using " + body;
            log.error("updateGlobalTag service exception : {}", msg);
            return ResponseFormatHelper.internalError("updateGlobalTag error: " + msg);
        }
    }
}
