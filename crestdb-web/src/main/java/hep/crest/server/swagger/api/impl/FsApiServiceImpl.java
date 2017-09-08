package hep.crest.server.swagger.api.impl;

import java.util.Date;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import hep.crest.server.services.DirectoryService;
import hep.crest.server.services.TagService;
import hep.crest.server.swagger.api.ApiResponseMessage;
import hep.crest.server.swagger.api.FsApiService;
import hep.crest.server.swagger.api.NotFoundException;
import hep.crest.swagger.model.TagDto;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-09-08T10:40:47.444+02:00")
@Component
public class FsApiServiceImpl extends FsApiService {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TagService tagService;
	
	@Autowired
	private DirectoryService dirsvc;

    @Override
    public Response buildTar( @NotNull String tagname,  @NotNull Long snapshot, SecurityContext securityContext, UriInfo info, HttpServletRequest request) throws NotFoundException {
		this.log.info("FileSystemRestController processing request for tag name " + tagname);
		try {
			TagDto dto = tagService.findOne(tagname);
			String reqid = request.getSession().getId() + new Date().getTime();
			if (dto == null) {
				log.debug("Entity Not Found for name " + tagname);
				String msg = "Entity not found exception ";
				ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
				return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
			}
			// load iovs for the given tag
			Date snap = new Date();
			if (snapshot != 0L) {
				snap = new Date(snapshot);
			}
			Future<String> future = dirsvc.dumpTag(tagname, snap, reqid);
			return Response.ok("Launched task for tar creation: tar will be available at "+reqid).build();
		
		} catch (Exception e) {
			String msg = "Error retrieving Tag resource to create tar file...";
			ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
		}
    }
}
