package hep.crest.server.swagger.api;

import hep.crest.server.swagger.api.*;
import hep.crest.swagger.model.*;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import hep.crest.swagger.model.FolderDto;

import java.util.List;
import hep.crest.server.swagger.api.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import javax.validation.constraints.*;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2018-05-10T14:37:32.399+02:00")
public abstract class FoldersApiService {
    public abstract Response createFolder(FolderDto body,SecurityContext securityContext, UriInfo info) throws NotFoundException;
    public abstract Response listFolders( String by, String sort,SecurityContext securityContext, UriInfo info) throws NotFoundException;
}
