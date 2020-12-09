package hep.crest.server.swagger.api.impl;

import hep.crest.server.swagger.api.ApiResponseMessage;
import hep.crest.swagger.model.HTTPResponse;

import javax.ws.rs.core.Response;

public class ResponseFormatHelper {

    /**
     * Return a response using the HTTPResponse object in input.
     *
     * @param resp the body of the response.
     * @return Response.
     */
    public static Response createApiResponse(HTTPResponse resp) {
        if (resp.getCode().equals(Response.Status.CREATED.getStatusCode())) {
            return Response.status(Response.Status.CREATED).entity(resp).build();
        }
        else if (resp.getCode().equals(Response.Status.SEE_OTHER.getStatusCode())) {
            return Response.status(Response.Status.SEE_OTHER).entity(resp).build();
        }
        else if (resp.getCode().equals(Response.Status.BAD_REQUEST.getStatusCode())) {
            return Response.status(Response.Status.BAD_REQUEST).entity(resp).build();
        }
        else if (resp.getCode().equals(Response.Status.NOT_FOUND.getStatusCode())) {
            return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
        }
        return internalError("Cannot create response message");
    }

    /**
     * Return an empty result set.
     *
     * @param resp the body of the response.
     * @return Response.
     */
    public static Response emptyResultSet(Object resp) {
        return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
    }

    /**
     * Return a bad format request response.
     *
     * @param msg the message of the response.
     * @return Response.
     */
    public static Response badRequest(String msg) {
        final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
        return Response.status(Response.Status.BAD_REQUEST).entity(resp).build();
    }

    /**
     * @param msg the String
     * @return Response
     */
    public static Response notFoundPojo(String msg) {
        final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.INFO, msg);
        return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
    }

    /**
     * @param msg the String
     * @return Response
     */
    public static Response alreadyExistsPojo(String msg) {
        final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.INFO, msg);
        return Response.status(Response.Status.SEE_OTHER).entity(resp).build();
    }

    /**
     * @param msg the String
     * @return Response
     */
    public static Response internalError(String msg) {
        // Exception, send a 500.
        final ApiResponseMessage resp = new ApiResponseMessage(ApiResponseMessage.ERROR, msg);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(resp).build();
    }
}
