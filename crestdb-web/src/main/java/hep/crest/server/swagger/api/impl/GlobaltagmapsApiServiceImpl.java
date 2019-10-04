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

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-09-05T16:23:23.401+02:00")
@Component
public class GlobaltagmapsApiServiceImpl extends GlobaltagmapsApiService {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	GlobalTagMapService globaltagmapService;

	@Override
	public Response createGlobalTagMap(GlobalTagMapDto body, SecurityContext securityContext, UriInfo info)
			throws NotFoundException {
		this.log.info("GlobalTagMapRestController processing request for creating a global tag map entry " + body);
		try {
			GlobalTagMapDto saved = globaltagmapService.insertGlobalTagMap(body);
			return Response.created(info.getRequestUri()).entity(saved).build();

		} catch (CdbServiceException e) {
			String msg = "Error creating globaltagmap resource using " + body.toString();
			e.printStackTrace();
			String message = e.getMessage();
			ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg + " : " + message);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
		}
	}

	@Override
	public Response findGlobalTagMap(String name, String xCrestMapMode, SecurityContext securityContext, UriInfo info)
			throws NotFoundException {
		this.log.info("GlobalTagMapRestController processing request to get map for GlobalTag name " + name);
		try {
			List<GlobalTagMapDto> dtolist = null;
			if (xCrestMapMode != null & xCrestMapMode.equals("Trace")) {
				dtolist = globaltagmapService.getTagMap(name);
			} else {
				dtolist = globaltagmapService.getTagMapByTagName(name);
			}
			GenericMap filters = new GenericMap();
			filters.put("name", name);
			filters.put("mode", xCrestMapMode);
			CrestBaseResponse setdto = new GlobalTagMapSetDto().resources(dtolist).filter(filters).size((long)dtolist.size()).datatype("maps");
			return Response.ok().entity(setdto).build();

		} catch (CdbServiceException e) {
			String message = e.getMessage();
			log.error("Internal error searching maps : {}",message);
			ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, message);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
		}
	}
}
