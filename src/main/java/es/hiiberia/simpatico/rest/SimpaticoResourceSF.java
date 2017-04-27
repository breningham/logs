package es.hiiberia.simpatico.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;

import es.hiiberia.simpatico.utils.Forms;
import es.hiiberia.simpatico.utils.SimpaticoProperties;
import es.hiiberia.simpatico.utils.Utils;

@Path("/sf")
public class SimpaticoResourceSF {

	private static String ES_INDEX = SimpaticoProperties.elasticSearchHIIndex;
	private static String ES_TYPE =  "SF";
	private static String ES_FIELD_SEARCH = SimpaticoProperties.elasticSearchFieldSearch;
	private static String FILE_LOG = SimpaticoProperties.simpaticoLog_Logs;
	private static String THIS_RESOURCE = "SF";
	
	
	private static String USER_ID = "userID";
	private static String E_SERVICE_ID = "e-serviceID";
	private static String COMPLEXITY = "complexity";
	
	private static String EVENT = "event";
	private static String EVENT_SESSION_FEEDBACK = "session_feedback";
	
	private static int numLinesPrintStackInternalError = 1;
	
    @GET
    @Path("/find")
    @Produces(MediaType.APPLICATION_JSON)
    public Response find_sf(@Context HttpServletRequest request, @Context UriInfo uriInfo) {
    	
    	try {
    		// Copy map (it is unmodificable)
	    	Map<String, List<String>> queryParamsUnmodificable = uriInfo.getQueryParameters();
	    	Map<String, List<String>> queryParams = new MultivaluedHashMap<>();
	    	
	    	if (queryParamsUnmodificable != null)
	    		queryParams.putAll(queryParamsUnmodificable);
	    	
	    	List<String> words = queryParams.get(SimpaticoResourceUtils.wordsParam);
			if (words == null) {
				words = new ArrayList<>();
				words.add(EVENT_SESSION_FEEDBACK);
				queryParams.put(SimpaticoResourceUtils.wordsParam, words);
			} else {
				words.add(EVENT_SESSION_FEEDBACK);
			}
			
	    	return SimpaticoResourceUtils.findRequest(request, queryParams, ES_INDEX, ES_TYPE, ES_FIELD_SEARCH, FILE_LOG, THIS_RESOURCE);
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
    @Path("/insert")
    @Produces(MediaType.APPLICATION_JSON)
    public Response insert(@Context HttpServletRequest request, String postData) {
    	/* JSON: {userID: <string>, e-serviceID: <string>, complexity: <string>}   	event: session_feedback
    	*/
    	
    	boolean badRequest = true;
    	
    	try {
	    	// Check parameters and generate event attribute
	    	JSONObject jsonObject = Utils.createJSONObjectIfValid(postData);
	    	if (jsonObject != null) {
	    		if (jsonObject.has(USER_ID) && jsonObject.has(E_SERVICE_ID) && jsonObject.has(COMPLEXITY)) {
    				badRequest = false;
    				jsonObject.put(EVENT, EVENT_SESSION_FEEDBACK);
	    		}
	    	}
	    	
	    	if (!badRequest) {    		
	    		return SimpaticoResourceUtils.insertRequest(request, jsonObject.toString(), ES_INDEX, ES_TYPE, ES_FIELD_SEARCH, FILE_LOG, THIS_RESOURCE);
	    	}
	    	
	    	Logger.getLogger(FILE_LOG).warn("[BAD REQUEST] Insert document. IP Remote: " + request.getRemoteAddr() + ". POST data: " + postData);
			return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverBadRequestCode, SimpaticoResourceUtils.badPOSTRequestResponse);
			
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
    @Path("/update")
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(@Context HttpServletRequest request, String postData) {
    	// Check params like insert
    	boolean badRequest = true;
    	
    	try {
	    	// Check parameters and generate event attribute
	    	JSONObject jsonObject = Utils.createJSONObjectIfValid(postData);
	    	if (jsonObject != null) {
	    		if (jsonObject != null) {
		    		if (jsonObject.has(USER_ID) && jsonObject.has(E_SERVICE_ID) && jsonObject.has(COMPLEXITY)) {
	    				badRequest = false;
	    				jsonObject.put(EVENT, EVENT_SESSION_FEEDBACK);
		    		}
		    	}
	    	}
	    	
	    	if (!badRequest) {    	

	    		return SimpaticoResourceUtils.updateRequest(request, jsonObject.toString(), ES_INDEX, ES_TYPE, ES_FIELD_SEARCH, FILE_LOG, THIS_RESOURCE);
	    	}
	    	
	    	Logger.getLogger(FILE_LOG).warn("[BAD REQUEST] Insert document. IP Remote: " + request.getRemoteAddr() + ". POST data: " + postData);
			return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverBadRequestCode, SimpaticoResourceUtils.badPOSTRequestResponse);
			
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
    @Path("/remove")
    @Produces(MediaType.APPLICATION_JSON)
    public Response remove(@Context HttpServletRequest request, String postData) {
    	try {
			return SimpaticoResourceUtils.removeRequest(request, postData, ES_INDEX, ES_TYPE, ES_FIELD_SEARCH, FILE_LOG, THIS_RESOURCE);
    	} catch (Exception e) {
			SimpaticoResourceUtils.logException(e, FILE_LOG, THIS_RESOURCE);
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse + ": " + SimpaticoResourceUtils.getInternalErrorMessageWithStackTrace(e, numLinesPrintStackInternalError));
    	}
    }
    
	
    @GET
    @Path("/selectdialog")
    @Produces(MediaType.TEXT_HTML)
    public String selectDialog(@QueryParam("id") String userId, @QueryParam("ctz") Boolean ctz, @QueryParam("simpl") Boolean simpl, @QueryParam("timeout") Boolean timeout,
    							@QueryParam("lang") String lang) {
    	// Initialize the forms with the correct language
    	Forms form = new Forms(lang);
    	
    	// Put pieces together
    	String result = form.getStartingDiv();
    	
    	if (ctz) result += form.getCtzPart();
    	if (simpl) result += form.getSimplificationPart();
    	if (timeout) result += form.getTimeoutPart();
    	
    	result += form.getCommonPart();
    	return result;
    }
    
    /** Test Method **/
    @GET
	@Path("/test")
	@Produces(MediaType.APPLICATION_JSON)
	public Response test() {
    	return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverOkCode, "Welcome to SIMPATICO " + THIS_RESOURCE + " API!");
	}
}