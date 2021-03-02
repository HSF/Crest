package hep.crest.server.swagger.api.impl;

import hep.crest.data.config.CrestProperties;
import hep.crest.data.pojo.Tag;
import hep.crest.server.exceptions.NotExistsPojoException;
import hep.crest.server.services.DirectoryService;
import hep.crest.server.services.TagService;
import hep.crest.server.swagger.api.ApiResponseMessage;
import hep.crest.server.swagger.api.FsApiService;
import hep.crest.server.swagger.api.NotFoundException;
import hep.crest.swagger.model.GenericMap;
import hep.crest.swagger.model.TagDto;
import hep.crest.swagger.model.TagSetDto;
import ma.glasnost.orika.MapperFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.util.Date;
import java.util.concurrent.Future;

/**
 * Rest enpoint to deal with filesystem storage for conditions data. It requires
 * a volume mounted, where the payload data can be stored. It is used to gather
 * all conditions for a tag, and prepare a tarball for clients.
 *
 * @author formica
 *
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2017-09-08T10:40:47.444+02:00")
@Component
public class FsApiServiceImpl extends FsApiService {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(FsApiServiceImpl.class);

    /**
     * Service.
     */
    @Autowired
    private TagService tagService;
    /**
     * Service.
     */
    @Autowired
    private DirectoryService dirsvc;
    /**
     * Properties.
     */
    @Autowired
    private CrestProperties cprops;
    /**
     * Mapper.
     */
    @Autowired
    @Qualifier("mapper")
    private MapperFacade mapper;

    /*
     * (non-Javadoc)
     * 
     * @see hep.crest.server.swagger.api.FsApiService#buildTar(java.lang.String,
     * java.lang.Long, javax.ws.rs.core.SecurityContext, javax.ws.rs.core.UriInfo,
     * javax.servlet.http.HttpServletRequest)
     */
    @Override
    public Response buildTar(@NotNull String tagname, @NotNull Long snapshot,
            SecurityContext securityContext, UriInfo info, HttpServletRequest request)
            throws NotFoundException {
        log.info("FileSystemRestController processing request for tag name " + tagname);
        try {
            // Find a tag using tagname in input.
            final Tag entity = tagService.findOne(tagname);
            log.debug("Found tag {}", entity.getName());
            final String reqid = request.getSession().getId() + new Date().getTime();
            
            // Tag was found: load iovs for the given tag
            Date snap = new Date();
            if (snapshot != 0L) {
                snap = new Date(snapshot);
            }
            @SuppressWarnings("unused")
            final Future<String> future = dirsvc.dumpTag(tagname, snap, reqid);
            // Send back a response. The reqid can be used for retrieval.
            return Response.ok("Launched task for tar creation: tar will be available at " + reqid)
                    .build();
        }
        catch (final NotExistsPojoException e) {
            final String msg = "Cannot find tag " + tagname;
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.INFO, msg);
            return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
        }
        catch (final RuntimeException e) {
            // An error occurred. Send a 500.
            final String msg = "Error retrieving Tag resource to create tar file...";
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
        }
    }

    @Override
    public Response findTag(@NotNull String tagname, @NotNull String reqid, SecurityContext securityContext,
                            UriInfo info)
            throws NotFoundException {
        log.info("FileSystemRestController processing request for request id {} and tag {} ", reqid, tagname);
        try {
            String outdir = "none";
            // Check req id
            if (!"none".equals(reqid)) {
                outdir = cprops.getDumpdir() + File.separator + reqid;
            }
            // Find a tag using tagname in input.
            Tag fst = dirsvc.getTag(tagname, outdir);
            TagDto dto = null;
            if ( fst != null) {
                // send back tag info
                dto = mapper.map(fst, TagDto.class);
            }
            else {
                final Tag entity = tagService.findOne(tagname);
                log.debug("Found tag {} in DB, dump it on file system.", entity.getName());
                dirsvc.insertTag(entity, outdir);
                dto = mapper.map(entity, TagDto.class);
            }
            // Create the set.
            GenericMap filterMap = new GenericMap();
            filterMap.put("tagname", tagname);
            final TagSetDto respdto = (TagSetDto) new TagSetDto().addResourcesItem(dto).size(1L)
                    .filter(filterMap).datatype("tags");
            return Response.ok().entity(respdto).build();
        }
        catch (final NotExistsPojoException e) {
            final String msg = "Cannot find tag " + tagname;
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.INFO, msg);
            return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
        }
        catch (final RuntimeException e) {
            // An error occurred. Send a 500.
            final String msg = "Error retrieving Tag resource from file system...";
            final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
        }
    }

}
