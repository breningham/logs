package es.hiiberia.simpatico.rest;

import java.net.URI;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
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
import org.elasticsearch.search.sort.SortOrder;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import es.hiiberia.simpatico.utils.ElasticSearchConnector;
import es.hiiberia.simpatico.utils.SimpaticoProperties;
import es.hiiberia.simpatico.utils.Utils;

import static org.elasticsearch.common.xcontent.XContentFactory.*;

@Path("/testanalytics")
public class SimpaticoResourceTest {

	private static String ES_INDEX = "test";
	private static String ES_TYPE =  "json";
	private static String ES_FIELD_SEARCH = SimpaticoProperties.elasticSearchFieldSearch;
	private static String FILE_LOG = SimpaticoProperties.simpaticoLog_Test;
	private static String THIS_RESOURCE = "Test";
	
	
    @GET
    @Path("/find")
    @Produces(MediaType.APPLICATION_JSON)
    public Response find(@Context HttpServletRequest request, @Context UriInfo uriInfo) {
        	
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
    		return this.insert(request, jsonInsertContent.toString());
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
