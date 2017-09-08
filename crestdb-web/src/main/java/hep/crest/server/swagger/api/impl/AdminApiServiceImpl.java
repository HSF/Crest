package hep.crest.server.swagger.api.impl;

import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.server.services.GlobalTagService;
import hep.crest.server.services.TagService;
import hep.crest.server.swagger.api.*;
import hep.crest.swagger.model.*;

import hep.crest.swagger.model.GlobalTagDto;

import java.util.List;
import java.io.InputStream;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.constraints.*;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-09-05T16:23:23.401+02:00")
@Component
public class AdminApiServiceImpl extends AdminApiService {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private GlobalTagService globalTagService;
		
	@Autowired
	private TagService tagService;

    @Override
    public Response removeGlobalTag(String name, SecurityContext securityContext, UriInfo info) throws NotFoundException {
		log.info("AdminRestController processing request for removing a global tag");
		try {
			globalTagService.removeGlobalTag(name);
			return Response.ok().build();
		} catch (CdbServiceException e) {
			String msg = "Error removing globaltag resource using " + name;
			ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
			return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
		}
    }
    
    @Override
    public Response removeTag(String name, SecurityContext securityContext, UriInfo info) throws NotFoundException {
		log.info("AdminRestController processing request for removing a tag");
		try {
			tagService.removeTag(name);
			return Response.ok().build();
		} catch (CdbServiceException e) {
			String msg = "Error removing tag resource using " + name;
			ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
			return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
		}
    }
    
    @Override
    public Response updateGlobalTag(String name, GlobalTagDto body, SecurityContext securityContext, UriInfo info) throws NotFoundException {
		log.info("AdminRestController processing request for updating a global tag");
		try {
			GlobalTagDto dtoentity = globalTagService.findOne(name);
			if (dtoentity.getDescription() != body.getDescription()) {
				dtoentity.setDescription(body.getDescription());
			}
			if (dtoentity.getRelease() != body.getRelease()) {
				dtoentity.setRelease(body.getRelease());
			}
			if (dtoentity.getWorkflow() != body.getWorkflow()) {
				dtoentity.setWorkflow(body.getWorkflow());
			}
			if (dtoentity.getScenario() != body.getScenario()) {
				dtoentity.setScenario(body.getScenario());
			}
			if (dtoentity.getType() != body.getType()) {
				dtoentity.setType(body.getType());
			}
			GlobalTagDto saved = globalTagService.insertGlobalTag(dtoentity);
			return Response.ok().entity(saved).build();
			
		} catch (Exception e) {
			String msg = "Error updating GlobalTag resource using "+body;
			ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
			return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
		}
    }
}
