package hep.crest.server.swagger.api;

import hep.crest.swagger.model.*;
import hep.crest.server.annotations.CacheControlCdb;
import hep.crest.server.swagger.api.IovsApiService;
/////import hep.crest.server.swagger.api.factories.IovsApiServiceFactory;

import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.*;

import hep.crest.swagger.model.GroupDto;
import hep.crest.swagger.model.IovDto;
import hep.crest.swagger.model.TagSummaryDto;

import java.util.List;
import hep.crest.server.swagger.api.NotFoundException;

import java.io.InputStream;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.*;
import javax.validation.constraints.*;

@Path("/iovs")

@io.swagger.annotations.Api(description = "the iovs API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-09-06T09:44:28.040+02:00")
public class IovsApi {
	// private final IovsApiService delegate = IovsApiServiceFactory.getIovsApi();

	@Autowired
	private IovsApiService delegate;

	@POST

	@Consumes({ "application/json" })
	@Produces({ "application/json" })
	@io.swagger.annotations.ApiOperation(value = "Create a Iov in the database.", notes = "This method allows to insert a Iov.Arguments: IovDto should be provided in the body as a JSON file.", response = IovDto.class, tags = {
			"iovs", })
	@io.swagger.annotations.ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = IovDto.class) })
	public Response createIov(
			@ApiParam(value = "A json string that is used to construct a iovdto object: { name: xxx, ... }", required = true) IovDto body,
			@Context SecurityContext securityContext, @Context UriInfo info) throws NotFoundException {
		return delegate.createIov(body, securityContext, info);
	}

	@GET

	@Produces({ "application/json" })
	@io.swagger.annotations.ApiOperation(value = "Finds a IovDtos lists.", notes = "This method allows to perform search by tagname and sorting.Arguments: tagname={a tag name}, page={ipage}, size={isize},      sort=<pattern>, where pattern is <field>:[DESC|ASC]", response = IovDto.class, responseContainer = "List", tags = {
			"iovs", })
	@io.swagger.annotations.ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = IovDto.class, responseContainer = "List") })
	public Response findAllIovs(
			@ApiParam(value = "tagname: the tag name {none}", defaultValue = "none") @DefaultValue("none") @QueryParam("tagname") String tagname,
			@ApiParam(value = "page: the page number {0}", defaultValue = "0") @DefaultValue("0") @QueryParam("page") Integer page,
			@ApiParam(value = "size: the page size {10000}", defaultValue = "10000") @DefaultValue("10000") @QueryParam("size") Integer size,
			@ApiParam(value = "sort: the sort pattern {id.since:ASC}", defaultValue = "id.since:ASC") @DefaultValue("id.since:ASC") @QueryParam("sort") String sort,
			@Context SecurityContext securityContext, @Context UriInfo info) throws NotFoundException {
		return delegate.findAllIovs(tagname, page, size, sort, securityContext, info);
	}

	@GET
	@Path("/getSize")

	@Produces({ "application/json" })
	@io.swagger.annotations.ApiOperation(value = "Get the number o iovs for the given tag.", notes = "This method allows to select the count of iovs in a tag. Also possible to get the size of snapshot, if the time added.Arguments: tagname={a tag name}, snapshotTime={snapshot time in milliseconds (Long) from epoch}", response = Long.class, tags = {
			"iovs", })
	@io.swagger.annotations.ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = Long.class) })
	public Response getSize(
			@ApiParam(value = "tagname: the tag name {none}", required = true, defaultValue = "none") @DefaultValue("none") @QueryParam("tagname") String tagname,
			@ApiParam(value = "snapshot: the snapshot time {0}", defaultValue = "0") @DefaultValue("0") @QueryParam("snapshot") Long snapshot,
			@Context SecurityContext securityContext, @Context UriInfo info) throws NotFoundException {
		return delegate.getSize(tagname, snapshot, securityContext, info);
	}

	@GET
	@Path("/getSizeByTag")
	@Produces({ "application/json" })
	@io.swagger.annotations.ApiOperation(value = "Get the number o iovs for tags matching pattern.", notes = "This method allows to select the count of iovs in a tag. Also possible to get the size of snapshot, if the time added.Arguments: tagname={a tag name}", response = TagSummaryDto.class, responseContainer = "List", tags = {
			"iovs", })
	@io.swagger.annotations.ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = TagSummaryDto.class, responseContainer = "List") })
	public Response getSizeByTag(
			@ApiParam(value = "tagname: the tag name {none}", required = true, defaultValue = "none") @DefaultValue("none") @QueryParam("tagname") String tagname,
			@Context SecurityContext securityContext, @Context UriInfo info) throws NotFoundException {
		return delegate.getSizeByTag(tagname, securityContext, info);
	}

	@GET
	@Path("/selectGroups")
	@Produces({ "application/json" })
	@io.swagger.annotations.ApiOperation(value = "Select groups for a given tagname.", notes = "This method allows to select a list of groups.Arguments: tagname={a tag name}, snapshot={snapshot time as long}", response = GroupDto.class, tags = {
			"iovs", })
	@io.swagger.annotations.ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = GroupDto.class) })
	public Response selectGroups(
			@ApiParam(value = "tagname: the tag name {none}", required = true, defaultValue = "none") @DefaultValue("none") @QueryParam("tagname") String tagname,
			@ApiParam(value = "snapshot: the snapshot time {0}", defaultValue = "0") @DefaultValue("0") @QueryParam("snapshot") Long snapshot,
			@Context SecurityContext securityContext, @Context UriInfo info, @Context Request request,
			@Context HttpHeaders headers) throws NotFoundException {
		return delegate.selectGroups(tagname, snapshot, securityContext, info, request, headers);
	}

	@GET
	@Path("/selectIovs")
	@Produces({ "application/json" })
	@io.swagger.annotations.ApiOperation(value = "Select iovs for a given tagname and in a given range.", notes = "This method allows to select a list of iovs in a tag, using a given range in time and (optionally) for a given snapshot time.Arguments: tagname={a tag name}, since={since time as string}, until={until time as string}, snapshot={snapshot time as long}", response = IovDto.class, responseContainer = "List", tags = {
			"iovs", })
	@io.swagger.annotations.ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = IovDto.class, responseContainer = "List") })
	public Response selectIovs(
			@ApiParam(value = "tagname: the tag name {none}", defaultValue = "none") @DefaultValue("none") @QueryParam("tagname") String tagname,
			@ApiParam(value = "since: the since time as a string {0}", defaultValue = "0") @DefaultValue("0") @QueryParam("since") String since,
			@ApiParam(value = "until: the until time as a string {INF}", defaultValue = "INF") @DefaultValue("INF") @QueryParam("until") String until,
			@ApiParam(value = "snapshot: the snapshot time {0}", defaultValue = "0") @DefaultValue("0") @QueryParam("snapshot") Long snapshot,
			@Context SecurityContext securityContext, @Context UriInfo info, @Context Request request,
			@Context HttpHeaders headers) throws NotFoundException {
		return delegate.selectIovs(tagname, since, until, snapshot, securityContext, info, request, headers);
	}

	@GET
	@Path("/selectSnapshot")

	@Produces({ "application/json" })
	@io.swagger.annotations.ApiOperation(value = "Select snapshot for a given tagname and insertion time.", notes = "This method allows to select a list of all iovs in a tag, using (optionally) a given snapshot time.Arguments: tagname={a tag name}, snapshot={snapshot time as long}", response = IovDto.class, responseContainer = "List", tags = {
			"iovs", })
	@io.swagger.annotations.ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = IovDto.class, responseContainer = "List") })
	public Response selectSnapshot(
			@ApiParam(value = "tagname: the tag name {none}", required = true, defaultValue = "none") @DefaultValue("none") @QueryParam("tagname") String tagname,
			@ApiParam(value = "snapshot: the snapshot time {0}", required = true, defaultValue = "0") @DefaultValue("0") @QueryParam("snapshot") Long snapshot,
			@Context SecurityContext securityContext, @Context UriInfo info) throws NotFoundException {
		return delegate.selectSnapshot(tagname, snapshot, securityContext, info);
	}
}
