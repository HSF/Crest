package hep.crest.server.swagger.impl;

import hep.crest.server.controllers.EntityDtoHelper;
import hep.crest.server.controllers.PageRequestHelper;
import hep.crest.server.converters.RunLumiMapper;
import hep.crest.server.data.runinfo.pojo.RunLumiInfo;
import hep.crest.server.exceptions.CdbBadRequestException;
import hep.crest.server.services.RunInfoService;
import hep.crest.server.swagger.api.NotFoundException;
import hep.crest.server.swagger.api.RuninfoApiService;
import hep.crest.server.swagger.model.CrestBaseResponse;
import hep.crest.server.swagger.model.GenericMap;
import hep.crest.server.swagger.model.RespPage;
import hep.crest.server.swagger.model.RunLumiInfoDto;
import hep.crest.server.swagger.model.RunLumiSetDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import java.math.BigInteger;
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
 */
@Component
@Slf4j
public class RuninfoApiServiceImpl extends RuninfoApiService {
    /**
     * Helper.
     */
    private PageRequestHelper prh;
    /**
     * Helper.
     */
    EntityDtoHelper edh;
    /**
     * Service.
     */
    private RunInfoService runinfoService;

    /**
     * Context.
     */
    @Autowired
    private JAXRSContext context;

    /**
     * Ctor with injected service.
     * @param RunInfoService the service.
     * @param prh the PageRequestHelper
     * @param edh the EntityDtoHelper
     */
    @Autowired
    public RuninfoApiServiceImpl(RunInfoService RunInfoService, PageRequestHelper prh,
                                 EntityDtoHelper edh) {
        this.runinfoService = RunInfoService;
        this.prh = prh;
        this.edh = edh;
    }
    /*
     * (non-Javadoc)
     *
     * @see hep.crest.server.swagger.api.RuninfoApiService#createRunLumiInfo(hep.crest.
     * swagger.model.RunLumiSetDto, jakarta.ws.rs.core.SecurityContext,
     * jakarta.ws.rs.core.UriInfo)
     */
    @Override
    public Response createRunInfo(RunLumiSetDto body, SecurityContext securityContext)
            throws NotFoundException {
        log.info("RunInfoRestController processing request for creating a run info entry using "
                 + body);
        // Create a list of resources
        final List<RunLumiInfoDto> dtolist = body.getResources();
        final List<RunLumiInfoDto> savedlist = new ArrayList<>();
        for (final RunLumiInfoDto runInfoDto : dtolist) {
            // Create a RunInfo resource.
            final RunLumiInfoDto saved = runinfoService.insertRunInfo(runInfoDto);
            // Add to the saved list for the response.
            savedlist.add(saved);
        }
        RespPage respPage = new RespPage().size(savedlist.size())
                .totalElements((long) savedlist.size()).totalPages(1)
                .number(0);
        final CrestBaseResponse respdto = new RunLumiSetDto()
                .resources(savedlist)
                .format("RunLumiSetDto")
                .filter(new GenericMap())
                .page(respPage)
                .size((long) savedlist.size()).datatype("runs");
        return Response.created(context.getUriInfo().getRequestUri()).entity(respdto).build();

    }

    @Override
    public Response updateRunInfo(RunLumiInfoDto runLumiInfoDto, SecurityContext securityContext)
            throws NotFoundException {
        log.info("RunInfoRestController processing request for updating a run info entry using "
                 + runLumiInfoDto);
        // Create a list of resources
        final RunLumiInfoDto saved = runinfoService.updateRunInfo(runLumiInfoDto);
        List<RunLumiInfoDto> reslist = new ArrayList<>();
        reslist.add(saved);
        RespPage respPage = new RespPage().size(reslist.size())
                .totalElements((long) reslist.size()).totalPages(1)
                .number(0);
        final CrestBaseResponse respdto = new RunLumiSetDto().resources(reslist)
                .page(respPage)
                .filter(new GenericMap())
                .format("RunLumiSetDto").size((long) reslist.size()).datatype("runs");
        return Response.created(context.getUriInfo().getRequestUri()).entity(respdto).build();
    }

    /*
     * (non-Javadoc)
     *
     * @see hep.crest.server.swagger.api.RuninfoApiService#selectRunInfo(java.lang.
     * String, java.lang.String, java.lang.String, java.lang.Integer,
     * java.lang.Integer, java.lang.String, jakarta.ws.rs.core.SecurityContext,
     * jakarta.ws.rs.core.UriInfo)
     */
    @Override
    public Response listRunInfo(String from, String to, String format, String mode,
                                Integer page, Integer size, String sort,
                                SecurityContext securityContext) throws NotFoundException {
        log.debug("Search resource list using from={}, to={}, format={}, mode={}", from, to,
                format, mode);
        // Select RunInfo in a range.
        // Create pagination request
        final PageRequest preq = prh.createPageRequest(page, size, sort);
        Page<RunLumiInfo> entitypage = null;
        if (mode.equalsIgnoreCase("runrange")) {
            if ("number".equalsIgnoreCase(format)) {
                // Interpret as Runs
                final BigInteger bfrom = BigInteger.valueOf(Long.valueOf(from));
                final BigInteger bto = BigInteger.valueOf(Long.valueOf(to));
                // Inclusive selection.
                entitypage = runinfoService.selectInclusiveByRun(bfrom, bto, preq);
            }
            else if ("run-lumi".equalsIgnoreCase(format)) {
                // Interpret as RunLumi
                final String[] fromparts = from.split("-");
                final String[] toparts = to.split("-");
                final BigInteger run = BigInteger.valueOf(Long.valueOf(fromparts[0]));
                final BigInteger bto = BigInteger.valueOf(Long.valueOf(toparts[0]));
                final BigInteger run2 = BigInteger.valueOf(Long.valueOf(fromparts[1]));
                final BigInteger lto = BigInteger.valueOf(Long.valueOf(toparts[1]));
                if (!run.equals(run2)) {
                    log.warn("Run numbers are different in from and to {} {}", from, to);
                    throw new CdbBadRequestException("Run numbers are different in from and to");
                }
                // Inclusive selection.
                entitypage = runinfoService.selectInclusiveByLumiBlock(run, bto, lto, preq);
            }
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
                tsfrom = new Timestamp(Long.valueOf(from));
                tsto = new Timestamp(Long.valueOf(to));
            }
            // Inclusive selection.
            entitypage = runinfoService.selectInclusiveByDate(new Date(tsfrom.getTime()),
                    new Date(tsto.getTime()), preq);
        }
        // Create filters
        GenericMap filters = new GenericMap();
        filters.put("from", from.toString());
        filters.put("to", to.toString());
        filters.put("mode", mode);

        // Search for global tags using where conditions.
        RespPage respPage = new RespPage().size(entitypage.getSize())
                .totalElements(entitypage.getTotalElements()).totalPages(entitypage.getTotalPages())
                .number(entitypage.getNumber());

        final List<RunLumiInfoDto> dtolist = edh.entityToDtoList(entitypage.toList(),
                RunLumiInfoDto.class, RunLumiMapper.class);
        Response.Status rstatus = Response.Status.OK;
        // Prepare the Set.
        final CrestBaseResponse saveddto = buildEntityResponse(dtolist, filters);
        saveddto.page(respPage);
        // Send a response and status 200.
        return Response.status(rstatus).entity(saveddto).build();
    }

    /**
     * Factorise code to build the RunLumiSetDto.
     *
     * @param dtolist the List<RunLumiInfoDto>
     * @param filters the GenericMap
     * @return RunLumiSetDto
     */
    protected RunLumiSetDto buildEntityResponse(List<RunLumiInfoDto> dtolist, GenericMap filters) {
        final RunLumiSetDto respdto = new RunLumiSetDto();
        // Create the Set for the response.
        ((RunLumiSetDto) respdto.datatype("runs")).resources(dtolist)
                .format("RunLumiSetDto").size((long) dtolist.size());
        respdto.filter(filters);
        return respdto;
    }
}
