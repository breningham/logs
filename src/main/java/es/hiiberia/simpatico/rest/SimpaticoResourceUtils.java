package es.hiiberia.simpatico.rest;

import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;

public class SimpaticoResourceUtils {

	public static String internalErrorResponse = "Internal error";
	public static String badPOSTRequestResponse = "Bad request. Post data must be json";
	public static String badParamsRequestResponse = "Bad params request.";
	public static String dataInsertedESResponse = "Data inserted";
	public static String dataUpdatedESResponse = "Data updated";
	public static String dataRemovedESResponse = "Data removed";
	
	// Variables message response
	public static String responseJSONResults = "results";		// JSON array of each result
	public static String responseJSONNumResults = "count";		// Num results founded in search
	public static String responseJSONId = "id";					// Each document id 
	public static String responseJSONData = "data";				// Each document data
	public static String responseJSONScore = "score";			// Each document score
	
	// Public params
	/* find */
	public static String wordsParam = "words";
	public static String limitParam = "limit";
	public static String sortASCParam = "sortasc";
	public static String sortDESCParam = "sortdesc";
	/* insert */
	public static String _idParam = "_id";
	/* update, remove */
	public static String idParam = "id";
	/* update */
	public static String contentParam = "content";
	
	public static String separateParam = ",";
	
	// Server codes
	public static int serverOkCode = 200;
	public static int serverCreatedCode = 201;
	public static int serverNoContentCode = 204;
	public static int serverBadRequestCode = 400;
	public static int serverInternalServerErrorCode = 500;
	
	
	/**
     * Method to convert elastic search response query to JSON response web service 
     * @param sr
     * @return
     * @throws JSONException
     */
    public static JSONObject searchResponse2JSONResponse(SearchResponse sr) throws JSONException {
    	
    	JSONObject jsonResponse = new JSONObject();
    	JSONObject jsonObj;
    	JSONArray jsonArray = new JSONArray();
    	String id, source;
    	Float score;
    	
    	int numHits = 0;
	
    	// Each hit (result elastic search)
    	for (SearchHit hit : sr.getHits().getHits()) {
    		id = hit.getId();
    		score = hit.getScore();
    		source = hit.getSourceAsString();    		
    		numHits++;
    		
    		// Create json object
    		jsonObj = new JSONObject();
    		if (id != null && !id.isEmpty())
    			jsonObj.put(responseJSONId, id);
    		if (score != null && !score.isNaN() && !score.isInfinite()) 
    			jsonObj.put(responseJSONScore, score);
    		if (source != null && !source.isEmpty())
    			jsonObj.put(responseJSONData, new JSONObject(source));
    		if (jsonObj.length() > 0) // json object has at least one key
    			jsonArray.put(jsonObj);
    	}
    	
    	jsonResponse.put(responseJSONNumResults, numHits);
    	jsonResponse.put(responseJSONResults, jsonArray);
    	
    	return jsonResponse;
    }
    
    /**
     * Create message JSON response with code status
     */
    public static Response createMessageResponse (int status, String message) {
    	// {"message": <message>}
    	String msg = "{\"message\": \"" + message.trim() + "\"}";
    	return Response.status(status).entity(msg).build();
    }
    
    /**
     * Create message JSON response with 200 code status 
     */
    public static Response createMessageResponse (JSONObject json) {
    	return Response.status(serverOkCode).entity(json.toString()).build();
    }
}
