package hep.crest.server.swagger.impl;

import hep.crest.server.converters.GlobalTagMapper;
import hep.crest.server.data.pojo.GlobalTag;
import hep.crest.server.data.pojo.GlobalTagMap;
import hep.crest.server.exceptions.CdbSQLException;
import hep.crest.server.exceptions.ConflictException;
import hep.crest.server.services.GlobalTagMapService;
import hep.crest.server.services.GlobalTagService;
import hep.crest.server.services.TagService;
import hep.crest.server.swagger.api.AdminApiService;
import hep.crest.server.swagger.model.GlobalTagDto;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.JDBCException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

/**
 * Rest endpoint for administration task. Essentially allows to remove
 * resources.
 *
 * @author formica
 */
@Component
@Slf4j
public class AdminApiServiceImpl extends AdminApiService {
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
     * Service.
     */
    @Autowired
    private GlobalTagMapService globalTagMapService;

    /**
     * Mapper.
     */
    @Autowired
    private GlobalTagMapper mapper;

    /* (non-Javadoc)
     * @see hep.crest.server.swagger.api.AdminApiService#removeGlobalTag(java.lang.String, jakarta.ws.rs.core.SecurityContext, jakarta.ws.rs.core.UriInfo)
     */
    @Override
    public Response removeGlobalTag(String name, SecurityContext securityContext) {
        log.info("AdminRestController processing request for removing global tag {}", name);
        // Remove the global tag identified by name.
        globalTagService.removeGlobalTag(name);
        return Response.ok().build();
    }

    /*
     * (non-Javadoc)
     * @see hep.crest.server.swagger.api.AdminApiService#removeTag(java.lang.String,
     * jakarta.ws.rs.core.SecurityContext, jakarta.ws.rs.core.UriInfo)
     */
    @Override
    public Response removeTag(String name, SecurityContext securityContext) {
        log.info("AdminRestController processing request for removing tag {}", name);
        // Remove the tag with name.
        // Verify that the tag is present. In case not, this method will throw an exception.
        tagService.findOne(name);
        // Get the list of global tags that are associated to this tag.
        Iterable<GlobalTagMap> associatedGlobalTags = globalTagMapService.getTagMapByTagName(name);
        if (associatedGlobalTags.iterator().hasNext()) {
            // Some global tags are associated to this tag. We cannot proceed to remove it.
            // Send an error message.
            log.error("Cannot remove tag {}, found association with global tags.", name);
            throw new ConflictException("Cannot remove tag "
                    + name
                    + ": clean up associations with global tags");
        }
        // We remove the tag. Here we could also test for locking status of the tag or similar.
        try {
            tagService.removeTag(name);
            return Response.ok().build();
        }
        catch (JDBCException e) {
            throw new CdbSQLException("Error in SQL execution", e);
        }
    }

    /*
     * (non-Javadoc)
     * @see hep.crest.server.swagger.api.AdminApiService#updateGlobalTag(java.lang.
     * String, hep.crest.swagger.model.GlobalTagDto,
     * jakarta.ws.rs.core.SecurityContext, jakarta.ws.rs.core.UriInfo)
     */
    @Override
    public Response updateGlobalTag(String name, GlobalTagDto body, SecurityContext securityContext) {
        log.info("AdminRestController processing request for updating global tag {} using {}", name, body);
        // Update the global tag identified by name. Set the type to N (normal).
        final char type = body.getType() != null ? body.getType().charAt(0) : 'N';

        // Find the global tag corresponding to input name.
        final GlobalTag entity = globalTagService.findOne(name);

        // Compare fields to set them from the input body object provided by the client.
        if (!entity.getDescription().equals(body.getDescription())) {
            // change description.
            entity.setDescription(body.getDescription());
        }
        if (!entity.getRelease().equals(body.getRelease())) {
            // change release.
            entity.setRelease(body.getRelease());
        }
        if (!entity.getWorkflow().equals(body.getWorkflow())) {
            // change workflow.
            entity.setWorkflow(body.getWorkflow());
        }
        if (!entity.getScenario().equals(body.getScenario())) {
            // change scenario.
            entity.setScenario(body.getScenario());
        }
        if (entity.getType() != type) {
            // change type.
            entity.setType(type);
        }
        // Update the global tag.
        final GlobalTag saved = globalTagService.updateGlobalTag(entity);
        final GlobalTagDto dto = mapper.toDto(saved);
        return Response.ok().entity(dto).build();
    }
}
