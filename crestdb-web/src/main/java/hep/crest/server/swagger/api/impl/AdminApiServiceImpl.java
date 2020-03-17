package hep.crest.server.swagger.api.impl;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.server.exceptions.NotExistsPojoException;
import hep.crest.server.services.GlobalTagService;
import hep.crest.server.services.TagService;
import hep.crest.server.swagger.api.AdminApiService;
import hep.crest.server.swagger.api.ApiResponseMessage;
import hep.crest.server.swagger.api.NotFoundException;
import hep.crest.swagger.model.GlobalTagDto;

/**
 * Rest endpoint for administration task. 
 * Essentially allows to remove resources.
 *
 * @author formica
 *
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-09-05T16:23:23.401+02:00")
@Component
public class AdminApiServiceImpl extends AdminApiService {
	
	/**
	 * Logger.
	 */
	private static final Logger log = LoggerFactory.getLogger( AdminApiServiceImpl.class);

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

    /* (non-Javadoc)
     * @see hep.crest.server.swagger.api.AdminApiService#removeGlobalTag(java.lang.String, javax.ws.rs.core.SecurityContext, javax.ws.rs.core.UriInfo)
     */
    @Override
    public Response removeGlobalTag(String name, SecurityContext securityContext, UriInfo info) throws NotFoundException {
		log.info("AdminRestController processing request for removing a global tag");
		try {
		    // Remove the global tag identified by name.
			globalTagService.removeGlobalTag(name);
			return Response.ok().build();
		} catch (final CdbServiceException e) {
			final String msg = "Error removing globaltag resource using " + name;
			final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
			return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
		}
    }
    
    /* (non-Javadoc)
     * @see hep.crest.server.swagger.api.AdminApiService#removeTag(java.lang.String, javax.ws.rs.core.SecurityContext, javax.ws.rs.core.UriInfo)
     */
    @Override
    public Response removeTag(String name, SecurityContext securityContext, UriInfo info) throws NotFoundException {
		log.info("AdminRestController processing request for removing a tag");
		try {
		    // Remove the tag with name.
			tagService.removeTag(name);
			return Response.ok().build();
		} catch (final CdbServiceException e) {
			final String msg = "Error removing tag resource using " + name + " : "+e.getMessage();
			final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
			return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
		}
    }
    
    /* (non-Javadoc)
     * @see hep.crest.server.swagger.api.AdminApiService#updateGlobalTag(java.lang.String, hep.crest.swagger.model.GlobalTagDto, javax.ws.rs.core.SecurityContext, javax.ws.rs.core.UriInfo)
     */
    @Override
    public Response updateGlobalTag(String name, GlobalTagDto body, SecurityContext securityContext, UriInfo info) throws NotFoundException {
		log.info("AdminRestController processing request for updating a global tag using "+body);
		try {
		    // Find the global tag corresponding to input name.
			final GlobalTagDto dtoentity = globalTagService.findOne(name);
			// Compare fields to set them from the input body object provided by the client.
			if (dtoentity.getDescription() != body.getDescription()) {
	            // change description.
				dtoentity.setDescription(body.getDescription());
			}
			if (dtoentity.getRelease() != body.getRelease()) {
                // change release.
				dtoentity.setRelease(body.getRelease());
			}
			if (dtoentity.getWorkflow() != body.getWorkflow()) {
                // change workflow.
				dtoentity.setWorkflow(body.getWorkflow());
			}
			if (dtoentity.getScenario() != body.getScenario()) {
                // change scenario.
				dtoentity.setScenario(body.getScenario());
			}
			if (dtoentity.getType() != body.getType()) {
                // change type.
				dtoentity.setType(body.getType());
			}
			// Update the global tag.
			final GlobalTagDto saved = globalTagService.updateGlobalTag(dtoentity);
			return Response.ok().entity(saved).build();
			
		} catch (final NotExistsPojoException e) {
			final String msg = "Error updating GlobalTag resource using "+body;
			final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
			return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
		} catch (final Exception e) {
			final String msg = "Error updating GlobalTag resource using "+body;
			final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
		}
    }
}
