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
import hep.crest.data.repositories.querydsl.IFilteringCriteria;
import hep.crest.data.repositories.querydsl.SearchCriteria;
import hep.crest.data.utils.RunIovConverter;
import hep.crest.server.controllers.PageRequestHelper;
import hep.crest.server.exceptions.AlreadyExistsPojoException;
import hep.crest.server.runinfo.services.RunLumiInfoService;
import hep.crest.server.swagger.api.ApiResponseMessage;
import hep.crest.server.swagger.api.NotFoundException;
import hep.crest.server.swagger.api.RuninfoApiService;
import hep.crest.swagger.model.RunLumiInfoDto;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-11-07T14:29:18.354+01:00")
@Component
public class RuninfoApiServiceImpl extends RuninfoApiService {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	PageRequestHelper prh;

	@Autowired
	@Qualifier("runlumiFiltering")
	private IFilteringCriteria filtering;

	@Autowired
	RunLumiInfoService runlumiService;

	@Override
	public Response createRunLumiInfo(RunLumiInfoDto body, SecurityContext securityContext, UriInfo info)
			throws NotFoundException {
		log.info("RunLumiRestController processing request for creating a run lumi info entry");
		try {
			RunLumiInfoDto saved = runlumiService.insertRunLumiInfo(body);
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
	public Response listRunLumiInfo(String by, Integer page, Integer size, String sort, SecurityContext securityContext,
			UriInfo info) throws NotFoundException {
		try {
			log.debug("Search resource list using by={}, page={}, size={}, sort={}", by, page, size, sort);
			PageRequest preq = prh.createPageRequest(page, size, sort);
			List<RunLumiInfoDto> dtolist = null;
			if (by.equals("none")) {
				dtolist = runlumiService.findAllRunLumiInfo(null, preq);
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
				dtolist = runlumiService.findAllRunLumiInfo(wherepred, preq);
			}
			if (dtolist == null) {
				String message = "No resource has been found";
				ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.INFO, message);
				return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
			}
			GenericEntity<List<RunLumiInfoDto>> entitylist = new GenericEntity<List<RunLumiInfoDto>>(dtolist) {
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

	@Override
	public Response findRunLumiInfo(String from, String to, String format, Integer page, Integer size, String sort,
			SecurityContext securityContext, UriInfo info) throws NotFoundException {
		try {
			log.debug("Search resource list using from={}, to={}, format={}, page={}, size={}, sort={}", from, to,
					format, page, size, sort);
			PageRequest preq = prh.createPageRequest(page, size, sort);
			List<RunLumiInfoDto> dtolist = null;
			String by = "";
			if (format.equals("time")) {
				log.debug("Using from and to as times in yyyymmddhhmiss");
				DateTimeFormatter locFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
				ZonedDateTime zdtfrom = LocalDateTime.parse(from, locFormatter).atZone(ZoneId.of("Z"));
				ZonedDateTime zdtto = LocalDateTime.parse(to, locFormatter).atZone(ZoneId.of("Z"));
				Timestamp tsfrom = new Timestamp(zdtfrom.toInstant().toEpochMilli());
				Timestamp tsto = new Timestamp(zdtto.toInstant().toEpochMilli());
				BigDecimal bfrom = new BigDecimal(tsfrom.getTime() * RunIovConverter.TO_NANOSECONDS);
				BigDecimal bto = new BigDecimal(tsto.getTime() * RunIovConverter.TO_NANOSECONDS);
				by = "starttime>" + bfrom.toString();
				by = by + ",starttime<" + bto.toString();

			} else if (format.equals("run-lumi")) {
				String[] fromarr = from.split("-");
				String[] toarr = to.split("-");
				BigDecimal bfrom = RunIovConverter.getCoolRunLumi(new Long(fromarr[0]), new Long(fromarr[1]));
				BigDecimal bto = RunIovConverter.getCoolRunLumi(new Long(toarr[0]), new Long(toarr[1]));
				by = "since>" + bfrom.toString();
				by = by + ",since<" + bto.toString();
			}

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
			dtolist = runlumiService.findAllRunLumiInfo(wherepred, preq);

			if (dtolist == null) {
				String message = "No resource has been found";
				ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.INFO, message);
				return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
			}
			GenericEntity<List<RunLumiInfoDto>> entitylist = new GenericEntity<List<RunLumiInfoDto>>(dtolist) {
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
