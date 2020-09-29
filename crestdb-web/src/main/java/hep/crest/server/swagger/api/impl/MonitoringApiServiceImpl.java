package hep.crest.server.swagger.api.impl;

import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.monitoring.repositories.IMonitoringRepository;
import hep.crest.server.controllers.PageRequestHelper;
import hep.crest.server.swagger.api.ApiResponseMessage;
import hep.crest.server.swagger.api.MonitoringApiService;
import hep.crest.server.swagger.api.NotFoundException;
import hep.crest.swagger.model.CrestBaseResponse;
import hep.crest.swagger.model.GenericMap;
import hep.crest.swagger.model.PayloadTagInfoDto;
import hep.crest.swagger.model.PayloadTagInfoSetDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.util.List;

/**
 * Rest endpoint for monitoring informations.
 *
 * @author formica
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-11-07T14:29:18.354+01:00")
@Component
public class MonitoringApiServiceImpl extends MonitoringApiService {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(MonitoringApiServiceImpl.class);

    /**
     * Helper.
     */
    @Autowired
    PageRequestHelper prh;

    /**
     * Repository.
     */
    @Autowired
    IMonitoringRepository monitoringrepo;

    /*
     * (non-Javadoc)
     * @see
     * hep.crest.server.swagger.api.MonitoringApiService#listPayloadTagInfo(java.
     * lang.String, javax.ws.rs.core.SecurityContext, javax.ws.rs.core.UriInfo)
     */
    @Override
    public Response listPayloadTagInfo(String tagname, SecurityContext securityContext, UriInfo info)
            throws NotFoundException {
        try {
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
                    .format("PayloadTagInfoSetDto").filter(filters).size((long)(dtolist.size())).datatype("payloadtaginfos");
            // Return 200.
            return Response.ok().entity(setdto).build();
        }
        catch (final CdbServiceException e) {
            // Exception, send a 500.
            log.error("Exception listing payload tag info : {}", e.getMessage());
            final String message = e.getMessage();
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, message);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
        }
    }
}
