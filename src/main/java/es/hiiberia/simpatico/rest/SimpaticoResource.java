package es.hiiberia.simpatico.rest;

import java.io.PrintWriter;
import java.io.StringWriter;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.engine.DocumentMissingException;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.internal.InternalSearchResponse;

import es.hiiberia.simpatico.utils.ElasticSearchConnector;
import es.hiiberia.simpatico.utils.SimpaticoProperties;
import es.hiiberia.simpatico.utils.Utils;

import static org.elasticsearch.common.xcontent.XContentFactory.*;

@Path("/")
public class SimpaticoResource {

	public static String internalErrorResponse = "Internal error";
	public static String badPOSTRequestResponse = "Bad request. Post data must be json";
	public static String dataInsertedESResponse = "Data inserted";
	public static String dataUpdatedESResponse = "Data updated";
	
	// Variables message response
	public static String responseJSONResults = "results";		// JSON array of each result
	public static String responseJSONNumResults = "count";		// Num results founded in search
	public static String responseJSONId = "id";					// Each document id 
	public static String responseJSONData = "data";				// Each document data
	public static String responseJSONScore = "score";			// Each document score
	
	// Public params
	/* find */
	
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
	
	
    @GET
    @Path("/find")
    @Produces(MediaType.APPLICATION_JSON)
    public Response find(@Context HttpServletRequest request, @Context UriInfo uriInfo) {
        	
    	ArrayList<String> literalWords = new ArrayList<>();
    	
    	//@QueryParam("literalword") List<String> literalWordsParam, 
 		//@QueryParam("wordEspaces") String wordSpaces,
    	//@QueryParam("wildcard") String wildCards
    	try {
    		// Query params
	    	Map<String, List<String>> queryParams = uriInfo.getQueryParameters();
	    	Logger.getLogger(SimpaticoProperties.simpaticoLogClients).info("Find data. IP Remote: " + request.getRemoteAddr() + ". Query: " + queryParams.toString());
	    	 	
	    	// Process query params
	        for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
	            String key = entry.getKey();
	            List<String> values = entry.getValue();
	            
	            // literal words
	            if (key.contentEquals("literalword")) {
	            	for (String word : values) {
	            		// Comma separated and add to array
	            		for (String splitWord : word.split(separateParam)) {
	            			literalWords.add(splitWord);
	            		}
	            	}
	            }
	        }
	        
	        /******/
	        /*try {		
	        	TransportClient client = ElasticSearchConnector.getInstance().getClient();
				// Specific words   
				QueryBuilder qb, qb2, qb3;
				
				qb = QueryBuilders.termsQuery(SimpaticoProperties.elasticSearchFieldSearch, literalWords);
				qb2 = QueryBuilders.wildcardQuery(SimpaticoProperties.elasticSearchFieldSearch, "?est*");
				//qb = QueryBuilders.matchQuery(SimpaticoProperties.elasticSearchFieldSearch, "");
		
				SearchResponse response = client.prepareSearch(SimpaticoProperties.elasticSearchIndex)
				        .setQuery(qb)
				      
				        //.setQuery(qb2)
				        .get();
			
				return createMessageResponse(serverInternalServerErrorCode, response.toString());
			} catch (IndexNotFoundException e) {
				Logger.getRootLogger().warn("Exception");		
				return createMessageResponse(serverInternalServerErrorCode, "ERROR");
			}*/ 
	        /******/
	        
	        SearchResponse responseES;
	        
	        // No params, so empty request -> return full data stored
	        if (literalWords.isEmpty()) {
	        	Logger.getRootLogger().info("Empty params");
	        	responseES = ElasticSearchConnector.getInstance().searchAllData(SimpaticoProperties.elasticSearchIndex, SimpaticoProperties.elasticSearchFieldSearch, 0, 0);
	        } else {
	        	responseES = ElasticSearchConnector.getInstance().searchData(SimpaticoProperties.elasticSearchIndex, SimpaticoProperties.elasticSearchFieldSearch, literalWords, 0, 0);
	        }
	        
	        return createMessageResponse(searchResponse2JSONResponse(responseES));
    	} catch (Exception e) {
    		// Print the exception and its trace on log
    		Logger.getRootLogger().error("Exception: " + e.getMessage());
			StringWriter errors = new StringWriter();
    		e.printStackTrace(new PrintWriter(errors));
    		Logger.getRootLogger().error(errors.toString());
    		return createMessageResponse(serverInternalServerErrorCode, internalErrorResponse);
    	}
    }
    
    public static SearchResponse joinSearchResponse(SearchResponse sr1, SearchResponse sr2, boolean associative) {
		return sr2;
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
    			Logger.getLogger(SimpaticoProperties.simpaticoLogClients).info("Insert data. IP Remote: " + request.getRemoteAddr() + ". POST data: " + postData); // Converted in Utils.createJSONStringIfValid
                
                // Elastic search connector
    			ElasticSearchConnector connector = ElasticSearchConnector.getInstance();
    			
                // Check if exist index
    			if (!connector.existsIndex(SimpaticoProperties.elasticSearchIndex)) {
    				connector.createIndex(SimpaticoProperties.elasticSearchIndex);
    			}
    			
    			// Check if "_id" param inside
    			if (jsonObject.has(_idParam)) {
    				id = jsonObject.getString(_idParam);
    				jsonObject.remove(_idParam);
    				// Insert data with id
    				responseInsert = connector.insertData(SimpaticoProperties.elasticSearchIndex, SimpaticoProperties.elasticSearchType, id, jsonObject.toString());
    			} else {
    				// Insert data without id
    				responseInsert = connector.insertData(SimpaticoProperties.elasticSearchIndex, SimpaticoProperties.elasticSearchType, jsonObject.toString());
    			}
    			
    			if (responseInsert.getResult() == Result.UPDATED) {
					response = createMessageResponse(serverOkCode, dataUpdatedESResponse);
				} else {
					response = createMessageResponse(serverCreatedCode, dataInsertedESResponse);
				}
    		} else {
    			Logger.getLogger(SimpaticoProperties.simpaticoLogClients).warn("[BAD REQUEST] Insert data. IP Remote: " + request.getRemoteAddr() + ". POST data: " + postData);
    			response = createMessageResponse(serverBadRequestCode, badPOSTRequestResponse);
    		}
    		
    		return response;
    	} catch (Exception e) {
			StringWriter err = new StringWriter();
			e.printStackTrace(new PrintWriter(err));
			Logger.getRootLogger().error(err.toString());
    		return createMessageResponse(serverInternalServerErrorCode, internalErrorResponse);
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
    			id = jsonObject.optString(idParam);
    			content = jsonObject.optString(contentParam);
    			if (id == null || id.isEmpty() || content == null || content.isEmpty()) {
    				badRequest = true; 
    			}
    		}
    		
    		if (badRequest) {
    			Logger.getLogger(SimpaticoProperties.simpaticoLogClients).info("[BAD REQUEST] Update data. IP Remote: " + request.getRemoteAddr() + ". Post data: " + postData);
    			response = createMessageResponse(serverBadRequestCode, badPOSTRequestResponse);
    		} else {
    			// Creates JSON from string content to convert to XContenBuilder
    			JSONObject jsonContent = Utils.createJSONObjectIfValid(content);
    			if (jsonContent == null) { // Content not on Json format
    				Logger.getLogger(SimpaticoProperties.simpaticoLogClients).info("[BAD REQUEST] Update data. IP Remote: " + request.getRemoteAddr() + ". Post data: " + postData);
    				response = createMessageResponse(serverBadRequestCode, badPOSTRequestResponse);
    			} else {
    				Logger.getLogger(SimpaticoProperties.simpaticoLogClients).info("Update data. IP Remote: " + request.getRemoteAddr() + ". Post data: " + postData);
    				
    				String message = jsonContent.toString();
        			XContentParser parser = XContentFactory.xContent(XContentType.JSON).createParser(message.getBytes());
        			parser.close();
        			XContentBuilder builder = jsonBuilder().copyCurrentStructure(parser);
        			
        			// Elastic search connector
        			ElasticSearchConnector connector = ElasticSearchConnector.getInstance();
        			
        			// Update data
        			UpdateResponse update = connector.update(SimpaticoProperties.elasticSearchIndex, SimpaticoProperties.elasticSearchType, id, builder);
        			if (update.getResult() == Result.CREATED) {
        				response = createMessageResponse(serverCreatedCode, dataInsertedESResponse);
	    			} else if (update.getResult() == Result.UPDATED) {
	    				response = createMessageResponse(serverOkCode, dataUpdatedESResponse);
	    			} else {
	    				response = createMessageResponse(serverInternalServerErrorCode, dataInsertedESResponse);
	    			}   
    			}
    		}

    		return response;
    	} catch (DocumentMissingException e) {
    		Logger.getRootLogger().info("Document missing, inserting...");
    		
    		// Include in json _id to insert with these id
    		JSONObject jsonInsertContent = Utils.createJSONObjectIfValid(content);
    		try {
				jsonInsertContent.put(_idParam, id);
			} catch (JSONException e1) {
				StringWriter err = new StringWriter();
				e1.printStackTrace(new PrintWriter(err));
				Logger.getRootLogger().error(err.toString());
				return createMessageResponse(serverInternalServerErrorCode, internalErrorResponse);
			}
    		return this.insert(request, jsonInsertContent.toString());
    	} catch (Exception e) {
			StringWriter err = new StringWriter();
			e.printStackTrace(new PrintWriter(err));
			Logger.getRootLogger().error(err.toString());
			return createMessageResponse(serverInternalServerErrorCode, internalErrorResponse);
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
    			id = jsonObject.optString(idParam);
    			if (id == null || id.isEmpty()) {
    				badRequest = true; 
    			}
    		}
    		
    		if (badRequest) {
    			Logger.getLogger(SimpaticoProperties.simpaticoLogClients).info("[BAD REQUEST] Delete data. IP Remote: " + request.getRemoteAddr() + ". Post data: " + postData);
    			response = createMessageResponse(serverBadRequestCode, badPOSTRequestResponse);
    			
    		} else {    		
    			Logger.getLogger(SimpaticoProperties.simpaticoLogClients).info("Delete data. IP Remote: " + request.getRemoteAddr() + ". Post data: " + postData); 
    			
	            // Elastic search connector
				ElasticSearchConnector connector = ElasticSearchConnector.getInstance();
				
	            // Check if exist index
				if (!connector.existsIndex(SimpaticoProperties.elasticSearchIndex)) {
					connector.createIndex(SimpaticoProperties.elasticSearchIndex);
				}
				
				// Delete data
				DeleteResponse delete = connector.delete(SimpaticoProperties.elasticSearchIndex, SimpaticoProperties.elasticSearchType, id);
				if (delete.getResult() == Result.NOT_FOUND) {
					response = createMessageResponse(serverNoContentCode, dataInsertedESResponse);
				} else {
					response = createMessageResponse(serverOkCode, dataInsertedESResponse);
				}    		
    		}
    		return response;
    	} catch (Exception e) {
			StringWriter err = new StringWriter();
			e.printStackTrace(new PrintWriter(err));
			Logger.getRootLogger().error(err.toString());
			return createMessageResponse(serverInternalServerErrorCode, internalErrorResponse);
    	}
    }
        
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
     * Create message 
     */
    public Response createMessageResponse (int status, String message) {
    	// {"message": <message>}
    	String msg = "{\"message\": \"" + message.trim() + "\"}";
    	return Response.status(status).entity(msg).build();
    }
    
    /**
     * Create message  
     */
    public Response createMessageResponse (JSONObject json) {
    	return Response.status(serverOkCode).entity(json.toString()).build();
    }
    
	/** Test Method **/
    @GET
	@Path("/test")
	@Produces(MediaType.APPLICATION_JSON)
	public Response test() {
    	return createMessageResponse(serverOkCode, "Welcome to SIMPATICO API!");
	}
    
    /** Test Method **/
    @GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public void index() {
    	URI uri = UriBuilder.fromUri("http://localhost:8080/simpatico").build();
    	Response.seeOther(uri);
	}
}
