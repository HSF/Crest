package hep.crest.server.swagger.api.impl;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
import hep.crest.data.monitoring.repositories.IMonitoringRepository;
import hep.crest.data.repositories.querydsl.IFilteringCriteria;
import hep.crest.data.repositories.querydsl.SearchCriteria;
import hep.crest.data.utils.RunIovConverter;
import hep.crest.server.controllers.PageRequestHelper;
import hep.crest.server.exceptions.AlreadyExistsPojoException;
import hep.crest.server.runinfo.services.RunLumiInfoService;
import hep.crest.server.swagger.api.ApiResponseMessage;
import hep.crest.server.swagger.api.MonitoringApiService;
import hep.crest.server.swagger.api.NotFoundException;
import hep.crest.server.swagger.api.RuninfoApiService;
import hep.crest.swagger.model.PayloadTagInfoDto;
import hep.crest.swagger.model.RunLumiInfoDto;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-11-07T14:29:18.354+01:00")
@Component
public class MonitoringApiServiceImpl extends MonitoringApiService {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	PageRequestHelper prh;

	@Autowired
	@Qualifier("runlumiFiltering")
	private IFilteringCriteria filtering;

	@Autowired
	IMonitoringRepository monitoringrepo;


	@Override
	public Response listPayloadTagInfo(String tagname, SecurityContext securityContext,
			UriInfo info) throws NotFoundException {
		try {
			log.debug("Search resource list using tagname or pattern={}", tagname);
			List<PayloadTagInfoDto> dtolist = null;
			String tagpattern=tagname;
			if (tagpattern.equals("none")) {
				tagpattern="%";
			} else {
				tagpattern="%"+tagpattern+"%";
			}
			dtolist = monitoringrepo.selectTagInfo(tagpattern);
			
			if (dtolist == null) {
				String message = "No resource has been found";
				ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.INFO, message);
				return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
			}
			GenericEntity<List<PayloadTagInfoDto>> entitylist = new GenericEntity<List<PayloadTagInfoDto>>(dtolist) {
			};
			return Response.ok().entity(entitylist).build();

		} catch (CdbServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			String message = e.getMessage();
			ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, message);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
		}
	}

}
