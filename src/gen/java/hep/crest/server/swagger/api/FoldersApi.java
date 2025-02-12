package hep.crest.server.swagger.api;

import hep.crest.server.swagger.api.FoldersApiService;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import hep.crest.server.swagger.model.FolderDto;
import hep.crest.server.swagger.model.FolderSetDto;

import java.util.Map;
import java.util.List;
import hep.crest.server.swagger.api.NotFoundException;

import java.io.InputStream;

import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import hep.crest.server.swagger.impl.JAXRSContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Request;

import jakarta.servlet.ServletConfig;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.*;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

@Path("/folders")


///// @jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJerseyServerCodegen")
public class FoldersApi  {

   @Autowired
   private FoldersApiService delegate;
   @Context
   protected Request request;
   @Context
   protected HttpHeaders headers;
   @Context
   protected UriInfo uriInfo;
   @Autowired
   protected JAXRSContext context;




    @jakarta.ws.rs.POST
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @Operation(summary = "Create an entry for folder information.", description = "", responses = {
            @ApiResponse(responseCode = "201", description = "successful operation", content =
                @Content(schema = @Schema(implementation = FolderDto.class))),
            },security = {
            @SecurityRequirement(name = "OpenID", scopes={ "openid" }),
            @SecurityRequirement(name = "BearerAuth")
        }, tags={ "folders", })
    public Response createFolder(@Parameter(description = "") @Valid  FolderDto folderDto,@Context SecurityContext securityContext)
    throws NotFoundException {
        context.setHttpHeaders(headers);
        context.setRequest(request);
        context.setUriInfo(uriInfo);
        return delegate.createFolder(folderDto, securityContext);
    }

    @jakarta.ws.rs.GET
    @Produces({ "application/json" })
    @Operation(summary = "Finds a FolderDto list.", description = "", responses = {
            @ApiResponse(responseCode = "200", description = "successful operation", content =
                @Content(schema = @Schema(implementation = FolderSetDto.class))),
            },security = {
            @SecurityRequirement(name = "OpenID", scopes={ "openid" }),
            @SecurityRequirement(name = "BearerAuth")
        }, tags={ "folders", })
    public Response listFolders(@Parameter(description = "the schema pattern {none}", example = "none") @DefaultValue("none") @QueryParam("schema")  String schema,@Context SecurityContext securityContext)
    throws NotFoundException {
        context.setHttpHeaders(headers);
        context.setRequest(request);
        context.setUriInfo(uriInfo);
        return delegate.listFolders(schema, securityContext);
    }
}
