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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;

import es.hiiberia.simpatico.utils.SimpaticoProperties;
import es.hiiberia.simpatico.utils.Utils;

@Path("/ife")
public class SimpaticoResourceIFE {

	private static String ES_INDEX = SimpaticoProperties.elasticSearchSharedIndex;
	private static String ES_TYPE =  "IFE";
	private static String ES_FIELD_SEARCH = SimpaticoProperties.elasticSearchFieldSearch;
	private static String FILE_LOG = SimpaticoProperties.simpaticoLog_Logs;
	private static String THIS_RESOURCE = "IFE";
	
	
	private static String USER_ID = "userID";
	private static String E_SERVICE_ID = "e-serviceID";
	private static String FORM_ID = "formID";
	private static String TIMESTAMP = "timestamp";
	private static String SESSION_DURATION = "sessionDuration";
	private static String AVERAGE_TIME = "averageTime";
	private static String ANNOTABLE_ELEMENT_ID = "annotableElementID";
	private static String CLICKS = "clicks";
	
	private static String EVENT = "event";
	private static String EVENT_SESSION_START = "session_start";
	private static String EVENT_SESSION_END = "session_end";
	private static String EVENT_FORM_START = "form_start";
	public static String EVENT_FORM_END = "form_end";
	private static String EVENT_ELEMENT_CLICKS = "elements_clicks";
	
	private static int numLinesPrintStackInternalError = 1;
	
	@GET
    @Path("/find/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response find_force(@Context HttpServletRequest request, @Context UriInfo uriInfo) {

		try {
	    	Map<String, List<String>> queryParams = uriInfo.getQueryParameters();
	    	
	    	return SimpaticoResourceUtils.findRequest(request, queryParams, ES_INDEX, ES_TYPE, ES_FIELD_SEARCH, FILE_LOG, THIS_RESOURCE);
		} catch (Exception e) {
			SimpaticoResourceUtils.logException(e, FILE_LOG, THIS_RESOURCE);
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse + ": " + SimpaticoResourceUtils.getInternalErrorMessageWithStackTrace(e, numLinesPrintStackInternalError));
    	}
    }
	
    @GET
    @Path("/find/sessionstart")
    @Produces(MediaType.APPLICATION_JSON)
    public Response find_sessionstart(@Context HttpServletRequest request, @Context UriInfo uriInfo) {
    	
    	try {
    		// Copy map (it is unmodificable)
	    	Map<String, List<String>> queryParamsUnmodificable = uriInfo.getQueryParameters();
	    	Map<String, List<String>> queryParams = new MultivaluedHashMap<>();
	    	
	    	if (queryParamsUnmodificable != null)
	    		queryParams.putAll(queryParamsUnmodificable);
	    	
	    	List<String> words = queryParams.get(SimpaticoResourceUtils.wordsParam);
			if (words == null) {
				words = new ArrayList<>();
				words.add(EVENT_SESSION_START);
				queryParams.put(SimpaticoResourceUtils.wordsParam, words);
			} else {
				words.add(EVENT_SESSION_START);
			}	
        	
	    	return SimpaticoResourceUtils.findRequest(request, queryParams, ES_INDEX, ES_TYPE, ES_FIELD_SEARCH, FILE_LOG, THIS_RESOURCE);
    	} catch (Exception e) {
			SimpaticoResourceUtils.logException(e, FILE_LOG, THIS_RESOURCE);
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse + ": " + SimpaticoResourceUtils.getInternalErrorMessageWithStackTrace(e, numLinesPrintStackInternalError));
    	}
    }
    
    @GET
    @Path("/find/sessionend")
    @Produces(MediaType.APPLICATION_JSON)
    public Response find_sessionend(@Context HttpServletRequest request, @Context UriInfo uriInfo) {

    	try {
    		// Copy map (it is unmodificable)
	    	Map<String, List<String>> queryParamsUnmodificable = uriInfo.getQueryParameters();
	    	Map<String, List<String>> queryParams = new MultivaluedHashMap<>();
	    	
	    	if (queryParamsUnmodificable != null)
	    		queryParams.putAll(queryParamsUnmodificable);
	    	List<String> words = queryParams.get(SimpaticoResourceUtils.wordsParam);
			if (words == null) {
				words = new ArrayList<>();
				words.add(EVENT_SESSION_END);
				queryParams.put(SimpaticoResourceUtils.wordsParam, words);
			} else {
				words.add(EVENT_SESSION_END);
			}
	    	
	    	return SimpaticoResourceUtils.findRequest(request, queryParams, ES_INDEX, ES_TYPE, ES_FIELD_SEARCH, FILE_LOG, THIS_RESOURCE);
    	} catch (Exception e) {
			SimpaticoResourceUtils.logException(e, FILE_LOG, THIS_RESOURCE);
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse + ": " + SimpaticoResourceUtils.getInternalErrorMessageWithStackTrace(e, numLinesPrintStackInternalError));
    	}
    }
    
    @GET
    @Path("/find/formstart")
    @Produces(MediaType.APPLICATION_JSON)
    public Response find_formstart(@Context HttpServletRequest request, @Context UriInfo uriInfo) {
    	
    	try {
    		// Copy map (it is unmodificable)
	    	Map<String, List<String>> queryParamsUnmodificable = uriInfo.getQueryParameters();
	    	Map<String, List<String>> queryParams = new MultivaluedHashMap<>();
	    	
	    	if (queryParamsUnmodificable != null)
	    		queryParams.putAll(queryParamsUnmodificable);
	    	
	    	List<String> words = queryParams.get(SimpaticoResourceUtils.wordsParam);
			if (words == null) {
				words = new ArrayList<>();
				words.add(EVENT_FORM_START);
				queryParams.put(SimpaticoResourceUtils.wordsParam, words);
			} else {
				words.add(EVENT_FORM_START);
			}
			
	    	return SimpaticoResourceUtils.findRequest(request, queryParams, ES_INDEX, ES_TYPE, ES_FIELD_SEARCH, FILE_LOG, THIS_RESOURCE);
    	} catch (Exception e) {
			SimpaticoResourceUtils.logException(e, FILE_LOG, THIS_RESOURCE);
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse + ": " + SimpaticoResourceUtils.getInternalErrorMessageWithStackTrace(e, numLinesPrintStackInternalError));
    	}
    }
    
    @GET
    @Path("/find/formend")
    @Produces(MediaType.APPLICATION_JSON)
    public Response find_formend(@Context HttpServletRequest request, @Context UriInfo uriInfo) {

    	try {
    		// Copy map (it is unmodificable)
	    	Map<String, List<String>> queryParamsUnmodificable = uriInfo.getQueryParameters();
	    	Map<String, List<String>> queryParams = new MultivaluedHashMap<>();
	    	
	    	if (queryParamsUnmodificable != null)
	    		queryParams.putAll(queryParamsUnmodificable);
	    	List<String> words = queryParams.get(SimpaticoResourceUtils.wordsParam);
			if (words == null) {
				words = new ArrayList<>();
				words.add(EVENT_FORM_END);
				queryParams.put(SimpaticoResourceUtils.wordsParam, words);
			} else {
				words.add(EVENT_FORM_END);
			}
	    	
	    	return SimpaticoResourceUtils.findRequest(request, queryParams, ES_INDEX, ES_TYPE, ES_FIELD_SEARCH, FILE_LOG, THIS_RESOURCE);
    	} catch (Exception e) {
			SimpaticoResourceUtils.logException(e, FILE_LOG, THIS_RESOURCE);
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse + ": " + SimpaticoResourceUtils.getInternalErrorMessageWithStackTrace(e, numLinesPrintStackInternalError));
    	}
    }
        
    @GET
    @Path("/find/clicks")
    @Produces(MediaType.APPLICATION_JSON)
    public Response find_clicks(@Context HttpServletRequest request, @Context UriInfo uriInfo) {

    	try {
    		// Copy map (it is unmodificable)
	    	Map<String, List<String>> queryParamsUnmodificable = uriInfo.getQueryParameters();
	    	Map<String, List<String>> queryParams = new MultivaluedHashMap<>();
	    	
	    	if (queryParamsUnmodificable != null)
	    		queryParams.putAll(queryParamsUnmodificable);
	    	List<String> words = queryParams.get(SimpaticoResourceUtils.wordsParam);
			if (words == null) {
				words = new ArrayList<>();
				words.add(EVENT_ELEMENT_CLICKS);
				queryParams.put(SimpaticoResourceUtils.wordsParam, words);
			} else {
				words.add(EVENT_ELEMENT_CLICKS);
			}
	    	
	    	return SimpaticoResourceUtils.findRequest(request, queryParams, ES_INDEX, ES_TYPE, ES_FIELD_SEARCH, FILE_LOG, THIS_RESOURCE);
    	} catch (Exception e) {
			SimpaticoResourceUtils.logException(e, FILE_LOG, THIS_RESOURCE);
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse + ": " + SimpaticoResourceUtils.getInternalErrorMessageWithStackTrace(e, numLinesPrintStackInternalError));
    	}
    }
    
    @POST
    @Path("/insert/sessionstart")
    @Produces(MediaType.APPLICATION_JSON)
    public Response insert_session_start(@Context HttpServletRequest request, String postData) {
    	
    	/* JSON: {userID: <string>, e-serviceID: <string>, timestamp: <datetime>}   												event: session_start */
		
		boolean badRequest = true;
		
		try {
			// Check parameters and generate event attribute
			JSONObject jsonObject = Utils.createJSONObjectIfValid(postData);
			if (jsonObject != null) {
				if (jsonObject.has(USER_ID) && jsonObject.has(E_SERVICE_ID) && jsonObject.has(TIMESTAMP)) {
					badRequest = false;
					jsonObject.put(EVENT, EVENT_SESSION_START);
				}
			}
			
			if (!badRequest) {    	
				
				return SimpaticoResourceUtils.insertRequest(request, jsonObject.toString(), ES_INDEX, ES_TYPE, ES_FIELD_SEARCH, FILE_LOG, THIS_RESOURCE);
			}
			
			Logger.getLogger(FILE_LOG).warn("[BAD REQUEST] Insert document. IP Remote: " + request.getRemoteAddr() + ". IP Header Real: " + SimpaticoResourceUtils.getRealIPHeader(request) + ". POST data: " + postData);
			return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverBadRequestCode, SimpaticoResourceUtils.badPOSTRequestResponse);
			
		} catch (Exception e) {
			SimpaticoResourceUtils.logException(e, FILE_LOG, THIS_RESOURCE);
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse + ": " + SimpaticoResourceUtils.getInternalErrorMessageWithStackTrace(e, numLinesPrintStackInternalError));
    	}
    }
    
    @POST
    @Path("/insert/sessionend")
    @Produces(MediaType.APPLICATION_JSON)
    public Response insert_session_end(@Context HttpServletRequest request, String postData) {
    	
    	/* JSON: {userID: <string>, e-serviceID: <string>, timestamp: <datetime>, sessionDuration: <int>, averageTime: <float>}   	event: session_end		*/
		
		boolean badRequest = true;
		
		try {
			// Check parameters and generate event attribute
			JSONObject jsonObject = Utils.createJSONObjectIfValid(postData);
			if (jsonObject != null) {
				if (jsonObject.has(USER_ID) && jsonObject.has(E_SERVICE_ID) && jsonObject.has(TIMESTAMP) 
						&& jsonObject.has(SESSION_DURATION) && jsonObject.has(AVERAGE_TIME)) {
					badRequest = false;
					jsonObject.put(EVENT, EVENT_SESSION_END);
				}
			}
			
			if (!badRequest) {    	
				
				return SimpaticoResourceUtils.insertRequest(request, jsonObject.toString(), ES_INDEX, ES_TYPE, ES_FIELD_SEARCH, FILE_LOG, THIS_RESOURCE);
			}
			
			Logger.getLogger(FILE_LOG).warn("[BAD REQUEST] Insert document. IP Remote: " + request.getRemoteAddr() + ". IP Header Real: " + SimpaticoResourceUtils.getRealIPHeader(request) + ". POST data: " + postData);
			return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverBadRequestCode, SimpaticoResourceUtils.badPOSTRequestResponse);
			
		} catch (Exception e) {
			SimpaticoResourceUtils.logException(e, FILE_LOG, THIS_RESOURCE);
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse + ": " + SimpaticoResourceUtils.getInternalErrorMessageWithStackTrace(e, numLinesPrintStackInternalError));
    	}
    }
    
    @POST
    @Path("/insert/formstart")
    @Produces(MediaType.APPLICATION_JSON)
    public Response insert_form_start(@Context HttpServletRequest request, String postData) {
    	
    	/* JSON: {userID: <string>, e-serviceID: <string>, formID: <string>, timestamp: <datetime>}   								event: form_start */
		
		boolean badRequest = true;
		
		try {
			// Check parameters and generate event attribute
			JSONObject jsonObject = Utils.createJSONObjectIfValid(postData);
			
			if (jsonObject != null) {
				if (jsonObject.has(USER_ID) && jsonObject.has(E_SERVICE_ID) && jsonObject.has(FORM_ID) 
						&& jsonObject.has(TIMESTAMP)) {
					badRequest = false;
					jsonObject.put(EVENT, EVENT_FORM_START);
				}
			}
			
			if (!badRequest) {    	
				
				return SimpaticoResourceUtils.insertRequest(request, jsonObject.toString(), ES_INDEX, ES_TYPE, ES_FIELD_SEARCH, FILE_LOG, THIS_RESOURCE);
			}
			
			Logger.getLogger(FILE_LOG).warn("[BAD REQUEST] Insert document. IP Remote: " + request.getRemoteAddr() + ". IP Header Real: " + SimpaticoResourceUtils.getRealIPHeader(request) + ". POST data: " + postData);
			return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverBadRequestCode, SimpaticoResourceUtils.badPOSTRequestResponse);
			
		} catch (Exception e) {
			SimpaticoResourceUtils.logException(e, FILE_LOG, THIS_RESOURCE);
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse + ": " + SimpaticoResourceUtils.getInternalErrorMessageWithStackTrace(e, numLinesPrintStackInternalError));
    	}
    }
    
    @POST
    @Path("/insert/formend")
    @Produces(MediaType.APPLICATION_JSON)
    public Response insert_form_end(@Context HttpServletRequest request, String postData) {
    	
    	/* JSON: {userID: <string>, e-serviceID: <string>, formID: <string>, timestamp: <datetime>}   								event: form_end   	*/

		boolean badRequest = true;
		
		try {
			// Check parameters and generate event attribute
			JSONObject jsonObject = Utils.createJSONObjectIfValid(postData);
			if (jsonObject.has(USER_ID) && jsonObject.has(E_SERVICE_ID) && jsonObject.has(FORM_ID) 
    				&& jsonObject.has(TIMESTAMP)) {
    			badRequest = false;
    			jsonObject.put(EVENT, EVENT_FORM_END);
    		}
			
			if (!badRequest) {    	
				
				return SimpaticoResourceUtils.insertRequest(request, jsonObject.toString(), ES_INDEX, ES_TYPE, ES_FIELD_SEARCH, FILE_LOG, THIS_RESOURCE);
			}
			
			Logger.getLogger(FILE_LOG).warn("[BAD REQUEST] Insert document. IP Remote: " + request.getRemoteAddr() + ". IP Header Real: " + SimpaticoResourceUtils.getRealIPHeader(request) + ". POST data: " + postData);
			return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverBadRequestCode, SimpaticoResourceUtils.badPOSTRequestResponse);
			
		} catch (Exception e) {
			SimpaticoResourceUtils.logException(e, FILE_LOG, THIS_RESOURCE);
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse + ": " + SimpaticoResourceUtils.getInternalErrorMessageWithStackTrace(e, numLinesPrintStackInternalError));
    	}
    }
    
    @POST
    @Path("/insert/clicks")
    @Produces(MediaType.APPLICATION_JSON)
    public Response insert_elements_clicks(@Context HttpServletRequest request, String postData) {
    	
    	/* JSON: {userID: <string>, e-serviceID: <string>, annotableElementID: <string>, clicks: <datetime>}   						event: elements_clicks
    	*/

		boolean badRequest = true;
		
		try {
			// Check parameters and generate event attribute
			JSONObject jsonObject = Utils.createJSONObjectIfValid(postData);
			if (jsonObject != null) {
				if (jsonObject.has(USER_ID) && jsonObject.has(E_SERVICE_ID) && jsonObject.has(ANNOTABLE_ELEMENT_ID)
						&& jsonObject.has(CLICKS)) {
					badRequest = false;
					jsonObject.put(EVENT, EVENT_ELEMENT_CLICKS);
				}
			}
			
			if (!badRequest) {    	
				
				return SimpaticoResourceUtils.insertRequest(request, jsonObject.toString(), ES_INDEX, ES_TYPE, ES_FIELD_SEARCH, FILE_LOG, THIS_RESOURCE);
			}
			
			Logger.getLogger(FILE_LOG).warn("[BAD REQUEST] Insert document. IP Remote: " + request.getRemoteAddr() + ". IP Header Real: " + SimpaticoResourceUtils.getRealIPHeader(request) + ". POST data: " + postData);
			return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverBadRequestCode, SimpaticoResourceUtils.badPOSTRequestResponse);
			
		} catch (Exception e) {
			SimpaticoResourceUtils.logException(e, FILE_LOG, THIS_RESOURCE);
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse + ": " + SimpaticoResourceUtils.getInternalErrorMessageWithStackTrace(e, numLinesPrintStackInternalError));
    	}
    }
    
    /**
     * Force insert a json document. The postData must be a valid json (we store the full json)
     * @param request
     * @param postData
     * @return
     */
    @POST
    @Path("/insert/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response insert_force(@Context HttpServletRequest request, String postData) {
    	
    	// No check params    	
    	try {
    		
    		return SimpaticoResourceUtils.insertRequest(request, postData, ES_INDEX, ES_TYPE, ES_FIELD_SEARCH, FILE_LOG, THIS_RESOURCE);
    	} catch (Exception e) {
			SimpaticoResourceUtils.logException(e, FILE_LOG, THIS_RESOURCE);
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse + ": " + SimpaticoResourceUtils.getInternalErrorMessageWithStackTrace(e, numLinesPrintStackInternalError));
    	}
    }    
        

    @PUT
    @Path("/update/sessionstart")
    @Produces(MediaType.APPLICATION_JSON)
    public Response update_session_start(@Context HttpServletRequest request, String postData) {
    	
    	/* JSON: {userID: <string>, e-serviceID: <string>, timestamp: <datetime>}   												event: session_start */
		
		boolean badRequest = true;
		
		try {
			// Check parameters and generate event attribute
			JSONObject jsonObject = Utils.createJSONObjectIfValid(postData);
			if (jsonObject != null) {
				if (jsonObject.has(USER_ID) && jsonObject.has(E_SERVICE_ID) && jsonObject.has(TIMESTAMP)) { // 4 = id attr + others
					badRequest = false;
					jsonObject.put(EVENT, EVENT_SESSION_START);
				}
			}
			
			if (!badRequest) {    	
				
				return SimpaticoResourceUtils.updateRequest(request, jsonObject.toString(), ES_INDEX, ES_TYPE, ES_FIELD_SEARCH, FILE_LOG, THIS_RESOURCE);
			}
			
			Logger.getLogger(FILE_LOG).warn("[BAD REQUEST] Insert document. IP Remote: " + request.getRemoteAddr() + ". IP Header Real: " + SimpaticoResourceUtils.getRealIPHeader(request) + ". POST data: " + postData);
			return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverBadRequestCode, SimpaticoResourceUtils.badPOSTRequestResponse);
			
		} catch (Exception e) {
			SimpaticoResourceUtils.logException(e, FILE_LOG, THIS_RESOURCE);
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse + ": " + SimpaticoResourceUtils.getInternalErrorMessageWithStackTrace(e, numLinesPrintStackInternalError));
    	}
    }
    
    @PUT
    @Path("/update/sessionend")
    @Produces(MediaType.APPLICATION_JSON)
    public Response update_session_end(@Context HttpServletRequest request, String postData) {
    	
    	/* JSON: {userID: <string>, e-serviceID: <string>, timestamp: <datetime>, sessionDuration: <int>, averageTime: <float>}   	event: session_end		*/
		
		boolean badRequest = true;
		
		try {
			// Check parameters and generate event attribute
			JSONObject jsonObject = Utils.createJSONObjectIfValid(postData);
			if (jsonObject != null) {
				if (jsonObject.has(USER_ID) && jsonObject.has(E_SERVICE_ID) && jsonObject.has(TIMESTAMP)  
						&& jsonObject.has(SESSION_DURATION) && jsonObject.has(AVERAGE_TIME)) { // 5 = id attr + others
					badRequest = false;
					jsonObject.put(EVENT, EVENT_SESSION_END);
				}
			}
			
			if (!badRequest) {    	
				
				return SimpaticoResourceUtils.updateRequest(request, jsonObject.toString(), ES_INDEX, ES_TYPE, ES_FIELD_SEARCH, FILE_LOG, THIS_RESOURCE);
			}
			
			Logger.getLogger(FILE_LOG).warn("[BAD REQUEST] Insert document. IP Remote: " + request.getRemoteAddr() + ". IP Header Real: " + SimpaticoResourceUtils.getRealIPHeader(request) + ". POST data: " + postData);
			return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverBadRequestCode, SimpaticoResourceUtils.badPOSTRequestResponse);
			
		} catch (Exception e) {
			SimpaticoResourceUtils.logException(e, FILE_LOG, THIS_RESOURCE);
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse + ": " + SimpaticoResourceUtils.getInternalErrorMessageWithStackTrace(e, numLinesPrintStackInternalError));
    	}
    }
    
    @PUT
    @Path("/update/formstart")
    @Produces(MediaType.APPLICATION_JSON)
    public Response update_form_start(@Context HttpServletRequest request, String postData) {
    	
    	/* JSON: {userID: <string>, e-serviceID: <string>, formID: <string>, timestamp: <datetime>}   								event: form_start */
		
		boolean badRequest = true;
		
		try {
			// Check parameters and generate event attribute
			JSONObject jsonObject = Utils.createJSONObjectIfValid(postData);
			if (jsonObject != null) {
				if (jsonObject.has(USER_ID) && jsonObject.has(E_SERVICE_ID) && jsonObject.has(FORM_ID) 
						&& jsonObject.has(TIMESTAMP)) {
					badRequest = false;
					jsonObject.put(EVENT, EVENT_FORM_START);
				}
			}
			
			if (!badRequest) {    	
				
				return SimpaticoResourceUtils.updateRequest(request, jsonObject.toString(), ES_INDEX, ES_TYPE, ES_FIELD_SEARCH, FILE_LOG, THIS_RESOURCE);
			}
			
			Logger.getLogger(FILE_LOG).warn("[BAD REQUEST] Insert document. IP Remote: " + request.getRemoteAddr() + ". IP Header Real: " + SimpaticoResourceUtils.getRealIPHeader(request) + ". POST data: " + postData);
			return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverBadRequestCode, SimpaticoResourceUtils.badPOSTRequestResponse);
			
		} catch (Exception e) {
			SimpaticoResourceUtils.logException(e, FILE_LOG, THIS_RESOURCE);
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse + ": " + SimpaticoResourceUtils.getInternalErrorMessageWithStackTrace(e, numLinesPrintStackInternalError));
    	}
    }
    
    @PUT
    @Path("/update/formend")
    @Produces(MediaType.APPLICATION_JSON)
    public Response update_form_end(@Context HttpServletRequest request, String postData) {
    	
    	/* JSON: {userID: <string>, e-serviceID: <string>, formID: <string>, timestamp: <datetime>}   								event: form_end   	*/

		boolean badRequest = true;
		
		try {
			// Check parameters and generate event attribute
			JSONObject jsonObject = Utils.createJSONObjectIfValid(postData);
			if (jsonObject.has(USER_ID) && jsonObject.has(E_SERVICE_ID) && jsonObject.has(FORM_ID) 
    				&& jsonObject.has(TIMESTAMP)) {
    			badRequest = false;
    			jsonObject.put(EVENT, EVENT_FORM_END);
    		}
			
			if (!badRequest) {    	
				
				return SimpaticoResourceUtils.updateRequest(request, jsonObject.toString(), ES_INDEX, ES_TYPE, ES_FIELD_SEARCH, FILE_LOG, THIS_RESOURCE);
			}
			
			Logger.getLogger(FILE_LOG).warn("[BAD REQUEST] Insert document. IP Remote: " + request.getRemoteAddr() + ". IP Header Real: " + SimpaticoResourceUtils.getRealIPHeader(request) + ". POST data: " + postData);
			return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverBadRequestCode, SimpaticoResourceUtils.badPOSTRequestResponse);
			
		} catch (Exception e) {
			SimpaticoResourceUtils.logException(e, FILE_LOG, THIS_RESOURCE);
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse + ": " + SimpaticoResourceUtils.getInternalErrorMessageWithStackTrace(e, numLinesPrintStackInternalError));
    	}
    }
    
    @PUT
    @Path("/update/clicks")
    @Produces(MediaType.APPLICATION_JSON)
    public Response update_elements_clicks(@Context HttpServletRequest request, String postData) {
    	
    	/* JSON: {userID: <string>, e-serviceID: <string>, annotableElementID: <string>, clicks: <datetime>}   						event: elements_clicks
    	*/

		boolean badRequest = true;
		
		try {
			// Check parameters and generate event attribute
			JSONObject jsonObject = Utils.createJSONObjectIfValid(postData);
			if (jsonObject != null) {
				if (jsonObject.has(USER_ID) && jsonObject.has(E_SERVICE_ID) && jsonObject.has(ANNOTABLE_ELEMENT_ID)
						&& jsonObject.has(CLICKS)) {
					badRequest = false;
					jsonObject.put(EVENT, EVENT_ELEMENT_CLICKS);
				}
			}
			
			if (!badRequest) {    	
				
				return SimpaticoResourceUtils.updateRequest(request, jsonObject.toString(), ES_INDEX, ES_TYPE, ES_FIELD_SEARCH, FILE_LOG, THIS_RESOURCE);
			}
			
			Logger.getLogger(FILE_LOG).warn("[BAD REQUEST] Insert document. IP Remote: " + request.getRemoteAddr() + ". IP Header Real: " + SimpaticoResourceUtils.getRealIPHeader(request) + ". POST data: " + postData);
			return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverBadRequestCode, SimpaticoResourceUtils.badPOSTRequestResponse);
			
		} catch (Exception e) {
			SimpaticoResourceUtils.logException(e, FILE_LOG, THIS_RESOURCE);
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse + ": " + SimpaticoResourceUtils.getInternalErrorMessageWithStackTrace(e, numLinesPrintStackInternalError));
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
    @Path("/update/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response update_force(@Context HttpServletRequest request, String postData) {
    	
    	// No Check params
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
