package hep.crest.server.swagger.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.springframework.beans.factory.annotation.Autowired;

import hep.crest.swagger.model.CrestBaseResponse;
import hep.crest.swagger.model.IovDto;
import hep.crest.swagger.model.IovSetDto;
import hep.crest.swagger.model.TagSummarySetDto;
import io.swagger.annotations.ApiParam;

@Path("/iovs")


@io.swagger.annotations.Api(description = "the iovs API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2019-10-02T17:41:20.963+02:00")
public class IovsApi  {
	@Autowired
	private IovsApiService delegate;

    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Create a Iov in the database.", notes = "This method allows to insert a Iov.Arguments: IovDto should be provided in the body as a JSON file.", response = IovDto.class, tags={ "iovs", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = IovDto.class) })
    public Response createIov(@ApiParam(value = "A json string that is used to construct a iovdto object: { name: xxx, ... }" ,required=true) IovDto body
,@Context SecurityContext securityContext,@Context UriInfo info)
    throws NotFoundException {
        return delegate.createIov(body,securityContext,info);
    }
    @GET
    
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Finds a IovDtos lists.", notes = "This method allows to perform search by tagname and sorting.Arguments: tagname={a tag name}, page={ipage}, size={isize},      sort=<pattern>, where pattern is <field>:[DESC|ASC]", response = IovSetDto.class, tags={ "iovs", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = IovSetDto.class) })
    public Response findAllIovs(@ApiParam(value = "you need a mandatory tagname:xxxx. Additional field can be since or insertionTime rules.",required=true, defaultValue="none") @DefaultValue("none") @QueryParam("by") String by
,@ApiParam(value = "page: the page number {0}", defaultValue="0") @DefaultValue("0") @QueryParam("page") Integer page
,@ApiParam(value = "size: the page size {10000}", defaultValue="10000") @DefaultValue("10000") @QueryParam("size") Integer size
,@ApiParam(value = "sort: the sort pattern {id.since:ASC}", defaultValue="id.since:ASC") @DefaultValue("id.since:ASC") @QueryParam("sort") String sort
,@ApiParam(value = "The format of the input time fields: {yyyyMMdd'T'HHmmssX | ms} DEFAULT: ms (so it is a long). Used for insertionTime comparaison." , defaultValue="ms")@HeaderParam("dateformat") String dateformat
,@Context SecurityContext securityContext,@Context UriInfo info)
    throws NotFoundException {
        return delegate.findAllIovs(by,page,size,sort,dateformat,securityContext,info);
    }
    @GET
    @Path("/getSize")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get the number o iovs for the given tag.", notes = "This method allows to select the count of iovs in a tag. Also possible to get the size of snapshot, if the time added.Arguments: tagname={a tag name}, snapshotTime={snapshot time in milliseconds (Long) from epoch}", response = CrestBaseResponse.class, tags={ "iovs", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = CrestBaseResponse.class) })
    public Response getSize(@ApiParam(value = "tagname: the tag name {none}",required=true, defaultValue="none") @DefaultValue("none") @QueryParam("tagname") String tagname
,@ApiParam(value = "snapshot: the snapshot time {0}", defaultValue="0") @DefaultValue("0") @QueryParam("snapshot") Long snapshot
,@Context SecurityContext securityContext,@Context UriInfo info)
    throws NotFoundException {
        return delegate.getSize(tagname,snapshot,securityContext,info);
    }
    @GET
    @Path("/getSizeByTag")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get the number o iovs for tags matching pattern.", notes = "This method allows to select the count of iovs in a tag. Also possible to get the size of snapshot, if the time is added. Arguments: tagname={a tag name}", response = TagSummarySetDto.class, tags={ "iovs", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = TagSummarySetDto.class) })
    public Response getSizeByTag(@ApiParam(value = "tagname: the tag name {none}",required=true, defaultValue="none") @DefaultValue("none") @QueryParam("tagname") String tagname
,@Context SecurityContext securityContext,@Context UriInfo info)
    throws NotFoundException {
        return delegate.getSizeByTag(tagname,securityContext,info);
    }
    @GET
    @Path("/selectGroups")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Select groups for a given tagname.", notes = "This method allows to select a list of groups.Arguments: tagname={a tag name}, snapshot={snapshot time as long}", response = IovSetDto.class, tags={ "iovs", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = IovSetDto.class) })
    public Response selectGroups(@ApiParam(value = "tagname: the tag name {none}",required=true, defaultValue="none") @DefaultValue("none") @QueryParam("tagname") String tagname
,@ApiParam(value = "snapshot: the snapshot time {0}", defaultValue="0") @DefaultValue("0") @QueryParam("snapshot") Long snapshot
,@Context SecurityContext securityContext,@Context UriInfo info,@Context Request request, @Context HttpHeaders headers)
    throws NotFoundException {
        return delegate.selectGroups(tagname,snapshot,securityContext,info,request,headers);
    }
    @GET
    @Path("/selectIovs")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Select iovs for a given tagname and in a given range.", notes = "This method allows to select a list of iovs in a tag, using a given range in time and (optionally) for a given snapshot time.Arguments: tagname={a tag name}, since={since time as string}, until={until time as string}, snapshot={snapshot time as long}", response = IovSetDto.class, tags={ "iovs", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = IovSetDto.class) })
    public Response selectIovs(@ApiParam(value = "The query type. The header parameter X-Crest-Query can be : groups (default) or ranges (include previous since)." , defaultValue="groups")@HeaderParam("X-Crest-Query") String xCrestQuery
,@ApiParam(value = "tagname: the tag name {none}", defaultValue="none") @DefaultValue("none") @QueryParam("tagname") String tagname
,@ApiParam(value = "since: the since time as a string {0}", defaultValue="0") @DefaultValue("0") @QueryParam("since") String since
,@ApiParam(value = "until: the until time as a string {INF}", defaultValue="INF") @DefaultValue("INF") @QueryParam("until") String until
,@ApiParam(value = "snapshot: the snapshot time {0}", defaultValue="0") @DefaultValue("0") @QueryParam("snapshot") Long snapshot
,@Context SecurityContext securityContext,@Context UriInfo info,@Context Request request, @Context HttpHeaders headers)
    throws NotFoundException {
        return delegate.selectIovs(xCrestQuery,tagname,since,until,snapshot,securityContext,info,request,headers);
    }
    
    @GET
    @Path("/selectSnapshot")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Select snapshot for a given tagname and insertion time.", notes = "This method allows to select a list of all iovs in a tag, using (optionally) a given snapshot time.Arguments: tagname={a tag name}, snapshot={snapshot time as long}", response = IovSetDto.class, tags={ "iovs", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = IovSetDto.class) })
    public Response selectSnapshot(@ApiParam(value = "tagname: the tag name {none}",required=true, defaultValue="none") @DefaultValue("none") @QueryParam("tagname") String tagname
,@ApiParam(value = "snapshot: the snapshot time {0}",required=true, defaultValue="0") @DefaultValue("0") @QueryParam("snapshot") Long snapshot
,@Context SecurityContext securityContext,@Context UriInfo info)
    throws NotFoundException {
        return delegate.selectSnapshot(tagname,snapshot,securityContext,info);
    }
    @POST
    @Path("/storebatch")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Create many IOVs in the database, associated to a tag name.", notes = "This method allows to insert multiple IOVs. Arguments: tagname,end time.", response = IovSetDto.class, tags={ "iovs", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "successful operation", response = IovSetDto.class) })
    public Response storeBatchIovMultiForm(@ApiParam(value = "A json string that is used to construct a IovSetDto object." ,required=true) IovSetDto body
,@Context SecurityContext securityContext,@Context UriInfo info)
    throws NotFoundException {
        return delegate.storeBatchIovMultiForm(body,securityContext,info);
    }

    @GET
    @Path("/lastIov")

    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Select last iov for a given tagname and before a given since.", notes = "This method allows to select the last iov in a tag, before a given time and (optionally) for a given snapshot time.Arguments: tagname={a tag name}, since={since time as string}, snapshot={snapshot time as long}", response = IovSetDto.class, tags={ "iovs", })
    @io.swagger.annotations.ApiResponses(value = {
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = IovSetDto.class) })
    public Response lastIov(@ApiParam(value = "tagname: the tag name {none}", defaultValue="none") @DefaultValue("none") @QueryParam("tagname") String tagname
,@ApiParam(value = "since: the since time ", defaultValue="now") @DefaultValue("now") @QueryParam("since") String since
,@ApiParam(value = "snapshot: the snapshot time {0}", defaultValue="0") @DefaultValue("0") @QueryParam("snapshot") Long snapshot
,@ApiParam(value = "The format of the input time fields: {yyyyMMdd'T'HHmmssX | ms} DEFAULT: ms (so it is a long). Used for insertionTime comparaison." , defaultValue="ms")@HeaderParam("dateformat") String dateformat
,@Context SecurityContext securityContext,@Context UriInfo info)
    throws NotFoundException {
        return delegate.lastIov(tagname,since,snapshot,dateformat,securityContext,info);
    }
}
