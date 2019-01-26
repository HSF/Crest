package hep.crest.server.swagger.api.impl;

import java.util.List;

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
import hep.crest.server.security.FolderService;
import hep.crest.server.swagger.api.ApiResponseMessage;
import hep.crest.server.swagger.api.FoldersApiService;
import hep.crest.server.swagger.api.NotFoundException;
import hep.crest.swagger.model.FolderDto;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2018-05-10T14:57:11.305+02:00")
@Component
public class FoldersApiServiceImpl extends FoldersApiService {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	PageRequestHelper prh;

	@Autowired
	@Qualifier("folderFiltering")
	private IFilteringCriteria filtering;

	@Autowired
	FolderService folderService;

	@Override
	public Response createFolder(FolderDto body, SecurityContext securityContext, UriInfo info)
			throws NotFoundException {
		log.info("FolderRestController processing request for creating a folder");
		try {
			FolderDto saved = folderService.insertFolder(body);
			return Response.created(info.getRequestUri()).entity(saved).build();
		} catch (AlreadyExistsPojoException e) {
			return Response.status(Response.Status.SEE_OTHER).entity(body).build();
		} catch (CdbServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			String message = e.getMessage();
			ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, message);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
		}
	}

	@Override
	public Response listFolders(String by, String sort, SecurityContext securityContext, UriInfo info)
			throws NotFoundException {
		try {
			log.debug("Search resource list using by={}, sort={}",by,sort);
			PageRequest preq = prh.createPageRequest(0, 10000, sort);
			List<FolderDto> dtolist = null;
			if (by.equals("none")) {
				dtolist = folderService.findAllFolders(null, preq);
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
				dtolist = folderService.findAllFolders(wherepred, preq);
			}
			if (dtolist == null) {
				String message = "No resource has been found";
				ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.INFO,message);
				return Response.status(Response.Status.NOT_FOUND).entity(resp).build();	
			}
			GenericEntity<List<FolderDto>> entitylist = new GenericEntity<List<FolderDto>>(dtolist) {};
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
