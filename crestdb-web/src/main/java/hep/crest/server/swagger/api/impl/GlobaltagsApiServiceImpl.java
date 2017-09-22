package hep.crest.server.swagger.api.impl;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.core.GenericEntity;
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
import hep.crest.server.exceptions.EmptyPojoException;
import hep.crest.server.services.GlobalTagService;
import hep.crest.server.swagger.api.ApiResponseMessage;
import hep.crest.server.swagger.api.GlobaltagsApiService;
import hep.crest.server.swagger.api.NotFoundException;
import hep.crest.swagger.model.GlobalTagDto;
import hep.crest.swagger.model.TagDto;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-09-05T16:23:23.401+02:00")
@Component
public class GlobaltagsApiServiceImpl extends GlobaltagsApiService {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	PageRequestHelper prh;

	@Autowired
	@Qualifier("globalTagFiltering")
	private IFilteringCriteria filtering;

	@Autowired
	GlobalTagService globaltagService;
	
    @Override
    public Response createGlobalTag(GlobalTagDto body,  String force, SecurityContext securityContext, UriInfo info) throws NotFoundException {
    		log.info("GlobalTagRestController processing request for creating a global tag");
		try {
			if (force.equals("false")) {
				body.setInsertionTime(null);
			}
			GlobalTagDto saved = globaltagService.insertGlobalTag(body);
			return Response.created(info.getRequestUri()).entity(saved).build();
		} catch (AlreadyExistsPojoException e) {
			return Response.status(Response.Status.SEE_OTHER).entity(body).build();
		} catch (CdbServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			String message = e.getMessage();
			ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,message);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
		}
	}
    @Override
    public Response findGlobalTag(String name, SecurityContext securityContext, UriInfo info) throws NotFoundException {
    		this.log.info("GlobalTagRestController processing request for global tag name " + name);
		try {
			GlobalTagDto dto = globaltagService.findOne(name); 
			if (dto != null) {
				log.debug("Found GlobalTag " + name);
			} else {
				String message = "Global tag "+name+" not found...";
				ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,message);
				return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
			}
			return Response.ok().entity(dto).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			String message = e.getMessage();
			ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,message);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
		}
	}
    
    @Override
    public Response findGlobalTagFetchTags(String name,  String record,  String label, SecurityContext securityContext, UriInfo info) throws NotFoundException {
		try {
			this.log.info("GlobalTagRestController processing request for global tag name " + name);
			List<TagDto> dtolist = globaltagService.getGlobalTagByNameFetchTags(name,record,label);
			log.debug("Found list of tags of length "+((dtolist==null) ? "0" : dtolist.size()));
			GenericEntity<List<TagDto>> entitylist = new GenericEntity<List<TagDto>>(dtolist) {};
			return Response.ok().entity(entitylist).build();
		} catch (EmptyPojoException e) {
			String message = "No tags are associated to "+name;
			ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,message);
			return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
		} catch (CdbServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			String message = e.getMessage();
			ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,message);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
		}
    }
    
    @Override
    public Response listGlobalTags( String by,  Integer page,  Integer size,  String sort, SecurityContext securityContext, UriInfo info) throws NotFoundException {
		try {
			log.debug("Search resource list using by={}, page={}, size={}, sort={}",by,page,size,sort);
			PageRequest preq = prh.createPageRequest(page, size, sort);
			List<GlobalTagDto> dtolist = null;
			if (by.equals("none")) {
				dtolist = globaltagService.findAllGlobalTags(null, preq);
			} else {

				List<SearchCriteria> params = prh.createMatcherCriteria(by);
				List<BooleanExpression> expressions = filtering.createFilteringConditions(params);
				BooleanExpression wherepred = null;

				for (BooleanExpression exp : expressions) {
					if (wherepred == null) {
						wherepred = exp;
					} else {
						wherepred = wherepred.and(exp);
					}
				}
				dtolist = globaltagService.findAllGlobalTags(wherepred, preq);
			}
			if (dtolist == null) {
				String message = "No resource has been found";
				ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.INFO,message);
				return Response.status(Response.Status.NOT_FOUND).entity(resp).build();	
			}
			GenericEntity<List<GlobalTagDto>> entitylist = new GenericEntity<List<GlobalTagDto>>(dtolist) {};
			return Response.ok().entity(entitylist).build();

		} catch (CdbServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			String message = e.getMessage();
			ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,message);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
		}
    }
}
