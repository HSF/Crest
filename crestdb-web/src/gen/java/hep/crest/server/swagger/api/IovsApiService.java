package hep.crest.server.swagger.api;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import hep.crest.swagger.model.IovDto;
import hep.crest.swagger.model.IovSetDto;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2019-10-02T17:41:20.963+02:00")
public abstract class IovsApiService {
    public abstract Response createIov(IovDto body,SecurityContext securityContext, UriInfo info) throws NotFoundException;
    public abstract Response findAllIovs( @NotNull String by, Integer page, Integer size, String sort,String dateformat,SecurityContext securityContext, UriInfo info) throws NotFoundException;
    public abstract Response getSize( @NotNull String tagname, Long snapshot,SecurityContext securityContext, UriInfo info) throws NotFoundException;
    public abstract Response getSizeByTag( @NotNull String tagname,SecurityContext securityContext, UriInfo info) throws NotFoundException;
    public abstract Response selectGroups( @NotNull String tagname, Long snapshot,SecurityContext securityContext, UriInfo info, Request request, HttpHeaders headers) throws NotFoundException;
    public abstract Response selectIovs(String xCrestQuery, String tagname, String since, String until, Long snapshot,SecurityContext securityContext, UriInfo info,Request request, HttpHeaders headers) throws NotFoundException;
    public abstract Response selectSnapshot( @NotNull String tagname, @NotNull Long snapshot,SecurityContext securityContext, UriInfo info) throws NotFoundException;
    public abstract Response storeBatchIovMultiForm(IovSetDto body,SecurityContext securityContext, UriInfo info) throws NotFoundException;
    public abstract Response lastIov(String tagname, String since, Long snapshot, String dateformat, SecurityContext securityContext, UriInfo info)  throws NotFoundException;
}
