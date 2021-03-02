package hep.crest.server.swagger.api;

import hep.crest.server.swagger.api.*;
import hep.crest.swagger.model.*;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;


import java.util.List;
import hep.crest.server.swagger.api.NotFoundException;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import javax.validation.constraints.*;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2021-02-11T22:23:54.659+01:00")
public abstract class FsApiService {
    public abstract Response buildTar( @NotNull String tagname, @NotNull Long snapshot,SecurityContext securityContext, UriInfo info, HttpServletRequest request) throws NotFoundException;
    public abstract Response findTag( @NotNull String tagname, @NotNull String reqid,SecurityContext securityContext,
                                      UriInfo info) throws NotFoundException;
}
