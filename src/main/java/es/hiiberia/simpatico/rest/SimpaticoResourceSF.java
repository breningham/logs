package es.hiiberia.simpatico.rest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.sort.SortOrder;

import es.hiiberia.simpatico.utils.ElasticSearchConnector;
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
	    	
	    	Logger.getLogger(FILE_LOG).warn("[BAD REQUEST] Insert document. IP Remote: " + request.getRemoteAddr() + ". IP Header Real: " + SimpaticoResourceUtils.getRealIPHeader(request) + ". POST data: " + postData);
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
	    	
	    	Logger.getLogger(FILE_LOG).warn("[BAD REQUEST] Insert document. IP Remote: " + request.getRemoteAddr() + ". IP Header Real: " + SimpaticoResourceUtils.getRealIPHeader(request) + ". POST data: " + postData);
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
    
	
    @POST
    @Path("/selectdialog")
    @Produces(MediaType.TEXT_HTML)
    public String selectDialog(@QueryParam("id") String userId, @QueryParam("ctz") Boolean ctz, @QueryParam("simpl") Boolean simpl, @QueryParam("timeout") Boolean timeout,
    							@QueryParam("lang") String lang) {
    	// Initialize the forms with the correct language
    	Forms form = new Forms(lang);
    	boolean wordSimp = false;
    	boolean phraseSimp = false;
    	boolean paragraphSimp = false;
    	
    	// Search in ES what the user has done since he started the last session
    	try {
    		// XXX: Y si primero hacer una quuery para coger el último "session_start" y luego ya esta?
    		// No la hago todavía por el lío más abajo explicado del "must" y el "should"
        	List<String> words = new ArrayList<String>();
        	words.add("paragraph_simplification");
        	words.add("word_simplification");
        	words.add("free_text_simplification");
        	words.add("citizenpedia_content_request");
        	words.add("session_start");
        	// XXX: Al hacer la query, pone un "should" en vez de un "must". Me salen resultados con "form_start" también. Pero con "must" no me salen resultados
        	SearchResponse responseES = ElasticSearchConnector.getInstance().searchByField("shared", null, "event", words, "created", SortOrder.DESC, 30, userId);
        	JSONObject jsonRes = SimpaticoResourceUtils.searchResponse2JSONResponse(responseES);
        	
        	// Search in the results the info needed
        	// XXX: Dos bucles. Uno solo para el último "session_start" y otro para el resto de cosas. Se soluciona cuando se solucione lo de arriba
        	JSONArray results = jsonRes.getJSONArray("results");
        	String lastSession = "";
        	for (int i=0; i<results.length(); i++) {
        		JSONObject data = results.getJSONObject(i);
        		String event = data.getJSONObject("data").getString("event");
        		if (event.equals("session_start")) {
        			lastSession = data.getJSONObject("data").getString("created");
        			Logger.getRootLogger().info("lastSession: " + lastSession);
        			break;
        		}
        	}
        	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        	Date lastSessionDate = sdf.parse(lastSession);
        	for (int i=0; i<results.length(); i++) {
        		JSONObject data = results.getJSONObject(i);
        		String event = data.getJSONObject("data").getString("event");
        		String created = data.getJSONObject("data").getString("created");
        		Date date = sdf.parse(created);
        		if (event.equals("citizenpedia_content_request")) {
//        			Logger.getRootLogger().info("Citizenpedia event in ES search");
        			if (date.after(lastSessionDate)) {
//        				Logger.getRootLogger().info("Citizenpedia event AFTER last session");
        				ctz = true;
        			} else {
//        				Logger.getRootLogger().info("Citizenpedia event BEFORE last session");
        			}
        		}
        		
        		if (event.equals("paragraph_simplification")) {
//        			Logger.getRootLogger().info("Paragraph simplification event in ES search");
        			if (date.after(lastSessionDate)) {
//        				Logger.getRootLogger().info("Paragraph simplification event AFTER last session");
        				paragraphSimp = true;
        			} else {
//        				Logger.getRootLogger().info("Paragraph simplification event BEFORE last session");
        			}
        		}
        		
        		if (event.equals("word_simplification")) {
//        			Logger.getRootLogger().info("Word simplification event in ES search");
        			if (date.after(lastSessionDate)) {
//        				Logger.getRootLogger().info("Word simplification event AFTER last session");
        				wordSimp = true;
        			} else {
//        				Logger.getRootLogger().info("Word simplification event BEFORE last session");
        			}
        		}
        		
        		if (event.equals("free_text_simplification")) {
//        			Logger.getRootLogger().info("Free text simplification event in ES search");
        			if (date.after(lastSessionDate)) {
//        				Logger.getRootLogger().info("Free text simplification event AFTER last session");
        				phraseSimp = true;
        			} else {
//        				Logger.getRootLogger().info("Free text simplification event BEFORE last session");
        			}
        		}
        	}
		} catch (Exception e) {
			SimpaticoResourceUtils.logException(e, FILE_LOG, THIS_RESOURCE);
		}
    	
    	// Put pieces together
    	String result = form.getStartingDiv();
    	if (ctz) result += form.getCtzPart();
    	if (wordSimp) result += form.getWordSimplificationPart();
    	if (paragraphSimp) result += form.getParagraphSimplificationPart();
    	if (phraseSimp) result += form.getPhraseSimplificationPart();
    	if (timeout) result += form.getTimeoutPart();
    	
    	result += form.getCommonPart();
    	return result;
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