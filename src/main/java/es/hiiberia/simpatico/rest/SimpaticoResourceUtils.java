package es.hiiberia.simpatico.rest;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.engine.DocumentMissingException;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import es.hiiberia.simpatico.utils.ElasticSearchConnector;
import es.hiiberia.simpatico.utils.SimpaticoProperties;
import es.hiiberia.simpatico.utils.Utils;

public class SimpaticoResourceUtils {

	public static String internalErrorResponse = "Internal error";
	public static String badPOSTRequestResponse = "Bad request. Post data must be json";
	public static String badPOSTPiwikBodyResponse = "JSON body must have a value for key 'method'";
	public static String badPOSTPiwikMethod = "Bad 'method' value. Must be a valid one";
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
	
	public static String getInternalErrorMessageWithStackTrace(Exception e, int numLines) {
		return e.getMessage() + "\n" + SimpaticoResourceUtils.exceptionStringifyStack(e, numLines);
	}
	
	public static String exceptionStringify(Exception e) {
		String rt = "Exception message: " + e.getMessage() + "\n";
		StringWriter error = new StringWriter();
		e.printStackTrace(new PrintWriter(error));
		
		return rt += error.toString();
	}
	
	public static String exceptionStringifyStack(Exception e) {
		StringWriter error = new StringWriter();
		e.printStackTrace(new PrintWriter(error));
		
		return error.toString();
	}
	
	public static String exceptionStringifyStack(Exception e, int maxNumLines) {
		
		StringWriter writer = new StringWriter();
	    e.printStackTrace(new PrintWriter(writer));
	    String[] lines = writer.toString().split("\n");
	    StringBuilder sb = new StringBuilder();
	    
	    for (int i = 0; i < Math.min(lines.length, maxNumLines); i++) {
	        sb.append(lines[i]).append("\n");
	    }
	    return sb.toString();
	}
	
	public static void logException (Exception e, String FILE_LOG, String THIS_RESOURCE) {
		Logger.getLogger(FILE_LOG).error("Exception in " + THIS_RESOURCE + ": " + e.getMessage());
		Logger.getRootLogger().error("Exception in " + THIS_RESOURCE + ": " + e.getMessage());
		Logger.getLogger(SimpaticoProperties.simpaticoLog_Error).error("Exception in " + THIS_RESOURCE + ": " + e.getMessage() + "\n" + SimpaticoResourceUtils.exceptionStringifyStack(e));
	}
	
	public static Response findRequest(HttpServletRequest request, Map<String, List<String>> queryParams, String ES_INDEX, String ES_TYPE, String ES_FIELD_SEARCH, String FILE_LOG, String THIS_RESOURCE) throws IOException, Exception {
		
		ArrayList<String> literalWords = new ArrayList<>();
		String common_key= null;
    	int limit = 0; 
    	String fieldSortName = "";
    	SortOrder sortOrder = SortOrder.ASC; // Inicialize. If fieldSort is empty dont sort
    	
    	Logger.getLogger(FILE_LOG).info("Find documents. IP Remote: " + request.getRemoteAddr() + ". IP Header Real: " + SimpaticoResourceUtils.getRealIPHeader(request) + ". Query: " + queryParams.toString());
    	
    	// Process query params
        for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
            String key = entry.getKey();
            List<String> values = entry.getValue();
            
            // literal words
            if (key.contentEquals(SimpaticoResourceUtils.wordsParam)) {
            	for (String word : values) {
            		// Comma separated and add to array
            		for (String splitWord : word.split(SimpaticoResourceUtils.separateParam)) {
            			literalWords.add(splitWord);
            		}
            	}
            // search fields	
            } else if(key.contentEquals("common")){
            	common_key= values.get(0);
            		
            // Search	
            }else if(key.contentEquals("search")){
            	for (String word : values) {
            		// Comma separated and add to array
            		for (String splitWord : word.split(SimpaticoResourceUtils.separateParam)) {
            			literalWords.add(splitWord);
            		}
            	}
            	
            	//we enter "searchByField to choose the correct function
            	literalWords.add("searchByField");
            	
            // Limit	
            } else if (key.contentEquals(SimpaticoResourceUtils.limitParam)) {
            	if (!values.isEmpty() && Utils.isInteger(values.get(0))) {
            		limit = Integer.parseInt(values.get(0));
            	}
            // Sort
            } else if (key.contentEquals(SimpaticoResourceUtils.sortASCParam)) {
            	fieldSortName = SimpaticoProperties.elasticSearchCreatedFieldName;
            	sortOrder = SortOrder.ASC;
            } else if (key.contentEquals(SimpaticoResourceUtils.sortDESCParam)) {
            	fieldSortName = SimpaticoProperties.elasticSearchCreatedFieldName;
            	sortOrder = SortOrder.DESC;
            } else {
            	// BAD PARAMS
            	Logger.getLogger(FILE_LOG).warn("[BAD REQUEST] Find documents. IP Remote: " + request.getRemoteAddr() + ". IP Header Real: " + SimpaticoResourceUtils.getRealIPHeader(request) + ". Query: " + queryParams.toString());
    			return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverBadRequestCode, SimpaticoResourceUtils.badParamsRequestResponse);
            }
        }
        	        
        SearchResponse responseES;
        // No params, so empty request -> return full documents stored
        if (literalWords.isEmpty()) {
        	responseES = ElasticSearchConnector.getInstance().search(ES_INDEX, ES_TYPE, fieldSortName, sortOrder, limit);
        
        	// Last word of literalWords indicates us search by any field
        }else if(literalWords.get(literalWords.size()-1).equals("searchByField")){
        	literalWords.remove(literalWords.size()-1);
        	responseES = ElasticSearchConnector.getInstance().searchByAnyField(ES_INDEX, ES_TYPE, ES_FIELD_SEARCH, common_key, literalWords, fieldSortName, sortOrder, limit);
        
        }else {
        	responseES = ElasticSearchConnector.getInstance().search(ES_INDEX, ES_TYPE, ES_FIELD_SEARCH, literalWords, fieldSortName, sortOrder, limit);
        }
        
        return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.searchResponse2JSONResponse(responseES));
	}

	public static Response findRequest(HttpServletRequest request, UriInfo uriInfo, String ES_INDEX, String ES_TYPE, String ES_FIELD_SEARCH, String FILE_LOG, String THIS_RESOURCE) throws IOException, Exception {
		
		ArrayList<String> literalWords = new ArrayList<>();
    	int limit = 0; 
    	String common_key= null;
    	String exist= null;
    	String fieldSortName = "";
    	String fieldSearch = "";
    	SortOrder sortOrder = SortOrder.ASC; // Inicialize. If fieldSort is empty dont sort
    	
		// Query params
    	Map<String, List<String>> queryParams = uriInfo.getQueryParameters();
    	Logger.getLogger(FILE_LOG).info("Find documents. IP Remote: " + request.getRemoteAddr() + ". IP Header Real: " + SimpaticoResourceUtils.getRealIPHeader(request) + ". Query: " + queryParams.toString());
    	 	
    	// Process query params
        for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
            String key = entry.getKey();
            List<String> values = entry.getValue();
            
            // literal words
            if (key.contentEquals(SimpaticoResourceUtils.wordsParam)) {
            	for (String word : values) {
            		// Comma separated and add to array
            		for (String splitWord : word.split(SimpaticoResourceUtils.separateParam)) {
            			literalWords.add(splitWord);
            		}
            	}
            //Store common_key
            } else if(key.contentEquals("common")){
            	common_key= values.get(0);
            	
            } else if(key.contentEquals("exist")){
            	if (!values.isEmpty()) {
            		exist= values.get(0);
            	}  	
            		
            // Search	
            } else if(key.contentEquals("search")){
            	for (String word : values) {
            		// Comma separated and add to array
            		for (String splitWord : word.split(SimpaticoResourceUtils.separateParam)) {
            			literalWords.add(splitWord);
            		}
            	}
            	literalWords.add("searchByField");
            		
            // Limit	
            }else if (key.contentEquals(SimpaticoResourceUtils.limitParam)) {
            	if (!values.isEmpty() && Utils.isInteger(values.get(0))) {
            		limit = Integer.parseInt(values.get(0));
            	}
            // Field Search	
            } else if (key.contentEquals("field")) {
            	if (!values.isEmpty()) {
            		fieldSearch = values.get(0);
            		//limit = Integer.parseInt(values.get(0));
            	}
            // Sort
            } else if (key.contentEquals(SimpaticoResourceUtils.sortASCParam)) {
            	fieldSortName = SimpaticoProperties.elasticSearchCreatedFieldName;
            	sortOrder = SortOrder.ASC;
            } else if (key.contentEquals(SimpaticoResourceUtils.sortDESCParam)) {
            	fieldSortName = SimpaticoProperties.elasticSearchCreatedFieldName;
            	sortOrder = SortOrder.DESC;
            } else {
            	// BAD PARAMS
            	Logger.getLogger(FILE_LOG).warn("[BAD REQUEST] Find documents. IP Remote: " + request.getRemoteAddr() + ". IP Header Real: " + SimpaticoResourceUtils.getRealIPHeader(request) + ". Query: " + queryParams.toString());
    			return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverBadRequestCode, SimpaticoResourceUtils.badParamsRequestResponse);
            }
        }
        	        
        SearchResponse responseES;
        
        if((common_key!=null)&&(!fieldSearch.equals(""))){
        //if(!fieldSearch.equals("")){
        	responseES = ElasticSearchConnector.getInstance().searchByKey_Value(ES_INDEX, ES_TYPE, common_key, fieldSearch, literalWords, fieldSortName, sortOrder, limit);
        
        // No params, so empty request -> return full documents stored
		} else if (!fieldSearch.isEmpty()) {
        	//Logger.getRootLogger().info("[RESOURCE_UTILS] FINDING with field");
        	responseES = ElasticSearchConnector.getInstance().search(ES_INDEX, ES_TYPE, fieldSearch, literalWords, fieldSortName, sortOrder, limit);
       
        //Calculate how many documents exist with this field.
		} else if (exist!=null) {
        	responseES = ElasticSearchConnector.getInstance().search_Exist(ES_INDEX, ES_TYPE, exist, common_key, fieldSortName, sortOrder, limit);
		
		} else if (literalWords.isEmpty()) {
        	//Logger.getRootLogger().info("[RESOURCE_UTILS] NOP");
        	responseES = ElasticSearchConnector.getInstance().search(ES_INDEX, ES_TYPE, fieldSortName, sortOrder, limit);
        
        // Last word of literalWords indicates us search by any field with common key
        } else if(literalWords.get(literalWords.size()-1).equals("searchByField")) { 
        	literalWords.remove(literalWords.size()-1);
        	responseES = ElasticSearchConnector.getInstance().searchByAnyField(ES_INDEX, ES_TYPE, ES_FIELD_SEARCH, common_key, literalWords, fieldSortName, sortOrder, limit);
      
		} else {
        	//Logger.getRootLogger().info("[RESOURCE_UTILS] NOP");
        	responseES = ElasticSearchConnector.getInstance().search(ES_INDEX, ES_TYPE, ES_FIELD_SEARCH, literalWords, fieldSortName, sortOrder, limit);
        }
        
        //Filter the result when it's one of two.
        if((fieldSearch.equals("slider_session_feedback_ctz")||fieldSearch.equals("slider_session_feedback_paragraph"))&&(!literalWords.isEmpty())){
        	Response query= SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.searchResponse2JSONResponse(responseES));
        	JSONArray JsonArray_aux= new JSONArray();
        	JSONObject jsonObject_final= new JSONObject();
        	JSONObject jsonObject_data;
        	JSONObject jsonObject = Utils.createJSONObjectIfValid(query.getEntity().toString());
        	        
        	//Get an array with all JSONObject
        	JSONArray JSON_arr= jsonObject.getJSONArray("results");

        	//Create a new JSONArray with values we want
        	for(int i=0; i< JSON_arr.length();i++){
        		JSONObject jsonObject2= (JSONObject) JSON_arr.get(i);
        		jsonObject_data= (JSONObject)jsonObject2.get("data");
        		
        		int valor= (int) jsonObject_data.get(fieldSearch);
        		if(literalWords.contains(Integer.toString(valor))){
        			JsonArray_aux.put((JSONObject) JSON_arr.get(i)); 
        		}			
        	}
        	
        	//Create a definitive JSONObject with values updates
        	Iterator<?> it= jsonObject.keys();
        	while(it.hasNext()){
        		String key= (String)it.next();
        		if(key.equals("count")){
        			jsonObject_final.put(key, JsonArray_aux.length());
        		
        		} else if(key.equals("results")){
        			jsonObject_final.put(key, JsonArray_aux);
        					
        		} else{
        			jsonObject_final.put(key, jsonObject.get(key));	
        			
        		}
        	}
        	return SimpaticoResourceUtils.createMessageResponse(jsonObject_final);		
        
        } else {
        	return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.searchResponse2JSONResponse(responseES));
        }
	}
	
	public static Response insertRequest(HttpServletRequest request, String postData, String ES_INDEX, String ES_TYPE, String ES_FIELD_SEARCH, String FILE_LOG, String THIS_RESOURCE) throws Exception {
		
		String id = "";
		IndexResponse responseInsert;
		Response response;
		
		
		JSONObject jsonObject = Utils.createJSONObjectIfValid(postData);
		if (jsonObject != null) {
			Logger.getLogger(FILE_LOG).info("Insert document. IP Remote: " + request.getRemoteAddr() + ". IP Header Real: " + SimpaticoResourceUtils.getRealIPHeader(request) + ". POST data: " + postData); // Converted in Utils.createJSONStringIfValid
            
            // Elastic search connector
			ElasticSearchConnector connector = ElasticSearchConnector.getInstance();
			
            // Check if exist index
			if (!connector.existsIndex(ES_INDEX)) {
				connector.createIndexWithDateField(ES_INDEX, ES_TYPE, SimpaticoProperties.elasticSearchCreatedFieldName);
			}
			
			
			// Add created time in utc
			jsonObject.put(SimpaticoProperties.elasticSearchCreatedFieldName, new DateTime(new Date()).withZone(DateTimeZone.UTC).toString("yyyy-MM-dd'T'HH:mm:ss'Z'"));
			
			
			// Check if "_id" param inside
			if (jsonObject.has(SimpaticoResourceUtils._idParam)) {
				id = jsonObject.getString(SimpaticoResourceUtils._idParam);
				jsonObject.remove(SimpaticoResourceUtils._idParam);
				
				// Insert data with id
				responseInsert = connector.insertDocument(ES_INDEX, ES_TYPE, id, jsonObject.toString());
			} else {
				// Insert data without id
				responseInsert = connector.insertDocument(ES_INDEX, ES_TYPE, jsonObject.toString());
			}
			
			if (responseInsert.getResult() == Result.UPDATED) {
				response = SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverOkCode, SimpaticoResourceUtils.dataUpdatedESResponse);
			} else {
				response = SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverCreatedCode, SimpaticoResourceUtils.dataInsertedESResponse);
			}
		} else {
			Logger.getLogger(FILE_LOG).warn("[BAD REQUEST] Insert document. IP Remote: " + request.getRemoteAddr() + ". IP Header Real: " + SimpaticoResourceUtils.getRealIPHeader(request) + ". POST data: " + postData);
			response = SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverBadRequestCode, SimpaticoResourceUtils.badPOSTRequestResponse);
		}
		
		return response;
    }
	
	public static Response updateRequest(HttpServletRequest request, String postData, String ES_INDEX, String ES_TYPE, String ES_FIELD_SEARCH, String FILE_LOG, String THIS_RESOURCE) throws Exception {
    	
    	String id = null, content = null;
    	
    	try {
    		Response response;
    		boolean badRequest = false;
    		
    		// Check JSON
    		JSONObject jsonObject = Utils.createJSONObjectIfValid(postData);
    		if (jsonObject == null) {
    			badRequest = true;
    		} else {
    			// Check json attributes
    			id = jsonObject.optString(SimpaticoResourceUtils.idParam);
    			content = jsonObject.optString(SimpaticoResourceUtils.contentParam);
    			if (id == null || id.isEmpty() || content == null || content.isEmpty()) {
    				badRequest = true; 
    			}
    		}
    		
    		if (badRequest) {
    			Logger.getLogger(FILE_LOG).warn("[BAD REQUEST] Update document. IP Remote: " + request.getRemoteAddr() + ". IP Header Real: " + SimpaticoResourceUtils.getRealIPHeader(request) + ". Post data: " + postData);
    			response = SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverBadRequestCode, SimpaticoResourceUtils.badPOSTRequestResponse);
    		} else {
    			// Creates JSON from string content to convert to XContenBuilder
    			JSONObject jsonContent = Utils.createJSONObjectIfValid(content);
    			if (jsonContent == null) { // Content not on Json format
    				Logger.getLogger(FILE_LOG).warn("[BAD REQUEST] Update document. IP Remote: " + request.getRemoteAddr() + ". IP Header Real: " + SimpaticoResourceUtils.getRealIPHeader(request) + ". Post data: " + postData);
    				response = SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverBadRequestCode, SimpaticoResourceUtils.badPOSTRequestResponse);
    			} else {
    				Logger.getLogger(FILE_LOG).info("Update document. IP Remote: " + request.getRemoteAddr() + ". IP Header Real: " + SimpaticoResourceUtils.getRealIPHeader(request) + ". Post data: " + postData);
    				
    				String message = jsonContent.toString();
        			XContentParser parser = XContentFactory.xContent(XContentType.JSON).createParser(message.getBytes());
        			parser.close();
        			XContentBuilder builder = jsonBuilder().copyCurrentStructure(parser);
        			
        			// Elastic search connector
        			ElasticSearchConnector connector = ElasticSearchConnector.getInstance();
        			
        			// Update data
        			UpdateResponse update = connector.update(ES_INDEX, ES_TYPE, id, builder);
        			if (update.getResult() == Result.CREATED) {
        				response = SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverCreatedCode, SimpaticoResourceUtils.dataInsertedESResponse);
	    			} else if (update.getResult() == Result.UPDATED) {
	    				response = SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverOkCode, SimpaticoResourceUtils.dataUpdatedESResponse);
	    			} else {
	    				response = SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.dataInsertedESResponse);
	    			}   
    			}
    		}

    		return response;
    	} catch (DocumentMissingException e) {
    		Logger.getLogger(FILE_LOG).info("Document missing, inserting...");
    		
    		// Include in json _id to insert with these id
    		JSONObject jsonInsertContent = Utils.createJSONObjectIfValid(content);
			jsonInsertContent.put(SimpaticoResourceUtils._idParam, id);
    		return insertRequest(request, jsonInsertContent.toString(), ES_INDEX, ES_TYPE, ES_FIELD_SEARCH, FILE_LOG, THIS_RESOURCE);
    	}
    }
	
	public static Response removeRequest(HttpServletRequest request, String postData, String ES_INDEX, String ES_TYPE, String ES_FIELD_SEARCH, String FILE_LOG, String THIS_RESOURCE) throws Exception {
        	
		Response response;
		boolean badRequest = false;
		String id = null;
		
		// Check JSON
		JSONObject jsonObject = Utils.createJSONObjectIfValid(postData);
		if (jsonObject == null) {
			badRequest = true;
		} else {
			// Check json attributes
			id = jsonObject.optString(SimpaticoResourceUtils.idParam);
			if (id == null || id.isEmpty()) {
				badRequest = true; 
			}
		}
		
		if (badRequest) {
			Logger.getLogger(FILE_LOG).warn("[BAD REQUEST] Delete document. IP Remote: " + request.getRemoteAddr() + ". IP Header Real: " + SimpaticoResourceUtils.getRealIPHeader(request) + ". Post data: " + postData);
			response = SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverBadRequestCode, SimpaticoResourceUtils.badPOSTRequestResponse);
			
		} else {    		
			Logger.getLogger(FILE_LOG).info("Delete document. IP Remote: " + request.getRemoteAddr() + ". IP Header Real: " + SimpaticoResourceUtils.getRealIPHeader(request) + ". Post data: " + postData); 
			
            // Elastic search connector
			ElasticSearchConnector connector = ElasticSearchConnector.getInstance();
			
            // Check if exist index
			if (!connector.existsIndex(ES_INDEX)) {
				connector.createIndexWithDateField(ES_INDEX, ES_TYPE, SimpaticoProperties.elasticSearchCreatedFieldName);
			}
			
			// Delete data
			DeleteResponse delete = connector.deleteDocument(ES_INDEX, ES_TYPE, id);
			if (delete.getResult() == Result.NOT_FOUND) {
				response = SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverNoContentCode, "");
			} else {
				response = SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverOkCode, SimpaticoResourceUtils.dataRemovedESResponse);
			}    		
		}
		return response;
    }
	
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
    	JSONObject jsonObj = new JSONObject();
    	try {
			jsonObj.put("message", message.trim());
			return Response.status(status).entity(jsonObj.toString()).build();
		} catch (JSONException e) {
			String msg = "{\"message\": \"Error Generating message\"}";
			return Response.status(status).entity(msg).build();
		}
    }
    
    /**
     * Create message JSON response with 200 code status 
     */
    public static Response createMessageResponse (JSONObject json) {
    	return Response.status(serverOkCode).entity(json.toString()).build();
    }
    
    /**
     * Return headers 
     */
    public static String getHeaders (HttpServletRequest request) {
    	String rtn = new String();
    	
    	@SuppressWarnings("rawtypes")
        Enumeration headerNames = request.getHeaderNames();
        while(headerNames.hasMoreElements()) {
     	   String headerName = (String)headerNames.nextElement();
     	   rtn += headerName + ": " + request.getHeader(headerName) + " ";
        }
        return rtn;
    }
    
    /**
     * Return header from Http request
     */
    public static String getHeader (HttpServletRequest request, String header) {
    	
    	String ret = request.getHeader(header);
    	return ret != null ? ret: "";
    }
    
    /**
     * Return real ip in header from proxy (i.e. Nginx)
     */
    public static String getRealIPHeader (HttpServletRequest request) {
    	
    	return request.getHeader(SimpaticoProperties.realIpHeaderName);
    }
}
