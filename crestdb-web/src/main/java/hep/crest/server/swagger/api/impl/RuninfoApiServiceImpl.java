package hep.crest.server.swagger.api.impl;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
import hep.crest.server.runinfo.services.RunLumiInfoService;
import hep.crest.server.swagger.api.ApiResponseMessage;
import hep.crest.server.swagger.api.NotFoundException;
import hep.crest.server.swagger.api.RuninfoApiService;
import hep.crest.swagger.model.CrestBaseResponse;
import hep.crest.swagger.model.GenericMap;
import hep.crest.swagger.model.RunLumiInfoDto;
import hep.crest.swagger.model.RunLumiSetDto;

/**
 * @author formica
 *
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2017-11-07T14:29:18.354+01:00")
@Component
public class RuninfoApiServiceImpl extends RuninfoApiService {

    /**
     * Logger.
     */
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Helper.
     */
    @Autowired
    private PageRequestHelper prh;

    /**
     * Filtering.
     */
    @Autowired
    @Qualifier("runlumiFiltering")
    private IFilteringCriteria filtering;

    /**
     * Service.
     */
    @Autowired
    private RunLumiInfoService runlumiService;

    /*
     * (non-Javadoc)
     *
     * @see
     * hep.crest.server.swagger.api.RuninfoApiService#createRunLumiInfo(hep.crest.
     * swagger.model.RunLumiInfoDto, javax.ws.rs.core.SecurityContext,
     * javax.ws.rs.core.UriInfo)
     */
    @Override
    public Response createRunLumiInfo(RunLumiInfoDto body, SecurityContext securityContext,
            UriInfo info) throws NotFoundException {
        log.info(
                "RunLumiRestController processing request for creating a run lumi info entry using "
                        + body);
        try {
            final RunLumiInfoDto saved = runlumiService.insertRunLumiInfo(body);
            return Response.created(info.getRequestUri()).entity(saved).build();
        }
        catch (final CdbServiceException e) {
            final String message = e.getMessage();
            log.error("Api method createRunLumiInfo got exception : {}", message);
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,
                    message);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * hep.crest.server.swagger.api.RuninfoApiService#listRunLumiInfo(java.lang.
     * String, java.lang.Integer, java.lang.Integer, java.lang.String,
     * javax.ws.rs.core.SecurityContext, javax.ws.rs.core.UriInfo)
     */
    @Override
    public Response listRunLumiInfo(String by, Integer page, Integer size, String sort,
            SecurityContext securityContext, UriInfo info) throws NotFoundException {
        try {
            log.debug("Search resource list using by={}, page={}, size={}, sort={}", by, page, size,
                    sort);
            final PageRequest preq = prh.createPageRequest(page, size, sort);
            List<SearchCriteria> params = null;
            final GenericMap filters = new GenericMap();

            List<RunLumiInfoDto> dtolist = null;
            if (by.equals("none")) {
                dtolist = runlumiService.findAllRunLumiInfo(null, preq);
            }
            else {
                params = prh.createMatcherCriteria(by);
                for (final SearchCriteria sc : params) {
                    filters.put(sc.getKey(), sc.getValue().toString());
                }
                final List<BooleanExpression> expressions = filtering
                        .createFilteringConditions(params);
                BooleanExpression wherepred = null;

                for (final BooleanExpression exp : expressions) {
                    if (wherepred == null) {
                        wherepred = exp;
                    }
                    else {
                        wherepred = wherepred.and(exp);
                    }
                }
                dtolist = runlumiService.findAllRunLumiInfo(wherepred, preq);
            }
            if (dtolist == null) {
                final String message = "No resource has been found";
                final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.INFO,
                        message);
                return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
            }
            final CrestBaseResponse setdto = new RunLumiSetDto().resources(dtolist)
                    .size((long) dtolist.size()).datatype("runs");
            if (!filters.isEmpty()) {
                setdto.filter(filters);
            }
            return Response.ok().entity(setdto).build();

        }
        catch (final CdbServiceException e) {
            final String message = e.getMessage();
            log.error("Api method listRunLumiInfo got exception : {}", message);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, message)).build();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * hep.crest.server.swagger.api.RuninfoApiService#findRunLumiInfo(java.lang.
     * String, java.lang.String, java.lang.String, java.lang.Integer,
     * java.lang.Integer, java.lang.String, javax.ws.rs.core.SecurityContext,
     * javax.ws.rs.core.UriInfo)
     */
    @Override
    public Response findRunLumiInfo(String from, String to, String format, Integer page,
            Integer size, String sort, SecurityContext securityContext, UriInfo info)
            throws NotFoundException {
        try {
            log.debug(
                    "Search resource list using from={}, to={}, format={}, page={}, size={}, sort={}",
                    from, to, format, page, size, sort);
            final PageRequest preq = prh.createPageRequest(page, size, sort);
            List<RunLumiInfoDto> dtolist = null;
            List<SearchCriteria> params = null;
            final GenericMap filters = new GenericMap();

            String by = "";
            if (format.equals("time")) {
                log.debug("Using from and to as times in yyyymmddhhmiss");
                final DateTimeFormatter locFormatter = DateTimeFormatter
                        .ofPattern("yyyyMMddHHmmss");
                final ZonedDateTime zdtfrom = LocalDateTime.parse(from, locFormatter)
                        .atZone(ZoneId.of("Z"));
                final ZonedDateTime zdtto = LocalDateTime.parse(to, locFormatter)
                        .atZone(ZoneId.of("Z"));
                final Timestamp tsfrom = new Timestamp(zdtfrom.toInstant().toEpochMilli());
                final Timestamp tsto = new Timestamp(zdtto.toInstant().toEpochMilli());
                final BigDecimal bfrom = new BigDecimal(
                        tsfrom.getTime() * RunIovConverter.TO_NANOSECONDS);
                final BigDecimal bto = new BigDecimal(
                        tsto.getTime() * RunIovConverter.TO_NANOSECONDS);
                by = "starttime>" + bfrom.toString();
                by = by + ",starttime<" + bto.toString();

            }
            else if (format.equals("run-lumi")) {
                final String[] fromarr = from.split("-");
                final String[] toarr = to.split("-");
                final BigDecimal bfrom = RunIovConverter.getCoolRunLumi(new Long(fromarr[0]),
                        new Long(fromarr[1]));
                final BigDecimal bto = RunIovConverter.getCoolRunLumi(new Long(toarr[0]),
                        new Long(toarr[1]));
                by = "since>" + bfrom.toString();
                by = by + ",since<" + bto.toString();
            }

            params = prh.createMatcherCriteria(by);
            for (final SearchCriteria sc : params) {
                filters.put(sc.getKey(), sc.getValue().toString());
            }
            final List<BooleanExpression> expressions = filtering.createFilteringConditions(params);
            BooleanExpression wherepred = null;

            for (final BooleanExpression exp : expressions) {
                if (wherepred == null) {
                    wherepred = exp;
                }
                else {
                    wherepred = wherepred.and(exp);
                }
            }
            dtolist = runlumiService.findAllRunLumiInfo(wherepred, preq);

            if (dtolist == null) {
                final String message = "No resource has been found";
                final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.INFO,
                        message);
                return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
            }
            final CrestBaseResponse setdto = new RunLumiSetDto().resources(dtolist)
                    .size((long) dtolist.size()).datatype("runs");
            if (!filters.isEmpty()) {
                setdto.filter(filters);
            }
            return Response.ok().entity(setdto).build();

        }
        catch (final CdbServiceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            final String message = e.getMessage();
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR,
                    message);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
        }
    }

}
