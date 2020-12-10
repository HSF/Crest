package hep.crest.server.swagger.api.impl;

import hep.crest.server.caching.CachingPolicyService;
import hep.crest.server.swagger.api.ApiResponseMessage;
import hep.crest.swagger.model.HTTPResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response;

@Component
public class ResponseFormatHelper {

    /**
     * Service.
     */
    @Autowired
    private CachingPolicyService cachesvc;

    /**
     * A cache control for errors.
     */
    private CacheControl cc;

    @PostConstruct
    private void postConstruct() {
        cc = cachesvc.getGroupsCacheControl(0L);
    }
    /**
     * Return a response using the HTTPResponse object in input.
     *
     * @param resp the body of the response.
     * @return Response.
     */
    public Response createApiResponse(HTTPResponse resp) {

        if (resp.getCode().equals(Response.Status.CREATED.getStatusCode())) {
            return Response.status(Response.Status.CREATED).entity(resp).build();
        }
        else if (resp.getCode().equals(Response.Status.SEE_OTHER.getStatusCode())) {
            return Response.status(Response.Status.SEE_OTHER).entity(resp).cacheControl(cc).build();
        }
        else if (resp.getCode().equals(Response.Status.BAD_REQUEST.getStatusCode())) {
            return Response.status(Response.Status.BAD_REQUEST).entity(resp).cacheControl(cc).build();
        }
        else if (resp.getCode().equals(Response.Status.NOT_FOUND.getStatusCode())) {
            return Response.status(Response.Status.NOT_FOUND).entity(resp).cacheControl(cc).build();
        }
        return internalError("Cannot create response message");
    }

    /**
     * Return an empty result set.
     *
     * @param resp the body of the response.
     * @return Response.
     */
    public Response emptyResultSet(Object resp) {
        return Response.status(Response.Status.NOT_FOUND).entity(resp).cacheControl(cc).build();
    }

    /**
     * Return a bad format request response.
     *
     * @param msg the message of the response.
     * @return Response.
     */
    public Response badRequest(String msg) {
        final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
        return Response.status(Response.Status.BAD_REQUEST).entity(resp).cacheControl(cc).build();
    }

    /**
     * @param msg the String
     * @return Response
     */
    public Response notFoundPojo(String msg) {
        final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.INFO, msg);
        return Response.status(Response.Status.NOT_FOUND).entity(resp).cacheControl(cc).build();
    }

    /**
     * @param msg the String
     * @return Response
     */
    public Response alreadyExistsPojo(String msg) {
        final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.INFO, msg);
        return Response.status(Response.Status.SEE_OTHER).entity(resp).build();
    }

    /**
     * @param msg the String
     * @return Response
     */
    public Response internalError(String msg) {
        // Exception, send a 500.
        final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).cacheControl(cc).build();
    }
}
