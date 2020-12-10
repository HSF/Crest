package hep.crest.server.swagger.api.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.repositories.querydsl.IFilteringCriteria;
import hep.crest.data.repositories.querydsl.SearchCriteria;
import hep.crest.server.controllers.PageRequestHelper;
import hep.crest.server.runinfo.services.RunInfoService;
import hep.crest.server.swagger.api.ApiResponseMessage;
import hep.crest.server.swagger.api.NotFoundException;
import hep.crest.server.swagger.api.RuninfoApiService;
import hep.crest.swagger.model.CrestBaseResponse;
import hep.crest.swagger.model.GenericMap;
import hep.crest.swagger.model.RunLumiInfoDto;
import hep.crest.swagger.model.RunLumiSetDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Rest endpoint for run information.
 *
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
    @Qualifier("runFiltering")
    private IFilteringCriteria filtering;

    /**
     * Service.
     */
    @Autowired
    private RunInfoService runinfoService;

    /**
     * Response helper.
     */
    @Autowired
    private ResponseFormatHelper rfh;

    /*
     * (non-Javadoc)
     *
     * @see hep.crest.server.swagger.api.RuninfoApiService#createRunLumiInfo(hep.crest.
     * swagger.model.RunLumiSetDto, javax.ws.rs.core.SecurityContext,
     * javax.ws.rs.core.UriInfo)
     */
    @Override
    public Response createRunInfo(RunLumiSetDto body, SecurityContext securityContext, UriInfo info)
            throws NotFoundException {
        log.info("RunInfoRestController processing request for creating a run info entry using "
                + body);
        try {
            // Create a list of resources
            final List<RunLumiInfoDto> dtolist = body.getResources();
            final List<RunLumiInfoDto> savedlist = new ArrayList<>();
            for (final RunLumiInfoDto runInfoDto : dtolist) {
                // Create a RunInfo resource.
                final RunLumiInfoDto saved = runinfoService.insertRunInfo(runInfoDto);
                // Add to the saved list for the response.
                savedlist.add(saved);
            }
            final CrestBaseResponse respdto = new RunLumiSetDto().resources(savedlist)
                    .size((long) savedlist.size()).datatype("runs");
            return Response.created(info.getRequestUri()).entity(respdto).build();
        }
        catch (final CdbServiceException e) {
            // Exception, send 500.
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
     * @see hep.crest.server.swagger.api.RuninfoApiService#listRunLumiInfo(java.lang.
     * String, java.lang.Integer, java.lang.Integer, java.lang.String,
     * javax.ws.rs.core.SecurityContext, javax.ws.rs.core.UriInfo)
     */
    @Override
    public Response listRunInfo(String by, Integer page, Integer size, String sort,
            SecurityContext securityContext, UriInfo info) throws NotFoundException {
        try {
            log.debug("Search resource list using by={}, page={}, size={}, sort={}", by, page, size,
                    sort);
            // Find a RunInfo resource.
            final CrestBaseResponse setdto = this.findRunLumiInfo(by, page, size, sort);
            if (setdto == null) {
                // Return 404.
                final String message = "No resource has been found";
                final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.INFO,
                        message);
                return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
            }
            // Return 200.
            return Response.ok().entity(setdto).build();

        }
        catch (final CdbServiceException e) {
            // Exception, send 500.
            final String message = e.getMessage();
            log.error("Api method listRunLumiInfo got exception : {}", message);
            return rfh.internalError("listRunInfo error: " + message);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see hep.crest.server.swagger.api.RuninfoApiService#selectRunInfo(java.lang.
     * String, java.lang.String, java.lang.String, java.lang.Integer,
     * java.lang.Integer, java.lang.String, javax.ws.rs.core.SecurityContext,
     * javax.ws.rs.core.UriInfo)
     */
    @Override
    public Response selectRunInfo(String from, String to, String format, String mode,
            SecurityContext securityContext, UriInfo info) throws NotFoundException {
        try {
            log.debug("Search resource list using from={}, to={}, format={}, mode={}", from, to,
                    format, mode);
            // Select RunInfo in a range.
            List<RunLumiInfoDto> dtolist = new ArrayList<>();
            if (mode.equalsIgnoreCase("runrange")) {
                // Interpret as Runs
                final BigDecimal bfrom = new BigDecimal(from);
                final BigDecimal bto = new BigDecimal(to);
                // Inclusive selection.
                dtolist = runinfoService.selectInclusiveByRun(bfrom, bto);
            }
            else if (mode.equalsIgnoreCase("daterange")) {
                // Interpret as Dates
                Timestamp tsfrom = null;
                Timestamp tsto = null;
                if (format.equals("iso")) {
                    log.debug("Using from and to as times in yyyymmddhhmiss");
                    final DateTimeFormatter locFormatter = DateTimeFormatter
                            .ofPattern("yyyyMMddHHmmss");
                    final ZonedDateTime zdtfrom = LocalDateTime.parse(from, locFormatter)
                            .atZone(ZoneId.of("Z"));
                    final ZonedDateTime zdtto = LocalDateTime.parse(to, locFormatter)
                            .atZone(ZoneId.of("Z"));
                    tsfrom = new Timestamp(zdtfrom.toInstant().toEpochMilli());
                    tsto = new Timestamp(zdtto.toInstant().toEpochMilli());
                }
                else if (format.equals("number")) {
                    tsfrom = new Timestamp(new Long(from));
                    tsto = new Timestamp(new Long(to));
                }
                // Inclusive selection.
                dtolist = runinfoService.selectInclusiveByDate(new Date(tsfrom.getTime()),
                        new Date(tsto.getTime()));
            }
            // Create response Set.
            final CrestBaseResponse setdto = new RunLumiSetDto().resources(dtolist)
                    .size((long) dtolist.size()).datatype("runs");
            final GenericMap filters = new GenericMap();
            filters.put("from", from);
            filters.put("to", to);
            filters.put("mode", mode);
            if (filters != null) {
                setdto.filter(filters);
            }
            return Response.ok().entity(setdto).build();
        }
        catch (final CdbServiceException e) {
            // Exception, send 500.
            final String message = e.getMessage();
            log.error("findRunLumiInfo got Exception : {}", message);
            return rfh.internalError("findRunLumiInfo error: " + message);
        }
    }

    /**
     * @param by
     *            the String
     * @param page
     *            the Integer
     * @param size
     *            the Integer
     * @param sort
     *            the String
     * @throws CdbServiceException
     *             If an exception occurred
     * @return CrestBaseResponse
     */
    protected CrestBaseResponse findRunLumiInfo(String by, Integer page, Integer size, String sort)
            throws CdbServiceException {
        final PageRequest preq = prh.createPageRequest(page, size, sort);

        List<RunLumiInfoDto> dtolist = null;
        List<SearchCriteria> params = null;
        GenericMap filters = null;
        if (by.equals("none")) {
            dtolist = runinfoService.findAllRunInfo(null, preq);
        }
        else {
            params = prh.createMatcherCriteria(by);
            filters = prh.getFilters(params);
            final List<BooleanExpression> expressions = filtering.createFilteringConditions(params);
            final BooleanExpression wherepred = prh.getWhere(expressions);
            dtolist = runinfoService.findAllRunInfo(wherepred, preq);
        }
        if (dtolist == null) {
            return null;
        }
        final CrestBaseResponse setdto = new RunLumiSetDto().resources(dtolist)
                .size((long) dtolist.size()).datatype("runs");
        if (filters != null) {
            setdto.filter(filters);
        }
        return setdto;
    }

}
