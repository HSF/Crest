package hep.crest.server.swagger.api;

import hep.crest.server.swagger.api.*;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;

import hep.crest.server.swagger.model.FolderDto;
import hep.crest.server.swagger.model.FolderSetDto;

import java.util.List;
import hep.crest.server.swagger.api.NotFoundException;

import java.io.InputStream;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.validation.constraints.*;
//// @jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJerseyServerCodegen")
public abstract class FoldersApiService {
    public abstract Response createFolder(FolderDto folderDto,SecurityContext securityContext) throws NotFoundException;
    public abstract Response listFolders(String schema,SecurityContext securityContext) throws NotFoundException;
}
