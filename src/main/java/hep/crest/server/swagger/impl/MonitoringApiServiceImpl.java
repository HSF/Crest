package hep.crest.server.swagger.impl;

import hep.crest.server.controllers.PageRequestHelper;
import hep.crest.server.repositories.monitoring.IMonitoringRepository;
import hep.crest.server.swagger.api.MonitoringApiService;
import hep.crest.server.swagger.api.NotFoundException;
import hep.crest.server.swagger.model.CrestBaseResponse;
import hep.crest.server.swagger.model.GenericMap;
import hep.crest.server.swagger.model.PayloadTagInfoDto;
import hep.crest.server.swagger.model.PayloadTagInfoSetDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;

/**
 * Rest endpoint for monitoring informations.
 *
 * @author formica
 */
@Component
@Slf4j
public class MonitoringApiServiceImpl extends MonitoringApiService {

    /**
     * Helper.
     */
    PageRequestHelper prh;

    /**
     * Repository.
     */
    IMonitoringRepository monitoringrepo;

    /**
     * Context
     *
     */
    private JAXRSContext context;

    /**
     * Ctor with injected service.
     * @param monitoringrepo the monitoring repository.
     * @param prh the page request helper.
     * @param context the context.
     */
    @Autowired
    public MonitoringApiServiceImpl(IMonitoringRepository monitoringrepo,
                                    PageRequestHelper prh, JAXRSContext context) {
        this.monitoringrepo = monitoringrepo;
        this.prh = prh;
        this.context = context;
    }

    /*
     * (non-Javadoc)
     * @see
     * hep.crest.server.swagger.api.MonitoringApiService#listPayloadTagInfo(java.
     * lang.String, jakarta.ws.rs.core.SecurityContext, jakarta.ws.rs.core.UriInfo)
     */
    @Override
    public Response listPayloadTagInfo(String tagname, SecurityContext securityContext)
            throws NotFoundException {
        log.debug("Search resource list using tagname or pattern={}", tagname);
        List<PayloadTagInfoDto> dtolist = null;
        String tagpattern = tagname;

        // Set default tag pattern.
        if ("none".equals(tagpattern)) {
            // select any tag.
            tagpattern = "%";
        }
        else {
            // Add special pattern regexp.
            tagpattern = "%" + tagpattern + "%";
        }
        // Create filters
        final GenericMap filters = new GenericMap();
        filters.put("tagname", tagpattern);
        // Select tag informations.
        dtolist = monitoringrepo.selectTagInfo(tagpattern);
        // The dtolist should always be non null....
        // Create the PayloadTagInfoSet
        final CrestBaseResponse setdto = new PayloadTagInfoSetDto().resources(dtolist)
                .filter(filters).size(1L).datatype("payloadtaginfos");
        // Return 200.
        return Response.ok().entity(setdto).build();
    }
}
