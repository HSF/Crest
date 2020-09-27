package hep.crest.server.swagger.api;

import hep.crest.swagger.model.*;
import hep.crest.server.swagger.api.MonitoringApiService;

import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.*;

import hep.crest.swagger.model.PayloadTagInfoDto;

import java.util.List;
import hep.crest.server.swagger.api.NotFoundException;

import java.io.InputStream;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.*;
import javax.validation.constraints.*;

@Path("/monitoring")


@io.swagger.annotations.Api(description = "the monitoring API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2018-01-14T18:09:32.330+01:00")
public class MonitoringApi  {

	@Autowired
	private MonitoringApiService delegate;

    @GET
    @Path("/payloads")
    
    @Produces({ "application/json", "application/xml" })
    @io.swagger.annotations.ApiOperation(value = "Retrieves monitoring information on payload as a list of PayloadTagInfoDtos.", notes = "This method allows to perform search and sorting.Arguments: by=<pattern>, page={ipage}, size={isize}, sort=<sortpattern>. The pattern <pattern> is in the form <param-name><operation><param-value>       <param-name> is the name of one of the fields in the dto       <operation> can be [< : >] ; for string use only [:]        <param-value> depends on the chosen parameter. A list of this criteria can be provided       using comma separated strings for <pattern>.      The pattern <sortpattern> is <field>:[DESC|ASC]", response = PayloadTagInfoSetDto.class, tags={ "monitoring", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = PayloadTagInfoSetDto.class) })
    public Response listPayloadTagInfo(@ApiParam(value = "tagname: the search pattern {none}", defaultValue="none") @DefaultValue("none") @QueryParam("tagname") String tagname
,@Context SecurityContext securityContext,@Context UriInfo info)
    throws NotFoundException {
        return delegate.listPayloadTagInfo(tagname,securityContext,info);
    }
}
