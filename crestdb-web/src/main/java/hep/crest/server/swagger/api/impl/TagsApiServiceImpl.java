package hep.crest.server.swagger.api.impl;

import java.math.BigDecimal;
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
import hep.crest.server.services.TagService;
import hep.crest.server.swagger.api.ApiResponseMessage;
import hep.crest.server.swagger.api.NotFoundException;
import hep.crest.server.swagger.api.TagsApiService;
import hep.crest.swagger.model.GenericMap;
import hep.crest.swagger.model.TagDto;
import hep.crest.swagger.model.TagMetaDto;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-09-05T16:23:23.401+02:00")
@Component
public class TagsApiServiceImpl extends TagsApiService {
	
	
	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	PageRequestHelper prh;

	@Autowired
	@Qualifier("tagFiltering")
	private IFilteringCriteria filtering;

	@Autowired
	TagService tagService;
	
    @Override
    public Response createTag(TagDto body, SecurityContext securityContext, UriInfo info) throws NotFoundException {
   		log.info("TagRestController processing request for creating a tag");
		try {
			TagDto saved = tagService.insertTag(body);
			return Response.created(info.getRequestUri()).entity(saved).build();

		} catch (AlreadyExistsPojoException e) {
			return Response.status(Response.Status.SEE_OTHER).entity(body).build();
		} catch (CdbServiceException e) {
			String message = e.getMessage();
			ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,message);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
		}
    }
    
    
    @Override
	public Response updateTag(String name, GenericMap body, SecurityContext securityContext, UriInfo info)
			throws NotFoundException {
    	log.info("TagRestController processing request for creating a tag");
		try {
			TagDto dto = tagService.findOne(name);
			if (dto == null) {
				log.debug("Cannot update null tag...."+name);
				String message = ("Tag "+name+" not found...");
				ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,message);
				return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
			}
			for (String  key : body.keySet()) {
				if (key == "description") {
					dto.setDescription(body.get(key));
				}
				if (key == "timeType") {
					dto.setTimeType(body.get(key));
				}
				if (key == "lastValidatedTime") {
					BigDecimal val = new BigDecimal(body.get(key));
					dto.setLastValidatedTime(val);
				}
				if (key == "endOfValidity") {
					BigDecimal val = new BigDecimal(body.get(key));
					dto.setEndOfValidity(val);
				}
				if (key == "payloadSpec") {
					dto.setPayloadSpec(body.get(key));
				}
			}
			TagDto saved = tagService.updateTag(dto);
			return Response.created(info.getRequestUri()).entity(saved).build();

		} catch (CdbServiceException e) {
			String message = e.getMessage();
			ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,message);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
		}
	}


	@Override
    public Response findTag(String name, SecurityContext securityContext, UriInfo info) throws NotFoundException {
		this.log.info("TagRestController processing request for tag name " + name);
		try {
			TagDto dto = tagService.findOne(name);
			if (dto == null) {
				log.debug("Entity not found for name " + name);
				ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,"Entity not found for name "+name);
				return Response.status(Response.Status.NOT_FOUND).entity(resp).build();				
			}
			return Response.ok().entity(dto).build();
		} catch (Exception e) {
			e.printStackTrace();
			String message = e.getMessage();
			ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,message);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
		}
    }
    @Override
    public Response listTags( String by,  Integer page,  Integer size,  String sort, SecurityContext securityContext, UriInfo info) throws NotFoundException {
		try {
			log.debug("Search resource list using by={}, page={}, size={}, sort={}",by,page,size,sort);
			PageRequest preq = prh.createPageRequest(page, size, sort);
			List<TagDto> dtolist = null;
			if (by.equals("none")) {
				dtolist = tagService.findAllTags(null, preq);
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
				dtolist = tagService.findAllTags(wherepred, preq);
			}
			if (dtolist == null) {
				String message = "No resource has been found";
				ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.INFO,message);
				return Response.status(Response.Status.NOT_FOUND).entity(resp).build();	
			}
			GenericEntity<List<TagDto>> entitylist = new GenericEntity<List<TagDto>>(dtolist) {};
			return Response.ok().entity(entitylist).build();

		} catch (CdbServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			String message = e.getMessage();
			ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,message);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
		}
    }


	/* (non-Javadoc)
	 * @see hep.crest.server.swagger.api.TagsApiService#createTagMeta(java.lang.String, hep.crest.swagger.model.TagMetaDto, javax.ws.rs.core.SecurityContext, javax.ws.rs.core.UriInfo)
	 */
	@Override
	public Response createTagMeta(String name, TagMetaDto body, SecurityContext securityContext, UriInfo info)
			throws NotFoundException {
   		log.info("TagRestController processing request for creating a tag meta data entry for {}",name);
		try {
			TagDto tagdto = tagService.findOne(name);
			if (tagdto == null) {
				ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,"Tag entity not found for name "+name);
				return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
			}
			log.debug("Add meta information to tag {}",name);
			TagMetaDto saved = tagService.insertTagMeta(body);
			return Response.created(info.getRequestUri()).entity(saved).build();

		} catch (AlreadyExistsPojoException e) {
			return Response.status(Response.Status.SEE_OTHER).entity(body).build();
		} catch (CdbServiceException e) {
			String message = e.getMessage();
			ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,message);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
		}
	}


	/* (non-Javadoc)
	 * @see hep.crest.server.swagger.api.TagsApiService#findTagMeta(java.lang.String, javax.ws.rs.core.SecurityContext, javax.ws.rs.core.UriInfo)
	 */
	@Override
	public Response findTagMeta(String name, SecurityContext securityContext, UriInfo info) throws NotFoundException {
		this.log.info("TagRestController processing request to find tag metadata for name " + name);
		try {
			TagMetaDto dto = tagService.findMeta(name);
			if (dto == null) {
				log.debug("Entity not found for name " + name);
				ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,"Entity not found for name "+name);
				return Response.status(Response.Status.NOT_FOUND).entity(resp).build();				
			}
			return Response.ok().entity(dto).build();
		} catch (Exception e) {
			e.printStackTrace();
			String message = e.getMessage();
			ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,message);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
		}
	}


	/* (non-Javadoc)
	 * @see hep.crest.server.swagger.api.TagsApiService#updateTagMeta(java.lang.String, hep.crest.swagger.model.GenericMap, javax.ws.rs.core.SecurityContext, javax.ws.rs.core.UriInfo)
	 */
	@Override
	public Response updateTagMeta(String name, GenericMap body, SecurityContext securityContext, UriInfo info)
			throws NotFoundException {
    	log.info("TagRestController processing request for creating a tag");
		try {
			TagMetaDto dto = tagService.findMeta(name);
			if (dto == null) {
				log.debug("Cannot update meta data on null tag meta entity for {}",name);
				String message = ("TagMeta "+name+" not found...");
				ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,message);
				return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
			}
			for (String  key : body.keySet()) {
				if (key == "description") {
					dto.setDescription(body.get(key));
				}
				if (key == "chansize") {
					dto.setChansize(new Integer(body.get(key)));
				}
				if (key == "colsize") {
					dto.setColsize(new Integer(body.get(key)));
				}
				if (key == "channelInfo") {
					byte[] val = body.get(key).getBytes();
					dto.setChannelInfo(new String(val));
				}
				if (key == "payloadInfo") {
					byte[] val = body.get(key).getBytes();
					dto.setPayloadInfo(new String(val));
				}
			}
			TagMetaDto saved = tagService.updateTagMeta(dto);
			return Response.created(info.getRequestUri()).entity(saved).build();

		} catch (CdbServiceException e) {
			String message = e.getMessage();
			ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,message);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
		}
	}
}
