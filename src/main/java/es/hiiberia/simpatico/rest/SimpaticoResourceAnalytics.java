package es.hiiberia.simpatico.rest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import es.hiiberia.simpatico.utils.SimpaticoProperties;

@Path("/analytics")
public class SimpaticoResourceAnalytics {

	private static String ES_INDEX = SimpaticoProperties.elasticSearchHIIndex;
	private static String ES_TYPE =  "DA";
	private static String ES_FIELD_SEARCH = SimpaticoProperties.elasticSearchFieldSearch;
	private static String FILE_LOG = SimpaticoProperties.simpaticoLog_Analytics;
	private static String THIS_RESOURCE = "Analytics";
	
	private static int numLinesPrintStackInternalError = 1;
	
	@GET
    @Path("/find/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response find(@Context HttpServletRequest request, @Context UriInfo uriInfo) {
    	try {
			return SimpaticoResourceUtils.findRequest(request, uriInfo, ES_INDEX, ES_TYPE, ES_FIELD_SEARCH, FILE_LOG, THIS_RESOURCE);
    	} catch (Exception e) {
			SimpaticoResourceUtils.logException(e, FILE_LOG, THIS_RESOURCE);
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse + ": " + SimpaticoResourceUtils.getInternalErrorMessageWithStackTrace(e, numLinesPrintStackInternalError));
    	}
    }
    
        
    /**
     * Insert a json document. The postData must be a valid json (we store the full json)
     * @param request
     * @param postData
     * @return
     */
    @POST
    @Path("/insert/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response insert(@Context HttpServletRequest request, String postData) {
    	try {
			return SimpaticoResourceUtils.insertRequest(request, postData, ES_INDEX, ES_TYPE, ES_FIELD_SEARCH, FILE_LOG, THIS_RESOURCE);
    	} catch (Exception e) {
			SimpaticoResourceUtils.logException(e, FILE_LOG, THIS_RESOURCE);
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse + ": " + SimpaticoResourceUtils.getInternalErrorMessageWithStackTrace(e, numLinesPrintStackInternalError));
    	}
    }
    
        
    /**
     * Update a json document. The postData must be a valid json and format: {"id": "<id to update>", "content": "<json>"}. 
     * If json has the same keys that the old document, the document updates.
     * If the document does not exists, it is created.
     * @param request
     * @param postData
     * @return
     */
    @PUT
    @Path("/update/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(@Context HttpServletRequest request, String postData) {
    	try {
			return SimpaticoResourceUtils.updateRequest(request, postData, ES_INDEX, ES_TYPE, ES_FIELD_SEARCH, FILE_LOG, THIS_RESOURCE);
    	} catch (Exception e) {
			SimpaticoResourceUtils.logException(e, FILE_LOG, THIS_RESOURCE);
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse + ": " + SimpaticoResourceUtils.getInternalErrorMessageWithStackTrace(e, numLinesPrintStackInternalError));
    	}
    }
    
    
    /**
     * Remove a json document. The postData must be a valid json and format: {"id": "<id to eliminate>"}
     * @param request
     * @param postData
     * @return
     */
    @DELETE
    @Path("/remove/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response remove(@Context HttpServletRequest request, String postData) {
    	try {
			return SimpaticoResourceUtils.removeRequest(request, postData, ES_INDEX, ES_TYPE, ES_FIELD_SEARCH, FILE_LOG, THIS_RESOURCE);
    	} catch (Exception e) {
			SimpaticoResourceUtils.logException(e, FILE_LOG, THIS_RESOURCE);
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse + ": " + SimpaticoResourceUtils.getInternalErrorMessageWithStackTrace(e, numLinesPrintStackInternalError));
    	}
    }
       
    
    /** Test Method **/
    @GET
	@Path("/test/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response testGet(@Context HttpServletRequest request) {
    	Logger.getLogger(FILE_LOG).warn("[TEST] IP Remote: " + request.getRemoteAddr() + ". IP Header Real: " + SimpaticoResourceUtils.getRealIPHeader(request));
    	return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverOkCode, "Welcome to SIMPATICO " + THIS_RESOURCE + " API! Method: GET");
	}
    
    @POST
	@Path("/test/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response testPost(@Context HttpServletRequest request) {
    	Logger.getLogger(FILE_LOG).warn("[TEST] IP Remote: " + request.getRemoteAddr() + ". IP Header Real: " + SimpaticoResourceUtils.getRealIPHeader(request));
    	return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverOkCode, "Welcome to SIMPATICO " + THIS_RESOURCE + " API! Method: POST");
	}
    
    @PUT
	@Path("/test/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response testPut(@Context HttpServletRequest request) {
    	Logger.getLogger(FILE_LOG).warn("[TEST] IP Remote: " + request.getRemoteAddr() + ". IP Header Real: " + SimpaticoResourceUtils.getRealIPHeader(request));
    	return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverOkCode, "Welcome to SIMPATICO " + THIS_RESOURCE + " API! Method: PUT");
	}
    
    @DELETE
   	@Path("/test/")
   	@Produces(MediaType.APPLICATION_JSON)
   	public Response testDelete(@Context HttpServletRequest request) {
    	Logger.getLogger(FILE_LOG).warn("[TEST] IP Remote: " + request.getRemoteAddr() + ". IP Header Real: " + SimpaticoResourceUtils.getRealIPHeader(request));
       	return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverOkCode, "Welcome to SIMPATICO " + THIS_RESOURCE + " API! Method: DELETE");
   	}
}
