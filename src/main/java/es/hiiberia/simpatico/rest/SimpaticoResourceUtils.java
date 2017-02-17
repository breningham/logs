package es.hiiberia.simpatico.rest;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
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
	
	public static Response findRequest(HttpServletRequest request, UriInfo uriInfo, String ES_INDEX, String ES_TYPE, String ES_FIELD_SEARCH, String FILE_LOG, String THIS_RESOURCE) {
		
		ArrayList<String> literalWords = new ArrayList<>();
    	int limit = 0; 
    	String fieldSortName = "";
    	SortOrder sortOrder = SortOrder.ASC; // Inicialize. If fieldSort is empty dont sort
    	
    	try {
    		// Query params
	    	Map<String, List<String>> queryParams = uriInfo.getQueryParameters();
	    	Logger.getLogger(FILE_LOG).info("Find documents. IP Remote: " + request.getRemoteAddr() + ". Query: " + queryParams.toString());
	    	 	
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
	            	Logger.getLogger(FILE_LOG).warn("[BAD REQUEST] Find documents. IP Remote: " + request.getRemoteAddr() + ". Query: " + queryParams.toString());
	    			return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverBadRequestCode, SimpaticoResourceUtils.badParamsRequestResponse);
	            }
	        }
	        	        
	        SearchResponse responseES;
	        // No params, so empty request -> return full documents stored
	        if (literalWords.isEmpty()) {
	        	responseES = ElasticSearchConnector.getInstance().search(ES_INDEX, fieldSortName, sortOrder, limit);
	        } else {
	        	responseES = ElasticSearchConnector.getInstance().search(ES_INDEX, ES_FIELD_SEARCH, literalWords, fieldSortName, sortOrder, limit);
	        }
	        
	        return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.searchResponse2JSONResponse(responseES));
    	} catch (Exception e) {
    		// Print the exception and its trace on log
    		Logger.getLogger(FILE_LOG).error("Exception in " + THIS_RESOURCE + ": " + e.getMessage());
    		Logger.getRootLogger().error("Exception in " + THIS_RESOURCE + ": " + e.getMessage());
			Logger.getLogger(SimpaticoProperties.simpaticoLog_Error).error("Exception in " + THIS_RESOURCE + ": " + e.getMessage() + "\n" + SimpaticoResourceUtils.exceptionStringifyStack(e));
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse);
    	}
	}
	
	public static Response insertRequest(HttpServletRequest request, String postData, String ES_INDEX, String ES_TYPE, String ES_FIELD_SEARCH, String FILE_LOG, String THIS_RESOURCE) {
    	try {
			String id = "";
			IndexResponse responseInsert;
    		Response response;
    		
    		JSONObject jsonObject = Utils.createJSONObjectIfValid(postData);
    		if (jsonObject != null) {
    			Logger.getLogger(FILE_LOG).info("Insert document. IP Remote: " + request.getRemoteAddr() + ". POST data: " + postData); // Converted in Utils.createJSONStringIfValid
                
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
    			Logger.getLogger(FILE_LOG).warn("[BAD REQUEST] Insert document. IP Remote: " + request.getRemoteAddr() + ". POST data: " + postData);
    			response = SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverBadRequestCode, SimpaticoResourceUtils.badPOSTRequestResponse);
    		}
    		
    		return response;
    	} catch (Exception e) {
    		Logger.getLogger(FILE_LOG).error("Exception in " + THIS_RESOURCE + ": " + e.getMessage());
    		Logger.getRootLogger().error("Exception in " + THIS_RESOURCE + ": " + e.getMessage());
			Logger.getLogger(SimpaticoProperties.simpaticoLog_Error).error("Exception in " + THIS_RESOURCE + ": " + e.getMessage() + "\n" + SimpaticoResourceUtils.exceptionStringifyStack(e));
    		
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse);
    	}
    }
	
	public static Response updateRequest(HttpServletRequest request, String postData, String ES_INDEX, String ES_TYPE, String ES_FIELD_SEARCH, String FILE_LOG, String THIS_RESOURCE) {
    	
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
    			Logger.getLogger(FILE_LOG).warn("[BAD REQUEST] Update document. IP Remote: " + request.getRemoteAddr() + ". Post data: " + postData);
    			response = SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverBadRequestCode, SimpaticoResourceUtils.badPOSTRequestResponse);
    		} else {
    			// Creates JSON from string content to convert to XContenBuilder
    			JSONObject jsonContent = Utils.createJSONObjectIfValid(content);
    			if (jsonContent == null) { // Content not on Json format
    				Logger.getLogger(FILE_LOG).warn("[BAD REQUEST] Update document. IP Remote: " + request.getRemoteAddr() + ". Post data: " + postData);
    				response = SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverBadRequestCode, SimpaticoResourceUtils.badPOSTRequestResponse);
    			} else {
    				Logger.getLogger(FILE_LOG).info("Update document. IP Remote: " + request.getRemoteAddr() + ". Post data: " + postData);
    				
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
    		try {
				jsonInsertContent.put(SimpaticoResourceUtils._idParam, id);
			} catch (JSONException e1) {
				Logger.getLogger(FILE_LOG).error("Exception in " + THIS_RESOURCE + ": " + e1.getMessage());
	    		Logger.getRootLogger().error("Exception in " + THIS_RESOURCE + ": " + e1.getMessage());
				Logger.getLogger(SimpaticoProperties.simpaticoLog_Error).error("Exception in " + THIS_RESOURCE + ": " + e1.getMessage() + "\n" + SimpaticoResourceUtils.exceptionStringifyStack(e1));
	    		
				return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse);
			}
    		return insertRequest(request, jsonInsertContent.toString(), ES_INDEX, ES_TYPE, ES_FIELD_SEARCH, FILE_LOG, THIS_RESOURCE);
    	} catch (Exception e) {
    		Logger.getLogger(FILE_LOG).error("Exception in " + THIS_RESOURCE + ": " + e.getMessage());
    		Logger.getRootLogger().error("Exception in " + THIS_RESOURCE + ": " + e.getMessage());
			Logger.getLogger(SimpaticoProperties.simpaticoLog_Error).error("Exception in " + THIS_RESOURCE + ": " + e.getMessage() + "\n" + SimpaticoResourceUtils.exceptionStringifyStack(e));
    		
			return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse);
    	}
    }
	
	public static Response removeRequest(HttpServletRequest request, String postData, String ES_INDEX, String ES_TYPE, String ES_FIELD_SEARCH, String FILE_LOG, String THIS_RESOURCE) {
    	try {        	
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
    			Logger.getLogger(FILE_LOG).warn("[BAD REQUEST] Delete document. IP Remote: " + request.getRemoteAddr() + ". Post data: " + postData);
    			response = SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverBadRequestCode, SimpaticoResourceUtils.badPOSTRequestResponse);
    			
    		} else {    		
    			Logger.getLogger(FILE_LOG).info("Delete document. IP Remote: " + request.getRemoteAddr() + ". Post data: " + postData); 
    			
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
    	} catch (Exception e) {
    		Logger.getLogger(FILE_LOG).error("Exception in " + THIS_RESOURCE + ": " + e.getMessage());
    		Logger.getRootLogger().error("Exception in " + THIS_RESOURCE + ": " + e.getMessage());
			Logger.getLogger(SimpaticoProperties.simpaticoLog_Error).error("Exception in " + THIS_RESOURCE + ": " + e.getMessage() + "\n" + SimpaticoResourceUtils.exceptionStringifyStack(e));
    		
			return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse);
    	}
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
