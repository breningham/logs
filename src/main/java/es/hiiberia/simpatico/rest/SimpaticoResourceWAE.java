package es.hiiberia.simpatico.rest;

import java.net.URI;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;

import es.hiiberia.simpatico.utils.SimpaticoProperties;
import es.hiiberia.simpatico.utils.Utils;

@Path("/wae")
public class SimpaticoResourceWAE {

	private static String ES_INDEX = SimpaticoProperties.elasticSearchLogsIndex;
	private static String ES_TYPE =  "WAE";
	private static String ES_FIELD_SEARCH = SimpaticoProperties.elasticSearchFieldSearch;
	private static String FILE_LOG = SimpaticoProperties.simpaticoLog_Logs;
	private static String THIS_RESOURCE = "WAE";
	
	
	private static String USER_ID = "userID";
	private static String E_SERVICE_ID = "e-serviceID";
	private static String TIMESTAMP = "timestamp";
	
	private static String EVENT = "event";
	private static String EVENT_WAE = "workflow_adaptation_request";
	
	
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
				words.add(EVENT_WAE);
				queryParams.put(SimpaticoResourceUtils.wordsParam, words);
			} else {
				words.add(EVENT_WAE);
			}
			
	    	return SimpaticoResourceUtils.findRequest(request, queryParams, ES_INDEX, ES_TYPE, ES_FIELD_SEARCH, FILE_LOG, THIS_RESOURCE);
    	} catch (Exception e) {
    		Logger.getLogger(FILE_LOG).error("Exception in " + THIS_RESOURCE + ": " + e.getMessage());
    		Logger.getRootLogger().error("Exception in " + THIS_RESOURCE + ": " + e.getMessage());
			Logger.getLogger(SimpaticoProperties.simpaticoLog_Error).error("Exception in " + THIS_RESOURCE + ": " + e.getMessage() + "\n" + SimpaticoResourceUtils.exceptionStringifyStack(e));
    		
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse);
    	}
    }
        
    @GET
    @Path("/find_force")
    @Produces(MediaType.APPLICATION_JSON)
    public Response find(@Context HttpServletRequest request, @Context UriInfo uriInfo) {

		try {
	    	Map<String, List<String>> queryParams = uriInfo.getQueryParameters();
	    	
	    	return SimpaticoResourceUtils.findRequest(request, queryParams, ES_INDEX, ES_TYPE, ES_FIELD_SEARCH, FILE_LOG, THIS_RESOURCE);
		} catch (Exception e) {
    		Logger.getLogger(FILE_LOG).error("Exception in " + THIS_RESOURCE + ": " + e.getMessage());
    		Logger.getRootLogger().error("Exception in " + THIS_RESOURCE + ": " + e.getMessage());
			Logger.getLogger(SimpaticoProperties.simpaticoLog_Error).error("Exception in " + THIS_RESOURCE + ": " + e.getMessage() + "\n" + SimpaticoResourceUtils.exceptionStringifyStack(e));
    		
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse);
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
    	/* JSON: {userID: <string>, e-serviceID: <string>, timestamp: <string>}   	event: workflow_adaptation_request
    	*/
    	
    	boolean badRequest = true;
    	
    	try {
	    	// Check parameters and generate event attribute
	    	JSONObject jsonObject = Utils.createJSONObjectIfValid(postData);
	    	if (jsonObject != null) {
	    		if (jsonObject.length() == 3 && jsonObject.has(USER_ID) && jsonObject.has(E_SERVICE_ID) && jsonObject.has(TIMESTAMP)) {
    				badRequest = false;
    				jsonObject.put(EVENT, EVENT_WAE);
	    		}
	    	}
	    	
	    	if (!badRequest) {    		
	    		return SimpaticoResourceUtils.insertRequest(request, jsonObject.toString(), ES_INDEX, ES_TYPE, ES_FIELD_SEARCH, FILE_LOG, THIS_RESOURCE);
	    	}
	    	
	    	Logger.getLogger(FILE_LOG).warn("[BAD REQUEST] Insert document. IP Remote: " + request.getRemoteAddr() + ". POST data: " + postData);
			return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverBadRequestCode, SimpaticoResourceUtils.badPOSTRequestResponse);
			
    	} catch (Exception e) {
    		Logger.getLogger(FILE_LOG).error("Exception in " + THIS_RESOURCE + ": " + e.getMessage());
    		Logger.getRootLogger().error("Exception in " + THIS_RESOURCE + ": " + e.getMessage());
			Logger.getLogger(SimpaticoProperties.simpaticoLog_Error).error("Exception in " + THIS_RESOURCE + ": " + e.getMessage() + "\n" + SimpaticoResourceUtils.exceptionStringifyStack(e));
    		
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse);
    	}
    }
    
    /**
     * Force insert a json document. The postData must be a valid json (we store the full json)
     * @param request
     * @param postData
     * @return
     */
    @POST
    @Path("/insert_force")
    @Produces(MediaType.APPLICATION_JSON)
    public Response insert_force(@Context HttpServletRequest request, String postData) {
    	
    	// No check params    	
    	try {
    		
    		return SimpaticoResourceUtils.insertRequest(request, postData, ES_INDEX, ES_TYPE, ES_FIELD_SEARCH, FILE_LOG, THIS_RESOURCE);
    	} catch (Exception e) {
    		Logger.getLogger(FILE_LOG).error("Exception in " + THIS_RESOURCE + ": " + e.getMessage());
    		Logger.getRootLogger().error("Exception in " + THIS_RESOURCE + ": " + e.getMessage());
			Logger.getLogger(SimpaticoProperties.simpaticoLog_Error).error("Exception in " + THIS_RESOURCE + ": " + e.getMessage() + "\n" + SimpaticoResourceUtils.exceptionStringifyStack(e));
    		
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse);
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
		    		if (jsonObject.length() == 4 && jsonObject.has(USER_ID) && jsonObject.has(E_SERVICE_ID) && jsonObject.has(TIMESTAMP)) { // Length + 1 : field "id"
	    				badRequest = false;
	    				jsonObject.put(EVENT, EVENT_WAE);
		    		}
		    	}
	    	}
	    	
	    	if (!badRequest) {    	

	    		return SimpaticoResourceUtils.updateRequest(request, jsonObject.toString(), ES_INDEX, ES_TYPE, ES_FIELD_SEARCH, FILE_LOG, THIS_RESOURCE);
	    	}
	    	
	    	Logger.getLogger(FILE_LOG).warn("[BAD REQUEST] Insert document. IP Remote: " + request.getRemoteAddr() + ". POST data: " + postData);
			return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverBadRequestCode, SimpaticoResourceUtils.badPOSTRequestResponse);
			
    	} catch (Exception e) {
    		Logger.getLogger(FILE_LOG).error("Exception in " + THIS_RESOURCE + ": " + e.getMessage());
    		Logger.getRootLogger().error("Exception in " + THIS_RESOURCE + ": " + e.getMessage());
			Logger.getLogger(SimpaticoProperties.simpaticoLog_Error).error("Exception in " + THIS_RESOURCE + ": " + e.getMessage() + "\n" + SimpaticoResourceUtils.exceptionStringifyStack(e));
    		
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse);
    	}
    }
    
    /**
     * Force update a json document. The postData must be a valid json and format: {"id": "<id to update>", "content": "<json>"}. 
     * If json has the same keys that the old document, the document updates.
     * If the document does not exists, it is created.
     * @param request
     * @param postData
     * @return
     */
    @PUT
    @Path("/update_force")
    @Produces(MediaType.APPLICATION_JSON)
    public Response update_force(@Context HttpServletRequest request, String postData) {
    	
    	// No Check params
    	try {

			return SimpaticoResourceUtils.updateRequest(request, postData, ES_INDEX, ES_TYPE, ES_FIELD_SEARCH, FILE_LOG, THIS_RESOURCE);
    	} catch (Exception e) {
    		Logger.getLogger(FILE_LOG).error("Exception in " + THIS_RESOURCE + ": " + e.getMessage());
    		Logger.getRootLogger().error("Exception in " + THIS_RESOURCE + ": " + e.getMessage());
			Logger.getLogger(SimpaticoProperties.simpaticoLog_Error).error("Exception in " + THIS_RESOURCE + ": " + e.getMessage() + "\n" + SimpaticoResourceUtils.exceptionStringifyStack(e));
    		
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse);
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
    	return SimpaticoResourceUtils.removeRequest(request, postData, ES_INDEX, ES_TYPE, ES_FIELD_SEARCH, FILE_LOG, THIS_RESOURCE);
    }
  
    
    /** Test Method **/
    @GET
	@Path("/test")
	@Produces(MediaType.APPLICATION_JSON)
	public Response test() {
    	return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverOkCode, "Welcome to SIMPATICO " + THIS_RESOURCE + " API!");
	}
    
    
    /** Method to redirect to web API **/
    @GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public void index() {
    	URI uri = UriBuilder.fromUri("http://127.0.0.1:8080/simpatico/").build();
    	Response.seeOther(uri);
	}
}
