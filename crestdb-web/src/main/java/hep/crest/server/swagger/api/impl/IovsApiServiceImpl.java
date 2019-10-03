package hep.crest.server.swagger.api.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import com.querydsl.core.types.dsl.BooleanExpression;

import hep.crest.data.config.CrestProperties;
import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.repositories.querydsl.IFilteringCriteria;
import hep.crest.data.repositories.querydsl.SearchCriteria;
import hep.crest.server.caching.CachingPolicyService;
import hep.crest.server.caching.CachingProperties;
import hep.crest.server.controllers.PageRequestHelper;
import hep.crest.server.services.IovService;
import hep.crest.server.services.TagService;
import hep.crest.server.swagger.api.ApiResponseMessage;
import hep.crest.server.swagger.api.IovsApiService;
import hep.crest.server.swagger.api.NotFoundException;
import hep.crest.swagger.model.CrestBaseResponse;
import hep.crest.swagger.model.GenericMap;
import hep.crest.swagger.model.GroupDto;
import hep.crest.swagger.model.IovDto;
import hep.crest.swagger.model.IovSetDto;
import hep.crest.swagger.model.TagDto;
import hep.crest.swagger.model.TagSummaryDto;
import hep.crest.swagger.model.TagSummarySetDto;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-09-05T16:23:23.401+02:00")
@Component
public class IovsApiServiceImpl extends IovsApiService {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	PageRequestHelper prh;

	@Autowired
	@Qualifier("iovFiltering")
	private IFilteringCriteria filtering;

	@Autowired
	private CachingPolicyService cachesvc;

	@Autowired
	IovService iovService;
	
	@Autowired
	TagService tagService;

	@Autowired
	private CachingProperties cprops;

	/* (non-Javadoc)
	 * @see hep.crest.server.swagger.api.IovsApiService#createIov(hep.crest.swagger.model.IovDto, javax.ws.rs.core.SecurityContext, javax.ws.rs.core.UriInfo)
	 */
	@Override
	public Response createIov(IovDto body, SecurityContext securityContext, UriInfo info) throws NotFoundException {
		log.info("IovRestController processing request for creating an iov");
		try {
			IovDto saved = iovService.insertIov(body);
			return Response.created(info.getRequestUri()).entity(saved).build();

		} catch (CdbServiceException e) {
			log.error("Exception in creating iov : {}",e.getMessage());
			String message = e.getMessage();
			ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, message);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
		}
	}

	
	/* (non-Javadoc)
	 * @see hep.crest.server.swagger.api.IovsApiService#storeBatchIovMultiForm(hep.crest.swagger.model.IovSetDto, javax.ws.rs.core.SecurityContext, javax.ws.rs.core.UriInfo)
	 */
	@Override
	public Response storeBatchIovMultiForm(IovSetDto dto, SecurityContext securityContext, UriInfo info)
			throws NotFoundException {
		this.log.info("IovRestController processing request to upload iovs batch {}",
				dto);
		GenericMap filters = dto.getFilter();
		if (!filters.containsKey("tagName")) {
			String msg = "Error creating multi iov resource because tagName is not defined ";
			ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
			return Response.status(Response.Status.NOT_ACCEPTABLE).entity(resp).build();
		}
		String tagName = filters.get("tagName");
		try {
			log.info("Batch insertion of {} iovs using file formatted in {} for tag {}", dto.getSize(), dto.getFormat(),tagName);
			List<IovDto> iovlist = dto.getResources();
			List<IovDto> savedList = new ArrayList<>();
			for (IovDto iovDto : iovlist) {
				if (iovDto.getTagName() == null || !(iovDto.getTagName().equals(tagName))) {
					iovDto.setTagName(tagName);
				}
				IovDto saved = iovService.insertIov(iovDto);
				savedList.add(saved);
			}
			CrestBaseResponse saveddto = new IovSetDto().resources(savedList).filter(filters).size((long)savedList.size()).datatype("iovs");
			return Response.created(info.getRequestUri()).entity(saveddto).build();

		} catch (CdbServiceException e) {
			String msg = "Error creating multi iov resource using " + tagName + " : " + e.getMessage();
			ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
		} catch (Exception e) {
			String msg = "Internal exception creating payload resource using uploadBatch " + tagName + " : "
					+ e.getMessage();
			ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
		}
	}


	/* (non-Javadoc)
	 * @see hep.crest.server.swagger.api.IovsApiService#findAllIovs(java.lang.String, java.lang.Integer, java.lang.Integer, java.lang.String, javax.ws.rs.core.SecurityContext, javax.ws.rs.core.UriInfo)
	 */
	@Override
	public Response findAllIovs(String by, Integer page, Integer size, String sort, String dateformat,
			SecurityContext securityContext, UriInfo info) throws NotFoundException {
		try {
			log.debug("Search resource list using by={}, page={}, size={}, sort={}", by, page, size, sort);
			
			if (dateformat == null) {
				dateformat = "ms";
			}
			List<SearchCriteria> params = null;
			PageRequest preq = prh.createPageRequest(page, size, sort);
			List<IovDto> dtolist = null;
			if (!by.matches("(.*)tag.ame(.*)")) {
				String message = "Cannot search iovs without a tagname selection.";
				ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, message);
				return Response.status(Response.Status.NOT_ACCEPTABLE).entity(resp).build();
			} else {
				
				params = prh.createMatcherCriteria(by,dateformat);
				List<BooleanExpression> expressions = filtering.createFilteringConditions(params);
				BooleanExpression wherepred = null;

				for (BooleanExpression exp : expressions) {
					if (wherepred == null) {
						wherepred = exp;
					} else {
						wherepred = wherepred.and(exp);
					}
				}
				dtolist = iovService.findAllIovs(wherepred, preq);
			}
			if (dtolist == null) {
				String message = "No resource has been found";
				ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.INFO, message);
				return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
			}
			String tagname = prh.getParam(params, "tagname");
			GenericMap filters = new GenericMap();
			filters.put("tagName", tagname);
			CrestBaseResponse saveddto = new IovSetDto().resources(dtolist).filter(filters).size((long)dtolist.size()).datatype("iovs");

			//GenericEntity<List<IovDto>> entitylist = new GenericEntity<List<IovDto>>(dtolist) {};
			return Response.ok().entity(saveddto).build();

		} catch (CdbServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			String message = e.getMessage();
			ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, message);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
		}
	}

	/* (non-Javadoc)
	 * @see hep.crest.server.swagger.api.IovsApiService#getSize(java.lang.String, java.lang.Long, javax.ws.rs.core.SecurityContext, javax.ws.rs.core.UriInfo)
	 */
	@Override
	public Response getSize(@NotNull String tagname, Long snapshot, SecurityContext securityContext, UriInfo info)
			throws NotFoundException {
		try {
			Long size = 0L;
			if (snapshot != 0L) {
				Date snap = null;
				snap = new Date(snapshot);
				size = iovService.getSizeByTagAndSnapshot(tagname, snap);
			} else {
				size = iovService.getSizeByTag(tagname);
			}
			CrestBaseResponse respdto = new CrestBaseResponse();
			GenericMap filters = new GenericMap();
			filters.put("tagName", tagname);
			filters.put("snapshot", snapshot.toString());
			respdto.size(size).datatype("count").filter(filters);
			return Response.ok().entity(respdto).build();

		} catch (Exception e) {
			log.error("Error in getting size for tagname {} : {}",tagname,e.getMessage());
			String message = e.getMessage();
			ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, message);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
		}
	}

	@Override
	public Response getSizeByTag(@NotNull String tagname, SecurityContext securityContext, UriInfo info)
			throws NotFoundException {
		try {
			List<TagSummaryDto> entitylist = iovService.getTagSummaryInfo(tagname);
			TagSummarySetDto respdto = new TagSummarySetDto();
			GenericMap filters = new GenericMap();
			filters.put("tagName", tagname);
			if (entitylist == null) {
				String message = "No resource has been found";
				ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.INFO, message);
				return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
			}
			((TagSummarySetDto)respdto.size((long) entitylist.size()).datatype("count").filter(filters)).resources(entitylist);
			
			return Response.ok().entity(respdto).build();

		} catch (Exception e) {
			log.error("Error in getting size by tagname {} : {}",tagname,e.getMessage());
			String message = e.getMessage();
			ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, message);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
		}
	}

	@Override
	public Response selectGroups(@NotNull String tagname, Long snapshot, SecurityContext securityContext, UriInfo info,
			Request request, HttpHeaders headers) throws NotFoundException {
		this.log.info("IovRestController processing request for iovs groups using tag name {}",tagname);
		try {
			GroupDto groups = null;
			// Integer maxage = 60;
			// Search for tag in order to load the time type:
			TagDto tagentity = tagService.findOne(tagname);
			if (tagentity == null) {
				throw new CdbServiceException("Cannot find tag for name "+tagname);
			}
			ResponseBuilder builder = cachesvc.verifyLastModified(request, tagentity);
			if (builder != null) {
				// Get request headers: this is just to dump the If-Modified-Since
				String ifmodsince = headers.getHeaderString("If-Modified-Since");
				log.debug("The output data are not modified since " + ifmodsince);
				return builder.build();
			}
			Long groupsize = null;
			String timetype = tagentity.getTimeType();
			if (timetype.equalsIgnoreCase("run")) {
				groupsize = new Long(cprops.getRuntypeGroupsize());
			} else if (timetype.equalsIgnoreCase("run-lumi")) {
				groupsize = new Long(cprops.getRuntypeGroupsize());
				groupsize = groupsize * 4294967296L; // transform to COOL run-lumi
			} else {
				// Assume COOL time format...
				groupsize = new Long(cprops.getTimetypeGroupsize());
				groupsize = groupsize * 1000000000L; // transform to COOL nanosec
			}
			// Set caching policy depending on snapshot argument
			// this is filling a mag-age parameter in the header
			CacheControl cc = cachesvc.getGroupsCacheControl(snapshot);
			// Retrieve all iovs groups
			Date snap = null;
			if (snapshot != 0L) {
				snap = new Date(snapshot);
			}
			groups = iovService.selectGroupDtoByTagNameAndSnapshotTime(tagname, snap, groupsize);
			return Response.ok().entity(groups).cacheControl(cc).build();

		} catch (Exception e) {
			String msg = "Error in selectGroups : " + tagname + ", " + snapshot;
			log.error("Exception catched by REST controller for {} : {}", msg, e.getMessage());
			String message = msg + " -- " + e.getMessage();
			ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, message);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
		}

	}

	@Override
	public Response selectIovs(String tagname, String since, String until, Long snapshot,
			SecurityContext securityContext, UriInfo info,Request request, HttpHeaders headers) throws NotFoundException {
		log.info("IovRestController processing request for iovs using tag name {} and range {} - {} ",tagname,since,until);
		try {
			List<IovDto> dtolist = null;
			// Retrieve all iovs 
			TagDto tagentity = tagService.findOne(tagname);
			if (tagentity == null) {
				throw new CdbServiceException("Cannot find tag for name " + tagname);
			}
			log.debug("Found tag "+tagentity);
			ResponseBuilder builder = cachesvc.verifyLastModified(request, tagentity);
			if (builder != null) {
				// Get request headers: this is just to dump the If-Modified-Since
				String ifmodsince = headers.getHeaderString("If-Modified-Since");
				log.debug("The output data are not modified since " + ifmodsince);
				return builder.build();
			}

			log.debug("Setting iov range to : {}, {}",since, until);
			BigDecimal runtil = null;
			if (until.equals("INF")) {
				log.debug("The end time will be set to : {}", CrestProperties.INFINITY);
				runtil = CrestProperties.INFINITY;
			} else {
				runtil = new BigDecimal(until);
				log.debug("The end time will be set to : " + runtil);
			}
			BigDecimal rsince = new BigDecimal(since);
			Date snap = null;
			if (snapshot != 0L) {
				snap = new Date(snapshot);
			}
			// Set caching policy depending on snapshot argument
			// this is filling a mag-age parameter in the header
			CacheControl cc = cachesvc.getIovsCacheControlForUntil(snapshot, runtil);
			// Retrieve all iovs
			dtolist = iovService.selectIovsByTagRangeSnapshot(tagname, rsince, runtil, snap);
			return Response.ok().entity(dtolist).cacheControl(cc).build();
		} catch (Exception e) {
			String msg = "Error in selectIovs : " + tagname + ", " + snapshot;
			log.error("Exception catched by REST controller for {} : {}", msg, e.getMessage());
			String message = msg + " -- " + e.getMessage();
			ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, message);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
		}
	}

	/* (non-Javadoc)
	 * @see hep.crest.server.swagger.api.IovsApiService#selectSnapshot(java.lang.String, java.lang.Long, javax.ws.rs.core.SecurityContext, javax.ws.rs.core.UriInfo)
	 */
	@Override
	public Response selectSnapshot(@NotNull String tagname, @NotNull Long snapshot, SecurityContext securityContext,
			UriInfo info) throws NotFoundException {
		try {
			Date snap = new Date();
			if (snapshot != 0L) {
				snap = new Date(snapshot);
			}
			List<IovDto> entitylist = iovService.selectSnapshotByTag(tagname, snap);
			return Response.ok().entity(entitylist).build();
		} catch (Exception e) {
			String msg = "Error in selectSnapshot : " + tagname + ", " + snapshot;
			log.error("Exception catched by REST controller for {} : {}", msg, e.getMessage());
			String message = msg + " -- " + e.getMessage();
			ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, message);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
		}
	}
}
